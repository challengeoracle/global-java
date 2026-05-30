package br.com.signal.signal_sales_service.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    public static final String SALES_EXCHANGE = "offpay.sales.exchange";
    public static final String PAYMENT_EXCHANGE = "offpay.payment.exchange";

    public static final String PAYMENT_REQUESTED_ROUTING_KEY = "payment.requested";
    public static final String PAYMENT_PROCESSED_ROUTING_KEY = "payment.processed";

    public static final String SALES_PAYMENT_PROCESSED_QUEUE = "offpay.sales.payment.processed.queue";

    @Bean
    public DirectExchange salesExchange() {
        return new DirectExchange(SALES_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue salesPaymentProcessedQueue() {
        return new Queue(SALES_PAYMENT_PROCESSED_QUEUE, true);
    }

    @Bean
    public Binding salesPaymentProcessedBinding() {
        return BindingBuilder
                .bind(salesPaymentProcessedQueue())
                .to(paymentExchange())
                .with(PAYMENT_PROCESSED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);

        return rabbitTemplate;
    }
}