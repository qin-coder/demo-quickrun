package com.xuwei.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TaskInfoResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal baseFee;
    private BigDecimal perKmRate;
    private boolean active;
}