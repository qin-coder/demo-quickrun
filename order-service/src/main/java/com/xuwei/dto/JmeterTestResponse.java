package com.xuwei.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JmeterTestResponse {
    private boolean success;
    private String message;
    private Long responseTimeMs;
    private Long timestamp;

    private Long totalSuccessOrders;
    private Long totalFailedOrders;
    private Long totalOrders;
    private Long previousTotalOrders;
    private Double successRate;

    private Integer batchSize;
    private Integer successCountInBatch;
    private Integer failedCountInBatch;

    private String orderId;
    private Integer activeRequests;
    private Long asyncQueueSize;
    private Long avgAsyncProcessingTime;
}