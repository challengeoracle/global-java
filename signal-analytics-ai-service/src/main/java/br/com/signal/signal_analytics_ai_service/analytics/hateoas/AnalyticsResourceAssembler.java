package br.com.signal.signal_analytics_ai_service.analytics.hateoas;

import br.com.signal.signal_analytics_ai_service.ai.controller.InsightAiController;
import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import br.com.signal.signal_analytics_ai_service.analytics.controller.AnalyticsController;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AnalyticsResourceAssembler {

    public EntityModel<SellerSummaryResponse> toSellerSummaryModel(SellerSummaryResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(AnalyticsController.class).getSellerSummary(null)).withSelfRel(),
                linkTo(methodOn(AnalyticsController.class).getSellerSummaryResource(null)).withRel("resource"),
                linkTo(methodOn(AnalyticsController.class).getSellerTopProducts(null)).withRel("top-products"),
                linkTo(methodOn(AnalyticsController.class).getSellerChart(null, 7)).withRel("chart"),
                linkTo(methodOn(InsightAiController.class).ask(null, new InsightAskRequest())).withRel("ask-ai")
        );
    }

    public EntityModel<CustomerSummaryResponse> toCustomerSummaryModel(CustomerSummaryResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(AnalyticsController.class).getCustomerSummary(null)).withSelfRel(),
                linkTo(methodOn(AnalyticsController.class).getCustomerSummaryResource(null)).withRel("resource"),
                linkTo(methodOn(AnalyticsController.class).getCustomerSpending(null)).withRel("spending"),
                linkTo(methodOn(AnalyticsController.class).getCustomerChart(null, 7)).withRel("chart"),
                linkTo(methodOn(InsightAiController.class).ask(null, new InsightAskRequest())).withRel("ask-ai")
        );
    }

    public CollectionModel<EntityModel<TopProductResponse>> toTopProductsModel(List<TopProductResponse> items) {
        return CollectionModel.of(
                items.stream().map(EntityModel::of).toList(),
                linkTo(methodOn(AnalyticsController.class).getSellerTopProducts(null)).withSelfRel(),
                linkTo(methodOn(AnalyticsController.class).getSellerTopProductsResource(null)).withRel("resource"),
                linkTo(methodOn(AnalyticsController.class).getSellerSummary(null)).withRel("seller-summary")
        );
    }

    public EntityModel<CustomerSpendingResponse> toCustomerSpendingModel(CustomerSpendingResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(AnalyticsController.class).getCustomerSpending(null)).withSelfRel(),
                linkTo(methodOn(AnalyticsController.class).getCustomerSpendingResource(null)).withRel("resource"),
                linkTo(methodOn(AnalyticsController.class).getCustomerSummary(null)).withRel("customer-summary"),
                linkTo(methodOn(InsightAiController.class).ask(null, new InsightAskRequest())).withRel("ask-ai")
        );
    }

    public EntityModel<AnalyticsPeriodSummaryResponse> toPeriodSummaryModel(AnalyticsPeriodSummaryResponse response, String period) {
        return EntityModel.of(
                response,
                linkTo(methodOn(AnalyticsController.class).getMyPeriodSummary(null, period)).withSelfRel(),
                linkTo(methodOn(AnalyticsController.class).getMyPeriodSummaryResource(null, period)).withRel("resource"),
                linkTo(methodOn(AnalyticsController.class).getMyChart(null, 7)).withRel("chart")
        );
    }

    public EntityModel<AnalyticsChartResponse> toChartModel(AnalyticsChartResponse response, int days) {
        return EntityModel.of(
                response,
                linkTo(methodOn(AnalyticsController.class).getMyChart(null, days)).withSelfRel(),
                linkTo(methodOn(AnalyticsController.class).getMyChartResource(null, days)).withRel("resource"),
                linkTo(methodOn(AnalyticsController.class).getMySummary(null)).withRel("summary")
        );
    }
}
