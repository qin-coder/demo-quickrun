package com.xuwei.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final ApplicationProperties props;

    public RabbitMQConfig(ApplicationProperties props) {
        this.props = props;
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(props.getOrderEventsExchange(), true, false);
    }

    @Bean
    public Queue newOrdersQueue() {
        return QueueBuilder.durable(props.getNewOrdersQueue()).build();
    }

    @Bean
    public Queue deliveredOrdersQueue() {
        return QueueBuilder.durable(props.getDeliveredOrdersQueue()).build();
    }

    @Bean
    public Queue cancelledOrdersQueue() {
        return QueueBuilder.durable(props.getCancelledOrdersQueue()).build();
    }

    @Bean
    public Queue errorOrdersQueue() {
        return QueueBuilder.durable(props.getErrorOrdersQueue()).build();
    }

    @Bean
    public Binding bindNewOrders() {
        return BindingBuilder.bind(newOrdersQueue()).to(orderExchange()).with(props.getNewOrdersQueue());
    }

    @Bean
    public Binding bindDeliveredOrders() {
        return BindingBuilder.bind(deliveredOrdersQueue()).to(orderExchange()).with(props.getDeliveredOrdersQueue());
    }

    @Bean
    public Binding bindCancelledOrders() {
        return BindingBuilder.bind(cancelledOrdersQueue()).to(orderExchange()).with(props.getCancelledOrdersQueue());
    }

    @Bean
    public Binding bindErrorOrders() {
        return BindingBuilder.bind(errorOrdersQueue()).to(orderExchange()).with(props.getErrorOrdersQueue());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf,
                                                                               Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(converter);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
