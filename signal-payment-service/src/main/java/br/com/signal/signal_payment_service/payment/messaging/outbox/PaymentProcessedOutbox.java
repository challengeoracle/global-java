package br.com.signal.signal_payment_service.payment.messaging.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "TB_PAYMENT_PROCESSED_OUTBOX",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_PAYMENT_PROCESSED_OUTBOX_ORDER",
                        columnNames = {"ORDER_ID"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ORDER_ID", nullable = false)
    private UUID orderId;

    @Column(name = "EVENT_TYPE", nullable = false, length = 60)
    private String eventType;

    @Lob
    @Column(name = "PAYLOAD_JSON", nullable = false)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private PaymentProcessedOutboxStatus status;

    @Column(name = "ATTEMPTS", nullable = false)
    private Integer attempts;

    @Column(name = "NEXT_ATTEMPT_AT", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "LAST_ERROR", length = 255)
    private String lastError;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "SENT_AT")
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) status = PaymentProcessedOutboxStatus.PENDING;
        if (attempts == null) attempts = 0;
        if (nextAttemptAt == null) nextAttemptAt = now;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
