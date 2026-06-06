package br.com.signal.signal_analytics_ai_service.analytics.controller;

import br.com.signal.signal_analytics_ai_service.analytics.hateoas.AnalyticsResourceAssembler;
import br.com.signal.signal_analytics_ai_service.analytics.hateoas.AnalyticsSummaryModelAssembler;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.*;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Resumo operacional, graficos e indicadores de uso do OffPay.")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsSummaryService analyticsSummaryService;
    private final AnalyticsSummaryModelAssembler analyticsSummaryModelAssembler;
    private final AnalyticsResourceAssembler analyticsResourceAssembler;

    @GetMapping("/me/summary")
    @Operation(summary = "Resumo geral do usuario", description = "Retorna o resumo consolidado do usuario autenticado.")
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
    @Operation(summary = "Resumo do vendedor", description = "Retorna indicadores especificos para o vendedor autenticado.")
    public SellerSummaryResponse getSellerSummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getSellerSummary(authorization);
    }

    @GetMapping("/seller/summary/resource")
    public EntityModel<SellerSummaryResponse> getSellerSummaryResource(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsResourceAssembler.toSellerSummaryModel(
                analyticsSummaryService.getSellerSummary(authorization)
        );
    }

    @GetMapping("/customer/summary")
    @Operation(summary = "Resumo do cliente", description = "Retorna indicadores especificos para o cliente autenticado.")
    public CustomerSummaryResponse getCustomerSummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getCustomerSummary(authorization);
    }

    @GetMapping("/customer/summary/resource")
    public EntityModel<CustomerSummaryResponse> getCustomerSummaryResource(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsResourceAssembler.toCustomerSummaryModel(
                analyticsSummaryService.getCustomerSummary(authorization)
        );
    }

    @GetMapping("/seller/top-products")
    @Operation(summary = "Top produtos do vendedor", description = "Retorna os produtos de melhor desempenho para o vendedor autenticado.")
    public List<TopProductResponse> getSellerTopProducts(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getSellerTopProducts(authorization);
    }

    @GetMapping("/seller/top-products/resource")
    public CollectionModel<EntityModel<TopProductResponse>> getSellerTopProductsResource(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsResourceAssembler.toTopProductsModel(
                analyticsSummaryService.getSellerTopProducts(authorization)
        );
    }

    @GetMapping("/customer/spending")
    public CustomerSpendingResponse getCustomerSpending(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getCustomerSpending(authorization);
    }

    @GetMapping("/customer/spending/resource")
    public EntityModel<CustomerSpendingResponse> getCustomerSpendingResource(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsResourceAssembler.toCustomerSpendingModel(
                analyticsSummaryService.getCustomerSpending(authorization)
        );
    }

    @GetMapping("/me/summary/period")
    public AnalyticsPeriodSummaryResponse getMyPeriodSummary(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "today") String period
    ) {
        return analyticsSummaryService.getMyPeriodSummary(authorization, period);
    }

    @GetMapping("/me/summary/period/resource")
    public EntityModel<AnalyticsPeriodSummaryResponse> getMyPeriodSummaryResource(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "today") String period
    ) {
        return analyticsResourceAssembler.toPeriodSummaryModel(
                analyticsSummaryService.getMyPeriodSummary(authorization, period),
                period
        );
    }

    @GetMapping("/me/chart")
    public AnalyticsChartResponse getMyChart(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "7") int days
    ) {
        return analyticsSummaryService.getMyChart(authorization, days);
    }

    @GetMapping("/me/chart/resource")
    public EntityModel<AnalyticsChartResponse> getMyChartResource(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "7") int days
    ) {
        return analyticsResourceAssembler.toChartModel(
                analyticsSummaryService.getMyChart(authorization, days),
                days
        );
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
