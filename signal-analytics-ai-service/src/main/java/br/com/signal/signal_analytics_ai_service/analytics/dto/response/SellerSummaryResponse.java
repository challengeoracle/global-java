package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerSummaryResponse {

    private UUID sellerId;
    private UUID storeId;
    private String storeName;

    private Integer totalSales;
    private Integer paidSales;
    private Integer rejectedPayments;
    private Integer pendingPayments;

    private BigDecimal totalSalesAmount;
    private BigDecimal paidSalesAmount;
    private BigDecimal rejectedSalesAmount;
    private BigDecimal pendingSalesAmount;

    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;

    private String topProductName;
    private Integer topProductQuantity;

    private String message;
}