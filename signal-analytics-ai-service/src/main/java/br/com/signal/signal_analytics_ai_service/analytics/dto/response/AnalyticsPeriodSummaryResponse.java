package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsPeriodSummaryResponse {

    private String role;
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalOrders;
    private Integer paidOrders;
    private Integer pendingPayments;
    private Integer rejectedPayments;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal rejectedAmount;
    private BigDecimal averageTicket;
    private String topProductName;
    private Integer topProductQuantity;
    private String message;
}
