package com.xuwei.utils;

import com.xuwei.dto.TaskInfoResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceCalculator {
    public static BigDecimal calculate(TaskInfoResponse task, BigDecimal distanceKm) {
        if (task == null) return BigDecimal.ZERO;
        BigDecimal base = task.getBaseFee() == null ? BigDecimal.ZERO : task.getBaseFee();
        BigDecimal perKm = task.getPerKmRate() == null ? BigDecimal.ZERO : task.getPerKmRate();
        BigDecimal dist = distanceKm == null ? BigDecimal.ZERO : distanceKm;
        BigDecimal total = base.add(perKm.multiply(dist));
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
