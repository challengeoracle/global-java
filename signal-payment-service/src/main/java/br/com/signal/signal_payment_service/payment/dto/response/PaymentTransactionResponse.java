package br.com.signal.signal_payment_service.payment.dto.response;

import br.com.signal.signal_payment_service.payment.enums.PaymentTransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionResponse {

    private UUID id;
    private UUID orderId;
    private String localOrderId;
    private UUID customerId;
    private UUID sellerId;
    private UUID storeId;
    private BigDecimal amount;
    private PaymentTransactionStatus status;
    private String failureReason;
    private String gatewayReference;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}