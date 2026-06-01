package br.com.signal.signal_payment_service.payment.messaging.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentProcessedOutboxRepository extends JpaRepository<PaymentProcessedOutbox, UUID> {

    Optional<PaymentProcessedOutbox> findByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from PaymentProcessedOutbox event where event.id = :id")
    Optional<PaymentProcessedOutbox> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select event
            from PaymentProcessedOutbox event
            where event.status <> br.com.signal.signal_payment_service.payment.messaging.outbox.PaymentProcessedOutboxStatus.SENT
              and event.nextAttemptAt <= :referenceTime
            order by event.createdAt asc
            """)
    List<PaymentProcessedOutbox> findRetryable(@Param("referenceTime") LocalDateTime referenceTime);
}
