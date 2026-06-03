package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsChartPointResponse {

    private LocalDate date;
    private Integer totalOrders;
    private Integer paidOrders;
    private Integer pendingOrders;
    private Integer rejectedOrders;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal rejectedAmount;
}
