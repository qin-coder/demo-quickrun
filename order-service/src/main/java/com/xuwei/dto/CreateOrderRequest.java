package com.xuwei.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    @NotBlank private String username;
    @NotBlank private String customerName;
    @NotBlank private String customerEmail;
    @NotBlank private String customerPhone;
    @NotBlank private String deliveryAddressLine1;
    private String deliveryAddressLine2;
    @NotBlank private String deliveryAddressCity;
    @NotBlank private String deliveryAddressState;
    @NotBlank private String deliveryAddressZipCode;
    @NotBlank private String deliveryAddressCountry;

    @NotNull private Long taskId;
    @NotNull @DecimalMin("0.0") private BigDecimal distanceKm;

}
