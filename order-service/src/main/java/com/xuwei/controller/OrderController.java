package com.xuwei.controller;

import com.xuwei.dto.CreateOrderRequest;
import com.xuwei.dto.CreateOrderResponse;
import com.xuwei.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest req) {
        CreateOrderResponse res = orderService.createOrder(req);
        return ResponseEntity.created(URI.create("/api/orders/" + res.getOrderNumber())).body(res);
    }
}
