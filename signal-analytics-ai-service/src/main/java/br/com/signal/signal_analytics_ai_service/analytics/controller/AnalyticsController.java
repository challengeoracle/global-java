package br.com.signal.signal_analytics_ai_service.analytics.controller;

import br.com.signal.signal_analytics_ai_service.analytics.hateoas.AnalyticsSummaryModelAssembler;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.*;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsSummaryService analyticsSummaryService;
    private final AnalyticsSummaryModelAssembler analyticsSummaryModelAssembler;

    @GetMapping("/me/summary")
    public AnalyticsSummaryResponse getMySummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getMySummary(authorization);
    }

    @GetMapping("/me/summary/resource")
    public EntityModel<AnalyticsSummaryResponse> getMySummaryResource(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryModelAssembler.toModel(
                analyticsSummaryService.getMySummary(authorization)
        );
    }

    @GetMapping("/seller/summary")
    public SellerSummaryResponse getSellerSummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getSellerSummary(authorization);
    }

    @GetMapping("/customer/summary")
    public CustomerSummaryResponse getCustomerSummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getCustomerSummary(authorization);
    }

    @GetMapping("/seller/top-products")
    public List<TopProductResponse> getSellerTopProducts(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getSellerTopProducts(authorization);
    }

    @GetMapping("/customer/spending")
    public CustomerSpendingResponse getCustomerSpending(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getCustomerSpending(authorization);
    }

    @GetMapping("/me/summary/period")
    public AnalyticsPeriodSummaryResponse getMyPeriodSummary(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "today") String period
    ) {
        return analyticsSummaryService.getMyPeriodSummary(authorization, period);
    }

    @GetMapping("/me/chart")
    public AnalyticsChartResponse getMyChart(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "7") int days
    ) {
        return analyticsSummaryService.getMyChart(authorization, days);
    }

    @GetMapping("/seller/chart")
    public AnalyticsChartResponse getSellerChart(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "7") int days
    ) {
        return analyticsSummaryService.getSellerChart(authorization, days);
    }

    @GetMapping("/customer/chart")
    public AnalyticsChartResponse getCustomerChart(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "7") int days
    ) {
        return analyticsSummaryService.getCustomerChart(authorization, days);
    }
}
