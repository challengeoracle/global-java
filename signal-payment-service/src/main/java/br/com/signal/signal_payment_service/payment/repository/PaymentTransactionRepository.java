package br.com.signal.signal_payment_service.payment.repository;

import br.com.signal.signal_payment_service.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByOrderId(UUID orderId);

    List<PaymentTransaction> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    List<PaymentTransaction> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}