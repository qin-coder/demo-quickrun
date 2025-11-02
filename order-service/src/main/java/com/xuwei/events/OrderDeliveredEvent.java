package com.xuwei.events;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDeliveredEvent {
    private String orderNumber;
    private String deliveryPerson;
    private LocalDateTime deliveredAt;
}
