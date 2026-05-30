package br.com.signal.signal_payment_service.payment.service;

import br.com.signal.signal_payment_service.payment.entity.PaymentTransaction;
import br.com.signal.signal_payment_service.payment.enums.PaymentTransactionStatus;
import br.com.signal.signal_payment_service.payment.messaging.PaymentProcessedPublisher;
import br.com.signal.signal_payment_service.payment.messaging.event.PaymentProcessedEvent;
import br.com.signal.signal_payment_service.payment.messaging.event.PaymentRequestedEvent;
import br.com.signal.signal_payment_service.payment.repository.PaymentTransactionRepository;
import br.com.signal.signal_payment_service.wallet.entity.Wallet;
import br.com.signal.signal_payment_service.wallet.entity.WalletTransaction;
import br.com.signal.signal_payment_service.wallet.enums.WalletTransactionType;
import br.com.signal.signal_payment_service.wallet.repository.WalletTransactionRepository;
import br.com.signal.signal_payment_service.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;
    private final PaymentProcessedPublisher paymentProcessedPublisher;

    @Transactional
    public void process(PaymentRequestedEvent event) {
        log.info("Processing payment for order {}", event.orderId());

        paymentTransactionRepository.findByOrderId(event.orderId())
                .ifPresentOrElse(
                        existingTransaction -> publishExistingResult(event, existingTransaction),
                        () -> processNewPayment(event)
                );
    }

    private void processNewPayment(PaymentRequestedEvent event) {
        LocalDateTime now = LocalDateTime.now();

        if (event.orderId() == null) {
            log.warn("Payment rejected: orderId is null");
            return;
        }

        if (event.storeId() == null) {
            PaymentTransaction transaction = createRejectedTransaction(
                    event,
                    "Store not found",
                    now
            );

            paymentProcessedPublisher.publish(toProcessedEvent(transaction));
            return;
        }

        if (event.customerId() == null) {
            PaymentTransaction transaction = createRejectedTransaction(
                    event,
                    "Customer not found",
                    now
            );

            paymentProcessedPublisher.publish(toProcessedEvent(transaction));
            return;
        }

        if (event.totalAmount() == null || event.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            PaymentTransaction transaction = createRejectedTransaction(
                    event,
                    "Invalid payment amount",
                    now
            );

            paymentProcessedPublisher.publish(toProcessedEvent(transaction));
            return;
        }

        Wallet customerWallet = walletService.getOrCreateCustomerWallet(event.customerId());

        if (customerWallet.getBalance().compareTo(event.totalAmount()) < 0) {
            PaymentTransaction transaction = createRejectedTransaction(
                    event,
                    "Insufficient wallet balance",
                    now
            );

            paymentProcessedPublisher.publish(toProcessedEvent(transaction));
            return;
        }

        Wallet storeWallet = walletService.getOrCreateStoreWallet(event.storeId());

        customerWallet.setBalance(customerWallet.getBalance().subtract(event.totalAmount()));
        storeWallet.setPendingBalance(storeWallet.getPendingBalance().add(event.totalAmount()));

        WalletTransaction customerDebit = WalletTransaction.builder()
                .wallet(customerWallet)
                .type(WalletTransactionType.PAYMENT_DEBIT)
                .amount(event.totalAmount())
                .description("Pagamento do pedido " + event.localOrderId())
                .referenceId(event.orderId().toString())
                .createdAt(now)
                .build();

        WalletTransaction storeCredit = WalletTransaction.builder()
                .wallet(storeWallet)
                .type(WalletTransactionType.PAYMENT_CREDIT)
                .amount(event.totalAmount())
                .description("Crédito do pedido " + event.localOrderId())
                .referenceId(event.orderId().toString())
                .createdAt(now)
                .build();

        walletTransactionRepository.save(customerDebit);
        walletTransactionRepository.save(storeCredit);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderId(event.orderId())
                .localOrderId(event.localOrderId())
                .customerId(event.customerId())
                .sellerId(event.sellerId())
                .storeId(event.storeId())
                .amount(event.totalAmount())
                .status(PaymentTransactionStatus.APPROVED)
                .failureReason(null)
                .gatewayReference("OFFPAY-" + UUID.randomUUID())
                .createdAt(now)
                .processedAt(now)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        paymentProcessedPublisher.publish(toProcessedEvent(savedTransaction));

        log.info("Payment approved for order {}", event.orderId());
    }

    private PaymentTransaction createRejectedTransaction(
            PaymentRequestedEvent event,
            String reason,
            LocalDateTime processedAt
    ) {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderId(event.orderId())
                .localOrderId(event.localOrderId())
                .customerId(event.customerId())
                .sellerId(event.sellerId())
                .storeId(event.storeId())
                .amount(event.totalAmount() == null ? BigDecimal.ZERO : event.totalAmount())
                .status(PaymentTransactionStatus.REJECTED)
                .failureReason(reason)
                .gatewayReference("OFFPAY-" + UUID.randomUUID())
                .createdAt(processedAt)
                .processedAt(processedAt)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        log.warn("Payment rejected for order {}: {}", event.orderId(), reason);

        return savedTransaction;
    }

    private void publishExistingResult(PaymentRequestedEvent event, PaymentTransaction existingTransaction) {
        log.info("Payment already processed for order {}", event.orderId());
        paymentProcessedPublisher.publish(toProcessedEvent(existingTransaction));
    }

    private PaymentProcessedEvent toProcessedEvent(PaymentTransaction transaction) {
        return new PaymentProcessedEvent(
                transaction.getOrderId(),
                transaction.getLocalOrderId(),
                transaction.getId(),
                transaction.getStoreId(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getFailureReason(),
                transaction.getProcessedAt()
        );
    }
}