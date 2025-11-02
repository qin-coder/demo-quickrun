package com.xuwei.utils;

import com.xuwei.dto.CreateOrderRequest;
import com.xuwei.dto.OrderResponse;
import com.xuwei.model.OrderEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderMapper {
    public OrderEntity toEntity(CreateOrderRequest req) {
        OrderEntity order = new OrderEntity();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(req.getCustomerName());
        order.setStatus("CREATED");
        return order;
    }

    public OrderResponse toResponse(OrderEntity entity) {
        return OrderResponse.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .status(entity.getStatus())
                .customerName(entity.getCustomerName())
                .totalPrice(entity.getTotalPrice())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
