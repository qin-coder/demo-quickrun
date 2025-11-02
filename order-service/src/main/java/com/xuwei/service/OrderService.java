package com.xuwei.service;

import com.xuwei.dto.CreateOrderRequest;
import com.xuwei.dto.CreateOrderResponse;

public interface OrderService {
    CreateOrderResponse createOrder(CreateOrderRequest req);
}
