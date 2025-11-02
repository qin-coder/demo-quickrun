package com.xuwei.controller;

import com.xuwei.dto.CreateOrderRequest;
import com.xuwei.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/stress-test")
@RequiredArgsConstructor
public class RealStressTestController {

    private final OrderService orderService;
    private final AtomicLong orderCounter = new AtomicLong(0);


    @PostMapping("/create-real-orders")
    public ResponseEntity<String> createRealOrders(
            @RequestParam(defaultValue = "100") int count) {
        
        log.info("Starting real order stress test: creating {} orders", count);
        
        long startTime = System.currentTimeMillis();
        long successCount = 0;
        long failureCount = 0;
        
        for (int i = 0; i < count; i++) {
            try {
                CreateOrderRequest request = createRealOrderRequest(i);
                orderService.createOrder(request);
                successCount++;
                orderCounter.incrementAndGet();

                if (i % 50 == 0) {
                    log.info("Created {}/{} orders", i, count);
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to create order {}: {}", i, e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double rate = (double) successCount / (duration / 1000.0);
        
        String result = String.format(
            "Real order stress test completed: %d successful, %d failed in %d ms (%.2f orders/sec)", 
            successCount, failureCount, duration, rate);
        
        log.info("{}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/continuous-real-orders")
    public ResponseEntity<String> startContinuousRealOrders(
            @RequestParam(defaultValue = "10") int ordersPerSecond,
            @RequestParam(defaultValue = "30") int durationSeconds) {
        
        new Thread(() -> {
            log.info("Starting continuous real orders: {} orders/sec for {} seconds",
                    ordersPerSecond, durationSeconds);
            
            long endTime = System.currentTimeMillis() + (durationSeconds * 1000);
            int intervalMs = 1000 / ordersPerSecond;
            long totalCreated = 0;
            long totalFailed = 0;
            
            while (System.currentTimeMillis() < endTime) {
                try {
                    CreateOrderRequest request = createRealOrderRequest((int) totalCreated);
                    orderService.createOrder(request);
                    totalCreated++;
                    orderCounter.incrementAndGet();
                    
                    Thread.sleep(intervalMs);
                } catch (Exception e) {
                    totalFailed++;
                    log.error("Error creating order: {}", e.getMessage());
                }
            }
            
            log.info("Continuous real orders completed: {} created, {} failed", totalCreated, totalFailed);
        }).start();
        
        return ResponseEntity.ok("Continuous real order creation started");
    }

    @GetMapping("/real-stats")
    public ResponseEntity<String> getRealStats() {
        return ResponseEntity.ok("Total real orders created: " + orderCounter.get());
    }

    private CreateOrderRequest createRealOrderRequest(int index) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUsername("stress-user-" + index);
        request.setCustomerName("Stress Test Customer " + index);
        request.setCustomerEmail("stress" + index + "@test.com");
        request.setCustomerPhone("1234567890");
        request.setDeliveryAddressLine1(index + " Test Street");
        request.setDeliveryAddressCity("Test City");
        request.setDeliveryAddressState("TS");
        request.setDeliveryAddressZipCode("12345");
        request.setDeliveryAddressCountry("US");
        request.setTaskId(1L);
        request.setDistanceKm(new BigDecimal("5.0"));
        return request;
    }
}