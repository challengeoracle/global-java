package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummaryResponse {

    private UUID userId;
    private String userName;
    private String role;

    private UUID storeId;
    private String storeName;

    private Integer totalOrders;
    private Integer paidOrders;
    private Integer rejectedPayments;
    private Integer pendingPayments;

    private BigDecimal totalAmount;
    private BigDecimal walletBalance;
    private BigDecimal walletPendingBalance;
    private BigDecimal personalWalletBalance;

    private String topProductName;
    private Integer topProductQuantity;

    private String message;
}