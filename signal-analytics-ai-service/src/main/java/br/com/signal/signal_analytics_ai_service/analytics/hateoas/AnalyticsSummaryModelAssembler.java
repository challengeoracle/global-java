package br.com.signal.signal_analytics_ai_service.analytics.hateoas;

import br.com.signal.signal_analytics_ai_service.analytics.controller.AnalyticsController;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsSummaryResponse;
import br.com.signal.signal_analytics_ai_service.ai.controller.InsightAiController;
import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AnalyticsSummaryModelAssembler {

    public EntityModel<AnalyticsSummaryResponse> toModel(AnalyticsSummaryResponse summaryResponse) {
        return EntityModel.of(
                summaryResponse,
                linkTo(methodOn(AnalyticsController.class).getMySummary(null)).withSelfRel(),
                linkTo(methodOn(AnalyticsController.class).getMySummaryResource(null)).withRel("resource"),
                linkTo(methodOn(AnalyticsController.class).getMyPeriodSummary(null, "today")).withRel("period-summary"),
                linkTo(methodOn(AnalyticsController.class).getMyChart(null, 7)).withRel("chart"),
                linkTo(methodOn(InsightAiController.class).ask(null, new InsightAskRequest())).withRel("ask-ai")
        );
    }
}
