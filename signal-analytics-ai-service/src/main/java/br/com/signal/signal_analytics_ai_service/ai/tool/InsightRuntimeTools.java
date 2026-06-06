package br.com.signal.signal_analytics_ai_service.ai.tool;

import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeRetrievalService;
import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeSnippet;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.CustomerSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.CustomerSpendingResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.SellerSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.TopProductResponse;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.stream.Collectors;

public class InsightRuntimeTools {

    private final String authorization;
    private final AuthUserResponse authUser;
    private final AnalyticsSummaryService analyticsSummaryService;
    private final KnowledgeRetrievalService knowledgeRetrievalService;

    public InsightRuntimeTools(
            String authorization,
            AuthUserResponse authUser,
            AnalyticsSummaryService analyticsSummaryService,
            KnowledgeRetrievalService knowledgeRetrievalService
    ) {
        this.authorization = authorization;
        this.authUser = authUser;
        this.analyticsSummaryService = analyticsSummaryService;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
    }

    @Tool(description = "Retorna um resumo operacional e financeiro agregado do usuario autenticado.")
    public AnalyticsSummaryResponse getMyAnalyticsSummary() {
        return analyticsSummaryService.getMySummary(authorization);
    }

    @Tool(description = "Retorna um resumo detalhado de vendedor ou cliente conforme o papel do usuario autenticado. Opcionalmente, o modelo pode enviar um texto curto de contexto adicional, que sera ignorado se vier vazio.")
    public Object getRoleSpecificSummary(
            @ToolParam(description = "Contexto adicional opcional sobre o usuario atual. Pode ser omitido.") String context
    ) {
        if (authUser.isSeller()) {
            SellerSummaryResponse summary = analyticsSummaryService.getSellerSummary(authorization);
            List<TopProductResponse> topProducts = analyticsSummaryService.getSellerTopProducts(authorization);
            return new SellerRuntimeSummary(summary, topProducts);
        }

        CustomerSummaryResponse summary = analyticsSummaryService.getCustomerSummary(authorization);
        CustomerSpendingResponse spending = analyticsSummaryService.getCustomerSpending(authorization);
        return new CustomerRuntimeSummary(summary, spending);
    }

    @Tool(description = "Retorna fatos objetivos sobre a compra mais recente do cliente autenticado.")
    public CustomerSummaryResponse getLastPurchaseFacts() {
        if (!authUser.isCustomer()) {
            throw new IllegalStateException("Somente clientes possuem compras para consulta.");
        }
        return analyticsSummaryService.getCustomerSummary(authorization);
    }

    @Tool(description = "Busca regras operacionais e trechos de conhecimento relevantes para responder a pergunta do usuario.")
    public String searchOperationalKnowledge(@ToolParam(description = "Pergunta ou topico a ser pesquisado.") String question) {
        List<KnowledgeSnippet> snippets = knowledgeRetrievalService.retrieve(authUser, question);
        return knowledgeRetrievalService.renderContext(snippets);
    }

    @Tool(description = "Lista as fontes de conhecimento disponiveis no runtime do OffPay.")
    public List<String> listKnowledgeSources() {
        return knowledgeRetrievalService.listAvailableSources().stream()
                .map(source -> source.id() + " - " + source.title())
                .collect(Collectors.toList());
    }

    public record SellerRuntimeSummary(
            SellerSummaryResponse summary,
            List<TopProductResponse> topProducts
    ) {
    }

    public record CustomerRuntimeSummary(
            CustomerSummaryResponse summary,
            CustomerSpendingResponse spending
    ) {
    }
}
