package br.com.signal.signal_sales_service.order.messaging.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentEventOutboxRepository extends JpaRepository<PaymentEventOutbox, UUID> {

    Optional<PaymentEventOutbox> findByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from PaymentEventOutbox event
            where event.id = :id
            """)
    Optional<PaymentEventOutbox> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select event
            from PaymentEventOutbox event
            where event.status <> br.com.signal.signal_sales_service.order.messaging.outbox.PaymentEventOutboxStatus.SENT
              and event.nextAttemptAt <= :referenceTime
            order by event.createdAt asc
            """)
    List<PaymentEventOutbox> findRetryable(@Param("referenceTime") LocalDateTime referenceTime);
}
