package com.xuwei.events;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderErrorEvent {
    private String orderNumber;
    private String errorMessage;
    private LocalDateTime occurredAt;
}
