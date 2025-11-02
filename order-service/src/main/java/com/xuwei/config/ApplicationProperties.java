package com.xuwei.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationProperties {

    private String orderEventsExchange = "quickrun.order.exchange";
    private String newOrdersQueue = "quickrun.order.new";
    private String deliveredOrdersQueue = "quickrun.order.delivered";
    private String cancelledOrdersQueue = "quickrun.order.cancelled";
    private String errorOrdersQueue = "quickrun.order.error";

}
