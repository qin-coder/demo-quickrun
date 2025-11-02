package com.xuwei.events;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderCancelledEvent {
    private String orderNumber;
    private String reason;
    private LocalDateTime cancelledAt;
}
