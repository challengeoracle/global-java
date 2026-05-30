package br.com.signal.signal_payment_service.payment.service;

import br.com.signal.signal_payment_service.payment.dto.response.PaymentTransactionResponse;
import br.com.signal.signal_payment_service.payment.entity.PaymentTransaction;
import br.com.signal.signal_payment_service.payment.mapper.PaymentTransactionMapper;
import br.com.signal.signal_payment_service.payment.repository.PaymentTransactionRepository;
import br.com.signal.signal_payment_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_payment_service.shared.exception.ForbiddenException;
import br.com.signal.signal_payment_service.shared.exception.NotFoundException;
import br.com.signal.signal_payment_service.shared.service.AuthIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentTransactionQueryService {

    private final AuthIdentityService authIdentityService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionMapper paymentTransactionMapper;

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponse> findMyTransactions(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);

        if (authUser.isSeller()) {
            return paymentTransactionRepository.findByStoreIdOrderByCreatedAtDesc(authUser.getStoreId())
                    .stream()
                    .map(paymentTransactionMapper::toResponse)
                    .toList();
        }

        return paymentTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(authUser.getId())
                .stream()
                .map(paymentTransactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentTransactionResponse findByOrderId(String authorization, UUID orderId) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);

        PaymentTransaction transaction = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment transaction not found"));

        if (authUser.isSeller()) {
            if (!transaction.getStoreId().equals(authUser.getStoreId())) {
                throw new ForbiddenException("You cannot access this payment transaction");
            }

            return paymentTransactionMapper.toResponse(transaction);
        }

        if (!transaction.getCustomerId().equals(authUser.getId())) {
            throw new ForbiddenException("You cannot access this payment transaction");
        }

        return paymentTransactionMapper.toResponse(transaction);
    }
}