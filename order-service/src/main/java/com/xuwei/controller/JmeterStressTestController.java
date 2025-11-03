package com.xuwei.controller;

import com.xuwei.dto.CreateOrderRequest;
import com.xuwei.dto.JmeterTestResponse;
import com.xuwei.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/jmeter-test")
@RequiredArgsConstructor
public class JmeterStressTestController {

    private final OrderService orderService;
    private final AtomicLong successCounter = new AtomicLong(0);
    private final AtomicLong failureCounter = new AtomicLong(0);
    private final AtomicLong totalOrdersCounter = new AtomicLong(0);
    private final AtomicLong asyncQueueSize = new AtomicLong(0);
    private final AtomicLong asyncProcessingTime = new AtomicLong(0);

    private final ConcurrentHashMap<String, Long> processingRequests = new ConcurrentHashMap<>();

    @Qualifier("orderAsyncExecutor")
    private final Executor orderAsyncExecutor;

    @PostMapping("/create-single-order-async")
    public ResponseEntity<JmeterTestResponse> createSingleOrderAsync() {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String requestId = generateRequestId();

        try {
            log.debug("Starting async order creation. RequestID: {}", requestId);

            if (processingRequests.containsKey(requestId)) {
                log.warn("Duplicate request detected. RequestID: {}", requestId);
                return ResponseEntity.status(409)
                        .body(JmeterTestResponse.builder()
                                .success(false)
                                .message("Request is already being processed")
                                .build());
            }

            processingRequests.put(requestId, startTime);
            asyncQueueSize.incrementAndGet();

            CreateOrderRequest request = generateJmeterOrderRequest();

            final String finalRequestId = requestId;
            final long asyncStartTime = System.currentTimeMillis();

            CompletableFuture.runAsync(() -> {
                try {
                    log.debug("Async processing started. RequestID: {}", finalRequestId);

                    String orderId = orderService.createOrderOptimized(request);
                    successCounter.incrementAndGet();
                    totalOrdersCounter.incrementAndGet();

                    long asyncEndTime = System.currentTimeMillis();
                    long asyncDuration = asyncEndTime - asyncStartTime;
                    asyncProcessingTime.addAndGet(asyncDuration);

                    log.debug("Async order created: {} for user: {}. RequestID: {}, Duration: {}ms",
                            orderId, request.getUsername(), finalRequestId, asyncDuration);

                } catch (Exception e) {
                    failureCounter.incrementAndGet();
                    log.error("Failed to create async order. RequestID: {}, Error: {}",
                            finalRequestId, e.getMessage());
                } finally {
                    processingRequests.remove(finalRequestId);
                    asyncQueueSize.decrementAndGet();
                }
            }, orderAsyncExecutor);

            success = true;
            log.debug("Async order submission completed. RequestID: {}", requestId);

        } catch (Exception e) {
            failureCounter.incrementAndGet();
            processingRequests.remove(requestId);
            asyncQueueSize.decrementAndGet();
            log.error("Failed to submit async order. RequestID: {}, Error: {}",
                    requestId, e.getMessage());
        }

        long responseTime = System.currentTimeMillis() - startTime;

        JmeterTestResponse response = JmeterTestResponse.builder()
                .success(success)
                .message(success ? "Order creation submitted" : "Failed to submit order")
                .responseTimeMs(responseTime)
                .totalSuccessOrders(successCounter.get())
                .totalFailedOrders(failureCounter.get())
                .totalOrders(totalOrdersCounter.get())
                .build();

        return success ?
                ResponseEntity.ok(response) :
                ResponseEntity.internalServerError().body(response);
    }

