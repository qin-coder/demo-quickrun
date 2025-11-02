package com.xuwei.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String status;
    private String customerName;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}
