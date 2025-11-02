package com.xuwei.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuwei.model.OrderEntity;
import com.xuwei.model.OrderEventEntity;
import com.xuwei.repository.OrderEventRepository;
import com.xuwei.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;

    private final AtomicLong newOrdersProcessed = new AtomicLong(0);
    private final AtomicLong deliveredOrdersProcessed = new AtomicLong(0);
    private final AtomicLong cancelledOrdersProcessed = new AtomicLong(0);
    private final AtomicLong errorOrdersProcessed = new AtomicLong(0);

    private final AtomicInteger newOrdersCurrentRate = new AtomicInteger(0);
    private final AtomicInteger deliveredOrdersCurrentRate = new AtomicInteger(0);
    private final AtomicInteger cancelledOrdersCurrentRate = new AtomicInteger(0);
    private final AtomicInteger errorOrdersCurrentRate = new AtomicInteger(0);

    private final AtomicLong totalNewOrderProcessingTime = new AtomicLong(0);
    private final AtomicLong slowProcessingCount = new AtomicLong(0);

    private final boolean saveEventsToDatabase = true;
    private final boolean saveOrdersFromEvents = false;

    @RabbitListener(queues = "#{@applicationProperties.newOrdersQueue}")
    public void handleNewOrderEvent(OrderCreatedEvent event) {
        long startTime = System.currentTimeMillis();

        try {

            if (saveEventsToDatabase) {
                saveEventToDatabase(event, "ORDER_CREATED");
            }

            if (saveOrdersFromEvents) {
                saveOrderFromEvent(event);
            }

            Thread.sleep(5);

            log.debug("[NEW ORDER] Processed event: orderNumber={}, user={}, totalPrice={}",
                    event.getOrderNumber(), event.getUsername(), event.getTotalPrice());

            newOrdersProcessed.incrementAndGet();
            newOrdersCurrentRate.incrementAndGet();

        } catch (Exception e) {
            log.error("Error processing new order event: {}", e.getMessage());
        } finally {

            long processingTime = System.currentTimeMillis() - startTime;
            totalNewOrderProcessingTime.addAndGet(processingTime);

            if (processingTime > 100) {
                slowProcessingCount.incrementAndGet();
                log.warn("Slow new order processing: {}ms for order {}", processingTime, event.getOrderNumber());
            }
        }
    }

    @RabbitListener(queues = "#{@applicationProperties.deliveredOrdersQueue}")
    public void handleDeliveredOrderEvent(OrderDeliveredEvent event) {
        long startTime = System.currentTimeMillis();

        try {

            if (saveEventsToDatabase) {
                saveEventToDatabase(event, "ORDER_DELIVERED");
            }

            Thread.sleep(3);

            log.debug("[DELIVERED ORDER] Processed event: orderNumber={}, deliveryPerson={}, deliveredAt={}",
                    event.getOrderNumber(), event.getDeliveryPerson(), event.getDeliveredAt());

            deliveredOrdersProcessed.incrementAndGet();
            deliveredOrdersCurrentRate.incrementAndGet();

        } catch (Exception e) {
            log.error("Error processing delivered order event: {}", e.getMessage());
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            if (processingTime > 50) {
                log.warn("Slow delivered order processing: {}ms", processingTime);
            }
        }
    }

    @RabbitListener(queues = "#{@applicationProperties.cancelledOrdersQueue}")
    public void handleCancelledOrderEvent(OrderCancelledEvent event) {
        long startTime = System.currentTimeMillis();

        try {

            if (saveEventsToDatabase) {
                saveEventToDatabase(event, "ORDER_CANCELLED");
            }

            Thread.sleep(3);

            log.debug("[CANCELLED ORDER] Processed event: orderNumber={}, reason={}, cancelledAt={}",
                    event.getOrderNumber(), event.getReason(), event.getCancelledAt());

            cancelledOrdersProcessed.incrementAndGet();
            cancelledOrdersCurrentRate.incrementAndGet();

        } catch (Exception e) {
            log.error("Error processing cancelled order event: {}", e.getMessage());
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            if (processingTime > 50) {
                log.warn("Slow cancelled order processing: {}ms", processingTime);
            }
        }
    }

    @RabbitListener(queues = "#{@applicationProperties.errorOrdersQueue}")
    public void handleErrorOrderEvent(OrderErrorEvent event) {
        long startTime = System.currentTimeMillis();

        try {

            if (saveEventsToDatabase) {
                saveEventToDatabase(event, "ORDER_ERROR");
            }

            Thread.sleep(5);

            log.debug("[ERROR ORDER] Processed event: orderNumber={}, errorMessage={}, occurredAt={}",
                    event.getOrderNumber(), event.getErrorMessage(), event.getOccurredAt());

            errorOrdersProcessed.incrementAndGet();
            errorOrdersCurrentRate.incrementAndGet();

        } catch (Exception e) {
            log.error("Error processing error order event: {}", e.getMessage());
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            if (processingTime > 50) {
                log.warn("Slow error order processing: {}ms", processingTime);
            }
        }
    }

    private void saveEventToDatabase(Object event, String eventType) {
        try {
            String orderNumber = extractOrderNumber(event);

            OrderEventEntity eventEntity = new OrderEventEntity();
            eventEntity.setOrderNumber(orderNumber);
            eventEntity.setEventId(UUID.randomUUID().toString());
            eventEntity.setEventType(eventType);
            eventEntity.setPayload(objectMapper.writeValueAsString(event));
            eventEntity.setCreatedAt(LocalDateTime.now());

            orderEventRepository.save(eventEntity);

            log.trace("Saved {} event to database for order: {}", eventType, orderNumber);

        } catch (Exception e) {
            log.error("Failed to save event to database: {}", e.getMessage());
        }
    }


    private String extractOrderNumber(Object event) {
        if (event instanceof OrderCreatedEvent) {
            return ((OrderCreatedEvent) event).getOrderNumber();
        } else if (event instanceof OrderDeliveredEvent) {
            return ((OrderDeliveredEvent) event).getOrderNumber();
        } else if (event instanceof OrderCancelledEvent) {
            return ((OrderCancelledEvent) event).getOrderNumber();
        } else if (event instanceof OrderErrorEvent) {
            return ((OrderErrorEvent) event).getOrderNumber();
        }
        return "UNKNOWN";
    }


    private void saveOrderFromEvent(OrderCreatedEvent event) {
        try {

            Optional<OrderEntity> existingOrder = orderRepository.findByOrderNumber(event.getOrderNumber());
            if (existingOrder.isPresent()) {
                log.debug("Order already exists: {}", event.getOrderNumber());
                return;
            }

            OrderEntity order = new OrderEntity();
            order.setOrderNumber(event.getOrderNumber());
            order.setUsername(event.getUsername());
            order.setCustomerName("Customer from Event");
            order.setCustomerEmail("event@" + event.getOrderNumber() + ".com");
            order.setCustomerPhone("0000000000");
            order.setDeliveryAddressLine1("Event Address");
            order.setDeliveryAddressCity("Event City");
            order.setDeliveryAddressState("ES");
            order.setDeliveryAddressZipCode("00000");
            order.setDeliveryAddressCountry("US");
            order.setStatus("CREATED_FROM_EVENT");
            order.setTotalPrice(event.getTotalPrice());
            order.setComments("Created from RabbitMQ event during stress test");

            orderRepository.save(order);
            log.debug("Saved order from event: {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to save order from event: {}", e.getMessage());
        }
    }


    private void startPerformanceMonitor() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            int newOrdersRate = newOrdersCurrentRate.getAndSet(0);
            int deliveredOrdersRate = deliveredOrdersCurrentRate.getAndSet(0);
            int cancelledOrdersRate = cancelledOrdersCurrentRate.getAndSet(0);
            int errorOrdersRate = errorOrdersCurrentRate.getAndSet(0);

            long totalNew = newOrdersProcessed.get();
            long avgProcessingTime = totalNew > 0 ?
                    totalNewOrderProcessingTime.get() / totalNew : 0;

            if (newOrdersRate > 0 || deliveredOrdersRate > 0 ||
                    cancelledOrdersRate > 0 || errorOrdersRate > 0) {

                log.info("📊 ORDER EVENT PERFORMANCE METRICS:");
                log.info("   New Orders: {}/sec (total: {}, avg: {}ms, slow: {})",
                        newOrdersRate, totalNew, avgProcessingTime, slowProcessingCount.get());
                log.info("   Delivered Orders: {}/sec (total: {})",
                        deliveredOrdersRate, deliveredOrdersProcessed.get());
                log.info("   Cancelled Orders: {}/sec (total: {})",
                        cancelledOrdersRate, cancelledOrdersProcessed.get());
                log.info("   Error Orders: {}/sec (total: {})",
                        errorOrdersRate, errorOrdersProcessed.get());
                log.info("   ---");
            }

        }, 1, 1, TimeUnit.SECONDS); // 每秒执行一次
    }

    public OrderEventListenerStats getStats() {
        long totalNew = newOrdersProcessed.get();
        long avgProcessingTime = totalNew > 0 ?
                totalNewOrderProcessingTime.get() / totalNew : 0;

        return OrderEventListenerStats.builder()
                .newOrdersProcessed(totalNew)
                .deliveredOrdersProcessed(deliveredOrdersProcessed.get())
                .cancelledOrdersProcessed(cancelledOrdersProcessed.get())
                .errorOrdersProcessed(errorOrdersProcessed.get())
                .averageProcessingTimeMs(avgProcessingTime)
                .slowProcessingCount(slowProcessingCount.get())
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderEventListenerStats {
        private long newOrdersProcessed;
        private long deliveredOrdersProcessed;
        private long cancelledOrdersProcessed;
        private long errorOrdersProcessed;
        private long averageProcessingTimeMs;
        private long slowProcessingCount;
        @Builder.Default
        private long timestamp = System.currentTimeMillis();
    }
}