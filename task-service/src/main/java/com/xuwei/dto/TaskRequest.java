package com.xuwei.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "baseFee must be provided")
    @DecimalMin(value = "0.0", inclusive = true, message = "baseFee must be >= 0")
    private BigDecimal baseFee;

    @NotNull(message = "perKmRate must be provided")
    @DecimalMin(value = "0.0", inclusive = true, message = "perKmRate must be >= 0")
    private BigDecimal perKmRate;

    @NotNull(message = "active flag must be provided")
    private Boolean active;


}
