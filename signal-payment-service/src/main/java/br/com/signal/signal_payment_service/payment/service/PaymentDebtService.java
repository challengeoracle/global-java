package br.com.signal.signal_payment_service.payment.service;

import br.com.signal.signal_payment_service.payment.dto.response.PaymentTransactionResponse;
import br.com.signal.signal_payment_service.payment.entity.PaymentTransaction;
import br.com.signal.signal_payment_service.payment.enums.PaymentTransactionStatus;
import br.com.signal.signal_payment_service.payment.mapper.PaymentTransactionMapper;
import br.com.signal.signal_payment_service.payment.repository.PaymentTransactionRepository;
import br.com.signal.signal_payment_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_payment_service.shared.exception.BadRequestException;
import br.com.signal.signal_payment_service.shared.exception.ForbiddenException;
import br.com.signal.signal_payment_service.shared.exception.NotFoundException;
import br.com.signal.signal_payment_service.shared.service.AuthIdentityService;
import br.com.signal.signal_payment_service.wallet.entity.Wallet;
import br.com.signal.signal_payment_service.wallet.entity.WalletTransaction;
import br.com.signal.signal_payment_service.wallet.enums.WalletTransactionType;
import br.com.signal.signal_payment_service.wallet.repository.WalletRepository;
import br.com.signal.signal_payment_service.wallet.repository.WalletTransactionRepository;
import br.com.signal.signal_payment_service.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentDebtService {

    private final AuthIdentityService authIdentityService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public PaymentTransactionResponse settleCreditDebt(String authorization, UUID orderId) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);

        if (!authUser.isCustomer()) {
            throw new ForbiddenException("Only customers can settle payment debt");
        }

        PaymentTransaction transaction = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment transaction not found"));

        if (!authUser.getId().equals(transaction.getCustomerId())) {
            throw new ForbiddenException("You cannot settle this payment debt");
        }

        if (transaction.getStatus() != PaymentTransactionStatus.APPROVED) {
            throw new BadRequestException("Only approved payments can have debt settled");
        }

        BigDecimal debtAmount = transaction.getCreditDebtAmount() == null ? BigDecimal.ZERO : transaction.getCreditDebtAmount();

        if (debtAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("This order does not have pending payment debt");
        }

        Wallet customerWallet = walletService.getOrCreateCustomerWalletForUpdate(authUser.getId());

        if (customerWallet.getBalance().compareTo(debtAmount) < 0) {
            throw new BadRequestException("Insufficient wallet balance to settle this debt");
        }

        LocalDateTime now = LocalDateTime.now();
        customerWallet.setBalance(customerWallet.getBalance().subtract(debtAmount));
        customerWallet.setUpdatedAt(now);
        Wallet savedWallet = walletRepository.save(customerWallet);

        walletTransactionRepository.save(
                WalletTransaction.builder()
                        .wallet(savedWallet)
                        .type(WalletTransactionType.DEBT_PAYMENT)
                        .amount(debtAmount)
                        .description("Quita��o do cr�dito devedor do pedido " + transaction.getLocalOrderId())
                        .referenceId(transaction.getOrderId().toString())
                        .createdAt(now)
                        .build()
        );

        transaction.setCreditDebtAmount(BigDecimal.ZERO);
        transaction.setCreditDebtSettledAt(now);

        return paymentTransactionMapper.toResponse(paymentTransactionRepository.save(transaction));
    }
}
