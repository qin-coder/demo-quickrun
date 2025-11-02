package com.xuwei.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    private final ApplicationProperties props;

    public RabbitMQConfig(ApplicationProperties props) {
        this.props = props;
        log.info("Initializing RabbitMQ Configuration");
    }

    @Bean
    public DirectExchange orderExchange() {
        String exchangeName = props.getOrderEventsExchange();
        log.info("Creating Exchange: {} (Direct, Durable)", exchangeName);
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue newOrdersQueue() {
        String queueName = props.getNewOrdersQueue();
        log.info("Creating Queue: {} (Durable)", queueName);
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue deliveredOrdersQueue() {
        String queueName = props.getDeliveredOrdersQueue();
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue cancelledOrdersQueue() {
        String queueName = props.getCancelledOrdersQueue();
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue errorOrdersQueue() {
        String queueName = props.getErrorOrdersQueue();
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding bindNewOrders() {
        String exchange = props.getOrderEventsExchange();
        String queue = props.getNewOrdersQueue();
        log.info("Binding Queue '{}' to Exchange '{}' with Routing Key '{}'", queue, exchange, queue);
        return BindingBuilder.bind(newOrdersQueue()).to(orderExchange()).with(queue);
    }

    @Bean
    public Binding bindDeliveredOrders() {
        String queue = props.getDeliveredOrdersQueue();
        return BindingBuilder.bind(deliveredOrdersQueue()).to(orderExchange()).with(queue);
    }

    @Bean
    public Binding bindCancelledOrders() {
        String queue = props.getCancelledOrdersQueue();
        return BindingBuilder.bind(cancelledOrdersQueue()).to(orderExchange()).with(queue);
    }

    @Bean
    public Binding bindErrorOrders() {
        String queue = props.getErrorOrdersQueue();
        return BindingBuilder.bind(errorOrdersQueue()).to(orderExchange()).with(queue);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        log.info("Configuring Jackson2JsonMessageConverter");
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        log.info("Creating RabbitTemplate with JSON message converter");
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message confirmed by broker: {}", correlationData);
            } else {
                log.error("Message rejected by broker: {}, cause: {}", correlationData, cause);
            }
        });

        template.setReturnsCallback(returned -> {
            log.error("Message returned: {}", returned);
        });

        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf,
                                                                               Jackson2JsonMessageConverter converter) {
        log.info("Configuring RabbitListener Container Factory");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(converter);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);

        factory.setMissingQueuesFatal(false);

        return factory;
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        log.info("Creating AmqpAdmin");
        return new RabbitAdmin(connectionFactory);
    }
}