package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSummaryResponse {

    private UUID customerId;
    private String customerName;

    private Integer totalPurchases;
    private Integer paidPurchases;
    private Integer rejectedPayments;
    private Integer pendingPayments;

    private BigDecimal totalSpent;
    private BigDecimal paidAmount;
    private BigDecimal rejectedAmount;
    private BigDecimal pendingAmount;

    private BigDecimal walletBalance;

    private UUID favoriteStoreId;
    private String mostPurchasedProductName;
    private Integer mostPurchasedProductQuantity;

    private String message;
}