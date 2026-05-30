package br.com.signal.signal_payment_service.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    @Value("${offpay.rabbit.sales-exchange}")
    private String salesExchangeName;

    @Value("${offpay.rabbit.payment-exchange}")
    private String paymentExchangeName;

    @Value("${offpay.rabbit.payment-requested-queue}")
    private String paymentRequestedQueueName;

    @Value("${offpay.rabbit.payment-requested-routing-key}")
    private String paymentRequestedRoutingKey;

    @Bean
    public TopicExchange salesExchange() {
        return ExchangeBuilder
                .topicExchange(salesExchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange paymentExchange() {
        return ExchangeBuilder
                .topicExchange(paymentExchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue paymentRequestedQueue() {
        return QueueBuilder
                .durable(paymentRequestedQueueName)
                .build();
    }

    @Bean
    public Binding paymentRequestedBinding() {
        return BindingBuilder
                .bind(paymentRequestedQueue())
                .to(salesExchange())
                .with(paymentRequestedRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }
}