package br.com.signal.signal_sales_service.messaging;

import br.com.signal.signal_sales_service.config.RabbitMQConfig;
import br.com.signal.signal_sales_service.entity.SalesOrder;
import br.com.signal.signal_sales_service.messaging.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentRequested(SalesOrder order) {
        try {
            PaymentRequestedEvent event = PaymentRequestedEvent.builder()
                    .orderId(order.getId())
                    .localOrderId(order.getLocalOrderId())
                    .storeId(order.getStoreId())
                    .customerId(order.getCustomerId())
                    .sellerId(order.getSellerId())
                    .totalAmount(order.getTotalAmount())
                    .paymentStatus(order.getPaymentStatus().name())
                    .syncStatus(order.getSyncStatus().name())
                    .createdAt(order.getCreatedAt())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SALES_EXCHANGE,
                    RabbitMQConfig.PAYMENT_REQUESTED_ROUTING_KEY,
                    event
            );
        } catch (AmqpException ex) {
            log.warn(
                    "Payment event could not be published for order {}. The order was kept saved.",
                    order.getId(),
                    ex
            );
        }
    }
}