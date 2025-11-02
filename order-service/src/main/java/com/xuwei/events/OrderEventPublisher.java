package com.xuwei.events;

import com.xuwei.config.ApplicationProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties props;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate, ApplicationProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publishOrderCreated(OrderCreatedEvent ev) {
        rabbitTemplate.convertAndSend(props.getOrderEventsExchange(), props.getNewOrdersQueue(), ev);
    }
}
