package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private String favoriteStoreName;
    private UUID lastPurchaseStoreId;
    private String lastPurchaseStoreName;
    private String lastPurchaseOrderId;
    private BigDecimal lastPurchaseAmount;
    private String lastPurchasePaymentStatus;
    private LocalDateTime lastPurchaseAt;
    private List<String> lastPurchaseProductNames;
    private String mostPurchasedProductName;
    private Integer mostPurchasedProductQuantity;

    private String message;
}
