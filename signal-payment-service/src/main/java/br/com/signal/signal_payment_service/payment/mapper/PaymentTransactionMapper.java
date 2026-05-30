package br.com.signal.signal_payment_service.payment.mapper;

import br.com.signal.signal_payment_service.payment.dto.response.PaymentTransactionResponse;
import br.com.signal.signal_payment_service.payment.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {

    public PaymentTransactionResponse toResponse(PaymentTransaction transaction) {
        return PaymentTransactionResponse.builder()
                .id(transaction.getId())
                .orderId(transaction.getOrderId())
                .localOrderId(transaction.getLocalOrderId())
                .customerId(transaction.getCustomerId())
                .sellerId(transaction.getSellerId())
                .storeId(transaction.getStoreId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .failureReason(transaction.getFailureReason())
                .gatewayReference(transaction.getGatewayReference())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }
}