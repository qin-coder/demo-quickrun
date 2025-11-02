package com.xuwei.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuwei.config.ApplicationProperties;
import com.xuwei.dto.*;
import com.xuwei.events.OrderCreatedEvent;
import com.xuwei.events.OrderEventPublisher;
import com.xuwei.model.OrderEntity;
import com.xuwei.model.OrderEventEntity;
import com.xuwei.repository.OrderEventRepository;
import com.xuwei.repository.OrderRepository;
import com.xuwei.service.OrderService;
import com.xuwei.service.TaskServiceClient;
import com.xuwei.utils.PriceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final TaskServiceClient taskServiceClient;
    private final OrderEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final ApplicationProperties props;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderEventRepository orderEventRepository,
                            TaskServiceClient taskServiceClient,
                            OrderEventPublisher eventPublisher,
                            ObjectMapper objectMapper,
                            ApplicationProperties props) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.taskServiceClient = taskServiceClient;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest req) {
        TaskInfoResponse task =
                taskServiceClient.getTaskById(req.getTaskId());
        if (task == null) {
            throw new IllegalStateException("task-service " +
                    "unavailable or task not found: " + req.getTaskId());
        }

        BigDecimal total = PriceCalculator.calculate(task,
                req.getDistanceKm());

        OrderEntity e = new OrderEntity();
        String orderNumber =
                "QR-" + UUID.randomUUID().toString().substring(0, 8);
        e.setOrderNumber(orderNumber);
        e.setUsername(req.getUsername());
        e.setCustomerName(req.getCustomerName());
        e.setCustomerEmail(req.getCustomerEmail());
        e.setCustomerPhone(req.getCustomerPhone());
        e.setDeliveryAddressLine1(req.getDeliveryAddressLine1());
        e.setDeliveryAddressLine2(req.getDeliveryAddressLine2());
        e.setDeliveryAddressCity(req.getDeliveryAddressCity());
        e.setDeliveryAddressState(req.getDeliveryAddressState());
        e.setDeliveryAddressZipCode(req.getDeliveryAddressZipCode());
        e.setDeliveryAddressCountry(req.getDeliveryAddressCountry());
        e.setStatus("CREATED");
        e.setComments("taskId=" + req.getTaskId());
        e.setTotalPrice(total);

        OrderEntity saved = orderRepository.save(e);

        OrderEventEntity evtEntity = new OrderEventEntity();
        evtEntity.setOrderNumber(saved.getOrderNumber());
        String eventId = UUID.randomUUID().toString();
        evtEntity.setEventId(eventId);
        evtEntity.setEventType("ORDER_CREATED");
        try {
            OrderCreatedEvent ev = new OrderCreatedEvent();
            ev.setOrderNumber(saved.getOrderNumber());
            ev.setUsername(saved.getUsername());
            ev.setTotalPrice(saved.getTotalPrice());
            ev.setCreatedAt(saved.getCreatedAt());
            String payload = objectMapper.writeValueAsString(ev);
            evtEntity.setPayload(payload);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize event",
                    ex);
        }
        evtEntity.setCreatedAt(java.time.LocalDateTime.now());
        orderEventRepository.save(evtEntity);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                OrderCreatedEvent publishedEvent =
                        new OrderCreatedEvent();
                publishedEvent.setOrderNumber(saved.getOrderNumber());
                publishedEvent.setUsername(saved.getUsername());
                publishedEvent.setTotalPrice(saved.getTotalPrice());
                publishedEvent.setCreatedAt(saved.getCreatedAt());
                eventPublisher.publishOrderCreated(publishedEvent);
            }
        });

        return new CreateOrderResponse(saved.getOrderNumber(),
                saved.getTotalPrice(), saved.getStatus());
    }
}
