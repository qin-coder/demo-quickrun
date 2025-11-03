package com.xuwei.service;

import com.xuwei.dto.*;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface OrderService {

    CreateOrderResponse createOrder(CreateOrderRequest req);

    PagedResult<OrderResponse> getAllOrders(Pageable pageable);

    Optional<OrderResponse> getOrderById(Long id);

    Optional<OrderResponse> updateOrderStatus(Long id, UpdateOrderStatusRequest req);

    void deleteOrder(Long id);

    String createOrderOptimized(CreateOrderRequest req);


    int createOrdersInBatch(int batchSize);

    CreateOrderRequest generateRandomOrderRequest();
}