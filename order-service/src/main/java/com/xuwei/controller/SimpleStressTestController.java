package com.xuwei.controller;

import com.xuwei.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/stress-test")
@RequiredArgsConstructor
public class SimpleStressTestController {

    private final RabbitTemplate rabbitTemplate;
    private final AtomicLong messageCounter = new AtomicLong(0);


    @PostMapping("/send-messages")
    public ResponseEntity<String> sendMessages(
            @RequestParam(defaultValue = "1000") int count,
            @RequestParam(defaultValue = "new-orders") String queueName) {

        log.info("Starting stress test: sending {} messages to queue: {}", count, queueName);

        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            try {
                OrderCreatedEvent event = createTestEvent(i);
                rabbitTemplate.convertAndSend("orders-exchange", queueName, event);
                messageCounter.incrementAndGet();

                if (i % 100 == 0) {
                    log.info("Sent {}/{} messages", i, count);
                }
            } catch (Exception e) {
                log.error("Failed to send message {}: {}", i, e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double rate = (double) count / (duration / 1000.0);
        
        log.info("Stress test completed: {} messages in {}ms ({} msg/sec)",
                count, duration, String.format("%.2f", rate));
        
        return ResponseEntity.ok(String.format(
            "Sent %d messages in %d ms (%.2f msg/sec)", count, duration, rate));
    }


    @PostMapping("/continuous")
    public ResponseEntity<String> startContinuousLoad(
            @RequestParam(defaultValue = "100") int messagesPerSecond,
            @RequestParam(defaultValue = "60") int durationSeconds) {
        
        new Thread(() -> {
            log.info("🔥 Starting continuous load: {} msg/sec for {} seconds", 
                    messagesPerSecond, durationSeconds);
            
            long endTime = System.currentTimeMillis() + (durationSeconds * 1000);
            int intervalMs = 1000 / messagesPerSecond;
            long totalSent = 0;
            
            while (System.currentTimeMillis() < endTime) {
                try {
                    OrderCreatedEvent event = createTestEvent((int) totalSent);
                    rabbitTemplate.convertAndSend("orders-exchange", "new-orders", event);
                    totalSent++;
                    messageCounter.incrementAndGet();
                    
                    Thread.sleep(intervalMs);
                } catch (Exception e) {
                    log.error("Error sending message: {}", e.getMessage());
                }
            }
            
            log.info(" Continuous load completed: {} total messages sent", totalSent);
        }).start();
        
        return ResponseEntity.ok("Continuous load test started");
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStats() {
        return ResponseEntity.ok("Total messages sent: " + messageCounter.get());
    }

    private OrderCreatedEvent createTestEvent(int index) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderNumber("STRESS-" + UUID.randomUUID().toString().substring(0, 8));
        event.setUsername("stress-user");
        event.setTotalPrice(new BigDecimal("99.99"));
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }
}