package br.com.signal.signal_sales_service.messaging.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestedEvent {

    private UUID orderId;
    private String localOrderId;
    private UUID storeId;
    private UUID customerId;
    private UUID sellerId;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String syncStatus;
    private LocalDateTime createdAt;
}