package br.com.signal.signal_payment_service.payment.entity;

import br.com.signal.signal_payment_service.payment.enums.PaymentTransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "TB_PAYMENT_TRANSACTIONS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_PAYMENT_TRANSACTIONS_ORDER",
                        columnNames = {"ORDER_ID"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ORDER_ID", nullable = false)
    private UUID orderId;

    @Column(name = "LOCAL_ORDER_ID", length = 100)
    private String localOrderId;

    @Column(name = "CUSTOMER_ID")
    private UUID customerId;

    @Column(name = "SELLER_ID")
    private UUID sellerId;

    @Column(name = "STORE_ID", nullable = false)
    private UUID storeId;

    @Column(name = "AMOUNT", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 30)
    private PaymentTransactionStatus status;

    @Column(name = "FAILURE_REASON", length = 255)
    private String failureReason;

    @Column(name = "GATEWAY_REFERENCE", length = 100)
    private String gatewayReference;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = PaymentTransactionStatus.PENDING;
        }
    }
}