    @PostMapping("/create-batch-orders")
    public ResponseEntity<JmeterTestResponse> createBatchOrders(
            @RequestParam(defaultValue = "5") int batchSize) {

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        String requestId = generateRequestId();

        log.info("Starting batch order creation. RequestID: {}, BatchSize: {}", requestId, batchSize);

        try {

            successCount = orderService.createOrdersInBatch(batchSize);
            totalOrdersCounter.addAndGet(successCount);
            successCounter.addAndGet(successCount);

            log.info("Batch order creation completed. RequestID: {}, SuccessCount: {}",
                    requestId, successCount);

        } catch (Exception e) {
            failureCount = batchSize;
            failureCounter.addAndGet(failureCount);
            log.error("Failed to create batch orders. RequestID: {}, Error: {}",
                    requestId, e.getMessage());
        }

        long responseTime = System.currentTimeMillis() - startTime;

        JmeterTestResponse response = JmeterTestResponse.builder()
                .success(failureCount == 0)
                .message(String.format("Batch completed: %d success, %d failed", successCount, failureCount))
                .responseTimeMs(responseTime)
                .batchSize(batchSize)
                .successCountInBatch(successCount)
                .failedCountInBatch(failureCount)
                .totalSuccessOrders(successCounter.get())
                .totalFailedOrders(failureCounter.get())
                .totalOrders(totalOrdersCounter.get())
                .build();

        return failureCount == 0 ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(207).body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<JmeterTestResponse> getTestStats() {
        long totalOrders = totalOrdersCounter.get();
        double successRate = totalOrders > 0 ?
                (double) successCounter.get() / totalOrders : 0;

        int activeRequests = processingRequests.size();
        long currentAsyncQueue = asyncQueueSize.get();
        long avgAsyncProcessingTime = asyncProcessingTime.get() / Math.max(1, successCounter.get());

        JmeterTestResponse response = JmeterTestResponse.builder()
                .success(true)
                .message("Current test statistics")
                .totalSuccessOrders(successCounter.get())
                .totalFailedOrders(failureCounter.get())
                .totalOrders(totalOrders)
                .successRate(successRate)
                .activeRequests(activeRequests)
                .asyncQueueSize(currentAsyncQueue)
                .avgAsyncProcessingTime(avgAsyncProcessingTime)
                .build();

        log.debug("Test stats queried. TotalOrders: {}, SuccessRate: {:.2f}%, AsyncQueue: {}, AvgAsyncTime: {}ms",
                totalOrders, successRate * 100, currentAsyncQueue, avgAsyncProcessingTime);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/async-stats")
    public ResponseEntity<JmeterTestResponse> getAsyncStats() {
        long totalOrders = totalOrdersCounter.get();
        double successRate = totalOrders > 0 ?
                (double) successCounter.get() / totalOrders : 0;

        int activeRequests = processingRequests.size();
        long currentAsyncQueue = asyncQueueSize.get();
        long avgAsyncProcessingTime = asyncProcessingTime.get() / Math.max(1, successCounter.get());

        JmeterTestResponse response = JmeterTestResponse.builder()
                .success(true)
                .message("Async processing statistics")
                .totalSuccessOrders(successCounter.get())
                .totalFailedOrders(failureCounter.get())
                .totalOrders(totalOrders)
                .successRate(successRate)
                .activeRequests(activeRequests)
                .asyncQueueSize(currentAsyncQueue)
                .avgAsyncProcessingTime(avgAsyncProcessingTime)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-counters")
    public ResponseEntity<JmeterTestResponse> resetCounters() {
        long previousTotal = totalOrdersCounter.get();

        successCounter.set(0);
        failureCounter.set(0);
        totalOrdersCounter.set(0);
        asyncQueueSize.set(0);
        asyncProcessingTime.set(0);
        processingRequests.clear();

        JmeterTestResponse response = JmeterTestResponse.builder()
                .success(true)
                .message("Counters reset successfully")
                .previousTotalOrders(previousTotal)
                .build();

        log.info("Counters reset. Previous total: {}", previousTotal);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<JmeterTestResponse> healthCheck() {
        JmeterTestResponse response = JmeterTestResponse.builder()
                .success(true)
                .message("Service is healthy")
                .timestamp(System.currentTimeMillis())
                .activeRequests(processingRequests.size())
                .asyncQueueSize(asyncQueueSize.get())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cleanup-test-data")
    public ResponseEntity<JmeterTestResponse> cleanupTestData() {
        try {
            long previousTotal = totalOrdersCounter.get();

            successCounter.set(0);
            failureCounter.set(0);
            totalOrdersCounter.set(0);
            asyncQueueSize.set(0);
            asyncProcessingTime.set(0);
            processingRequests.clear();

            JmeterTestResponse response = JmeterTestResponse.builder()
                    .success(true)
                    .message("Test counters cleared. Note: Database data needs separate cleanup.")
                    .previousTotalOrders(previousTotal)
                    .build();

            log.info("Test counters cleared. Previous total: {}", previousTotal);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to cleanup test data: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(JmeterTestResponse.builder()
                            .success(false)
                            .message("Cleanup failed: " + e.getMessage())
                            .build());
        }
    }

    private CreateOrderRequest generateJmeterOrderRequest() {
        long timestamp = System.currentTimeMillis();
        int randomSuffix = (int) (timestamp % 100000);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUsername("jmeter-user-" + randomSuffix);
        request.setCustomerName("JMeter Customer " + randomSuffix);
        request.setCustomerEmail("jmeter" + randomSuffix + "@test.com");
        request.setCustomerPhone("13800138" + (randomSuffix % 10000));
        request.setDeliveryAddressLine1(randomSuffix + " JMeter Street");
        request.setDeliveryAddressCity("Test City");
        request.setDeliveryAddressState("TC");
        request.setDeliveryAddressZipCode("10000" + (randomSuffix % 10));
        request.setDeliveryAddressCountry("CN");
        request.setTaskId((long) (randomSuffix % 10 + 1)); // taskId 1-10
        request.setDistanceKm(new BigDecimal((randomSuffix % 50) + 1)); // 1-50 km

        return request;
    }

    private String generateRequestId() {
        return "req-" + System.currentTimeMillis() + "-" +
                Thread.currentThread().getId() + "-" +
                (int)(Math.random() * 1000);
    }
}