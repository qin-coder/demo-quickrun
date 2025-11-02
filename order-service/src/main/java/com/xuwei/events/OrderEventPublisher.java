package com.xuwei.events;

import com.xuwei.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties props;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate, ApplicationProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publishOrderCreated(OrderCreatedEvent ev) {
        try {
            log.info(" Attempting to publish OrderCreatedEvent:");
            log.info(" Exchange: {}", props.getOrderEventsExchange());
            log.info(" Routing Key: {}", props.getNewOrdersQueue());
            log.info(" Order Number: {}", ev.getOrderNumber());

            rabbitTemplate.convertAndSend(props.getOrderEventsExchange(), props.getNewOrdersQueue(), ev);

            log.info("OrderCreatedEvent published successfully");
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent", e);
        }
    }

}
