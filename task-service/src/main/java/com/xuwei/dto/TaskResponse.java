package com.xuwei.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal baseFee;
    private BigDecimal perKmRate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
