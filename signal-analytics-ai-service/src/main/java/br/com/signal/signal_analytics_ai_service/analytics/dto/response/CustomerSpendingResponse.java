package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSpendingResponse {

    private UUID customerId;
    private String customerName;

    private Integer totalPurchases;
    private BigDecimal totalSpent;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal rejectedAmount;

    private List<CustomerSpendingByStoreResponse> spendingByStore;
    private List<TopProductResponse> mostPurchasedProducts;

    private String message;
}