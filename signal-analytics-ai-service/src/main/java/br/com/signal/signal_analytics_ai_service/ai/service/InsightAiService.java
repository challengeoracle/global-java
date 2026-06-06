package br.com.signal.signal_analytics_ai_service.ai.service;

import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import br.com.signal.signal_analytics_ai_service.ai.dto.response.InsightAskResponse;
import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeRetrievalService;
import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeSnippet;
import br.com.signal.signal_analytics_ai_service.ai.tool.InsightRuntimeTools;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsPeriodSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.CustomerSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_analytics_ai_service.shared.exception.TooManyRequestsException;
import br.com.signal.signal_analytics_ai_service.shared.service.AuthIdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InsightAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final AnalyticsSummaryService analyticsSummaryService;
    private final AuthIdentityService authIdentityService;
    private final KnowledgeRetrievalService knowledgeRetrievalService;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    public InsightAskResponse ask(String authorization, InsightAskRequest request) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        String sanitizedQuestion = sanitizeQuestion(request.getQuestion());
        QuestionScope questionScope = analyzeQuestion(sanitizedQuestion);

        if (questionScope.greetingOnly()) {
            return buildStaticResponse(buildGreetingReply(authUser), "greeting_scope");
        }

        AnalyticsSummaryResponse summary = analyticsSummaryService.getMySummary(authorization);
        List<KnowledgeSnippet> snippets = knowledgeRetrievalService.retrieve(authUser, request.getQuestion());
        AnalyticsPeriodSummaryResponse periodSummary = questionScope.period() == null
                ? null
                : analyticsSummaryService.getMyPeriodSummary(authorization, questionScope.period());
        CustomerSummaryResponse customerSummary = authUser.isCustomer()
                ? analyticsSummaryService.getCustomerSummary(authorization)
                : null;

        String scopedReply = buildScopedReply(authUser, questionScope, periodSummary, customerSummary);
        if (scopedReply != null) {
            return buildStaticResponse(scopedReply, "period_scope");
        }

        String knowledgeContext = knowledgeRetrievalService.renderContext(snippets);
        String summaryJson = toJson(buildPromptSummary(authorization, authUser, summary, periodSummary, questionScope, customerSummary));
        InsightRuntimeTools runtimeTools = new InsightRuntimeTools(
                authorization,
                authUser,
                analyticsSummaryService,
                knowledgeRetrievalService
        );
        String answer;

        try {
            answer = chatClient.prompt()
                    .system("""
                            Voce e o OffPay Insights.
                            Responda com base somente nos dados do usuario autenticado e nas regras recuperadas.
                            Nao invente valores, datas, lojas, pedidos ou status.
                            Responda em portugues do Brasil.
                            Seja objetivo e use no maximo 4 frases.
                            Nunca mencione prompt, PDF, retrieval, ferramentas, MCP ou detalhes tecnicos.

                            Trechos recuperados por retrieval:
                            %s
                            """.formatted(knowledgeContext))
                    .user("""
                            Usuario: %s
                            Papel: %s
                            Loja: %s
                            Pergunta: %s
                            Resumo operacional compacto: %s
                            """.formatted(
                            defaultValue(authUser.getName()),
                            defaultValue(authUser.getRole()),
                            defaultValue(authUser.getStoreName()),
                            sanitizedQuestion,
                            summaryJson
                    ))
                    .toolCallbacks(ToolCallbacks.from(runtimeTools))
                    .call()
                    .content();
        } catch (Exception ex) {
            throw translateAiException(ex);
        }

        return InsightAskResponse.builder()
                .answer(answer)
                .source("pdf_rag_compact_context")
                .model(model)
                .sources(snippets.stream().map(snippet -> snippet.source() + ":" + snippet.title()).toList())
                .capabilities(List.of("RAG", "TOOLING", "MCP"))
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private Map<String, Object> buildPromptSummary(
            String authorization,
            AuthUserResponse authUser,
            AnalyticsSummaryResponse summary,
            AnalyticsPeriodSummaryResponse periodSummary,
            QuestionScope questionScope,
            CustomerSummaryResponse customerSummary
    ) {
        Map<String, Object> promptSummary = new LinkedHashMap<>();
        promptSummary.put("userName", summary.getUserName());
        promptSummary.put("role", summary.getRole());
        promptSummary.put("storeName", summary.getStoreName());
        promptSummary.put("totalOrders", summary.getTotalOrders());
        promptSummary.put("paidOrders", summary.getPaidOrders());
        promptSummary.put("pendingPayments", summary.getPendingPayments());
        promptSummary.put("rejectedPayments", summary.getRejectedPayments());
        promptSummary.put("totalAmount", scaleMoney(summary.getTotalAmount()));
        promptSummary.put("walletBalance", scaleMoney(summary.getWalletBalance()));
        promptSummary.put("walletPendingBalance", scaleMoney(summary.getWalletPendingBalance()));
        promptSummary.put("personalWalletBalance", scaleMoney(summary.getPersonalWalletBalance()));
        promptSummary.put("topProductName", summary.getTopProductName());
        promptSummary.put("topProductQuantity", summary.getTopProductQuantity());
        promptSummary.put("questionScope", Map.of(
                "greetingOnly", questionScope.greetingOnly(),
                "period", defaultValue(questionScope.period()),
                "intent", defaultValue(questionScope.intent())
        ));

        if (periodSummary != null) {
            Map<String, Object> periodData = new LinkedHashMap<>();
            periodData.put("period", periodSummary.getPeriod());
            periodData.put("startDate", String.valueOf(periodSummary.getStartDate()));
            periodData.put("endDate", String.valueOf(periodSummary.getEndDate()));
            periodData.put("totalOrders", periodSummary.getTotalOrders());
            periodData.put("paidOrders", periodSummary.getPaidOrders());
            periodData.put("pendingPayments", periodSummary.getPendingPayments());
            periodData.put("rejectedPayments", periodSummary.getRejectedPayments());
            periodData.put("totalAmount", scaleMoney(periodSummary.getTotalAmount()));
            periodData.put("paidAmount", scaleMoney(periodSummary.getPaidAmount()));
            periodData.put("pendingAmount", scaleMoney(periodSummary.getPendingAmount()));
            periodData.put("rejectedAmount", scaleMoney(periodSummary.getRejectedAmount()));
            periodData.put("averageTicket", scaleMoney(periodSummary.getAverageTicket()));
            periodData.put("topProductName", defaultValue(periodSummary.getTopProductName()));
            periodData.put("topProductQuantity", defaultNumber(periodSummary.getTopProductQuantity()));
            promptSummary.put("periodSummary", periodData);
        }

        if (authUser.isSeller()) {
            var sellerSummary = analyticsSummaryService.getSellerSummary(authorization);
            promptSummary.put("sellerSummary", Map.of(
                    "totalSales", sellerSummary.getTotalSales(),
                    "paidSales", sellerSummary.getPaidSales(),
                    "pendingPayments", sellerSummary.getPendingPayments(),
                    "rejectedPayments", sellerSummary.getRejectedPayments(),
                    "totalSalesAmount", scaleMoney(sellerSummary.getTotalSalesAmount()),
                    "availableBalance", scaleMoney(sellerSummary.getAvailableBalance()),
                    "pendingBalance", scaleMoney(sellerSummary.getPendingBalance()),
                    "topProductName", defaultValue(sellerSummary.getTopProductName()),
                    "topProductQuantity", defaultNumber(sellerSummary.getTopProductQuantity())
            ));
            promptSummary.put("sellerTopProducts", analyticsSummaryService.getSellerTopProducts(authorization).stream().limit(3).map(item -> Map.of(
                    "productName", defaultValue(item.getProductName()),
                    "quantitySold", defaultNumber(item.getQuantitySold()),
                    "totalAmount", scaleMoney(item.getTotalAmount())
            )).toList());
        } else {
            customerSummary = customerSummary == null
                    ? analyticsSummaryService.getCustomerSummary(authorization)
                    : customerSummary;
            var customerSpending = analyticsSummaryService.getCustomerSpending(authorization);
            Map<String, Object> customerSummaryData = new LinkedHashMap<>();
            customerSummaryData.put("totalPurchases", customerSummary.getTotalPurchases());
            customerSummaryData.put("paidPurchases", customerSummary.getPaidPurchases());
            customerSummaryData.put("pendingPayments", customerSummary.getPendingPayments());
            customerSummaryData.put("rejectedPayments", customerSummary.getRejectedPayments());
            customerSummaryData.put("totalSpent", scaleMoney(customerSummary.getTotalSpent()));
            customerSummaryData.put("walletBalance", scaleMoney(customerSummary.getWalletBalance()));
            customerSummaryData.put("favoriteStoreId", customerSummary.getFavoriteStoreId() == null ? "Nao informado" : customerSummary.getFavoriteStoreId().toString());
            customerSummaryData.put("favoriteStoreName", defaultValue(customerSummary.getFavoriteStoreName()));
            customerSummaryData.put("lastPurchaseStoreId", customerSummary.getLastPurchaseStoreId() == null ? "Nao informado" : customerSummary.getLastPurchaseStoreId().toString());
            customerSummaryData.put("lastPurchaseStoreName", defaultValue(customerSummary.getLastPurchaseStoreName()));
            customerSummaryData.put("lastPurchaseOrderId", defaultValue(customerSummary.getLastPurchaseOrderId()));
            customerSummaryData.put("lastPurchaseAmount", scaleMoney(customerSummary.getLastPurchaseAmount()));
            customerSummaryData.put("lastPurchasePaymentStatus", defaultValue(customerSummary.getLastPurchasePaymentStatus()));
            customerSummaryData.put("lastPurchaseAt", String.valueOf(customerSummary.getLastPurchaseAt()));
            customerSummaryData.put("lastPurchaseProductNames", customerSummary.getLastPurchaseProductNames() == null ? List.of() : customerSummary.getLastPurchaseProductNames());
            customerSummaryData.put("mostPurchasedProductName", defaultValue(customerSummary.getMostPurchasedProductName()));
            customerSummaryData.put("mostPurchasedProductQuantity", defaultNumber(customerSummary.getMostPurchasedProductQuantity()));
            promptSummary.put("customerSummary", customerSummaryData);

            Map<String, Object> customerSpendingData = new LinkedHashMap<>();
            customerSpendingData.put("totalPurchases", customerSpending.getTotalPurchases());
            customerSpendingData.put("totalSpent", scaleMoney(customerSpending.getTotalSpent()));
            customerSpendingData.put("paidAmount", scaleMoney(customerSpending.getPaidAmount()));
            customerSpendingData.put("pendingAmount", scaleMoney(customerSpending.getPendingAmount()));
            customerSpendingData.put("rejectedAmount", scaleMoney(customerSpending.getRejectedAmount()));
            customerSpendingData.put("recentPurchases", customerSpending.getRecentPurchases() == null ? List.of() : customerSpending.getRecentPurchases().stream().limit(3).map(item -> Map.of(
                    "storeId", item.getStoreId() == null ? "Nao informado" : item.getStoreId().toString(),
                    "storeName", defaultValue(item.getStoreName()),
                    "orderId", item.getOrderId() == null ? "Nao informado" : item.getOrderId().toString(),
                    "localOrderId", defaultValue(item.getLocalOrderId()),
                    "purchasedAt", String.valueOf(item.getPurchasedAt()),
                    "totalAmount", scaleMoney(item.getTotalAmount()),
                    "paymentStatus", defaultValue(item.getPaymentStatus()),
                    "productNames", item.getProductNames() == null ? List.of() : item.getProductNames()
            )).toList());
            promptSummary.put("customerSpending", customerSpendingData);
        }

        return promptSummary;
    }

    private InsightAskResponse buildStaticResponse(String answer, String source) {
        return InsightAskResponse.builder()
                .answer(answer)
                .source(source)
                .model(model)
                .sources(List.of())
                .capabilities(List.of("RAG", "TOOLING", "MCP"))
                .build();
    }

    private String buildGreetingReply(AuthUserResponse authUser) {
        if (authUser.isSeller()) {
            return "Oi! Posso te ajudar com vendas, saldo, pagamentos pendentes, produto mais vendido ou desempenho por periodo da sua loja.";
        }
        return "Oi! Posso te ajudar com gastos, saldo da carteira, compras pendentes, lojas em que voce mais comprou e desempenho por periodo.";
    }

    private String buildScopedReply(
            AuthUserResponse authUser,
            QuestionScope questionScope,
            AnalyticsPeriodSummaryResponse periodSummary,
            CustomerSummaryResponse customerSummary
    ) {
        if (authUser.isCustomer() && customerSummary != null && questionScope.intent() != null) {
            switch (questionScope.intent()) {
                case "last_store" -> {
                    return buildLastStoreReply(customerSummary);
                }
                case "last_purchase" -> {
                    return buildLastPurchaseReply(customerSummary);
                }
                default -> {
                }
            }
        }

        if (periodSummary == null || questionScope.intent() == null) {
            return null;
        }

        String periodLabel = periodLabel(questionScope.period());

        return switch (questionScope.intent()) {
            case "spent_amount" -> authUser.isCustomer()
                    ? "Voce gastou R$ " + scaleMoney(periodSummary.getTotalAmount()) + " " + periodLabel + "."
                    : "Sua loja vendeu R$ " + scaleMoney(periodSummary.getTotalAmount()) + " " + periodLabel + ".";
            case "paid_amount" -> authUser.isCustomer()
                    ? "Dos seus pedidos, R$ " + scaleMoney(periodSummary.getPaidAmount()) + " foram pagos " + periodLabel + "."
                    : "Sua loja recebeu R$ " + scaleMoney(periodSummary.getPaidAmount()) + " em pedidos pagos " + periodLabel + ".";
            case "pending_amount" -> "O valor pendente " + periodLabel + " e de R$ " + scaleMoney(periodSummary.getPendingAmount()) + ".";
            case "rejected_amount" -> "O valor rejeitado " + periodLabel + " e de R$ " + scaleMoney(periodSummary.getRejectedAmount()) + ".";
            case "orders_count" -> authUser.isCustomer()
                    ? "Voce fez " + defaultNumber(periodSummary.getTotalOrders()) + " compra(s) " + periodLabel + "."
                    : "Sua loja registrou " + defaultNumber(periodSummary.getTotalOrders()) + " venda(s) " + periodLabel + ".";
            case "top_product" -> "O produto com mais recorrencia " + periodLabel + " foi " + defaultValue(periodSummary.getTopProductName())
                    + ", com " + defaultNumber(periodSummary.getTopProductQuantity()) + " unidade(s).";
            default -> null;
        };
    }

    private String periodLabel(String period) {
        if (period == null) {
            return "no periodo solicitado";
        }

        return switch (period.toLowerCase(Locale.ROOT)) {
            case "today", "hoje" -> "hoje";
            case "yesterday", "ontem" -> "ontem";
            case "week", "this_week", "semana", "esta_semana", "last_7_days", "ultimos_7_dias" -> "nos ultimos 7 dias";
            case "month", "this_month", "mes", "este_mes" -> "neste mes";
            default -> "no periodo solicitado";
        };
    }

    private QuestionScope analyzeQuestion(String question) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT).trim();
        if (normalized.isBlank()) {
            return QuestionScope.defaultScope();
        }

        Set<String> greetings = Set.of("oi", "ola", "olá", "e ai", "e aí", "bom dia", "boa tarde", "boa noite");
        boolean greetingOnly = greetings.contains(normalized);
        String period = detectPeriod(normalized);
        String intent = detectIntent(normalized);

        return new QuestionScope(greetingOnly, period, intent);
    }

    private String detectPeriod(String question) {
        if (containsAny(
                question,
                "ultimos 7 dias",
                "últimos 7 dias",
                "7 dias",
                "ultima semana",
                "última semana"
        )) {
            return "last_7_days";
        }
        if (question.contains("hoje")) {
            return "today";
        }
        if (question.contains("ontem")) {
            return "yesterday";
        }
        if (question.contains("semana")) {
            return "week";
        }
        if (question.contains("mes") || question.contains("mês")) {
            return "month";
        }
        return null;
    }

    private String detectIntent(String question) {
        if (containsAny(question, "ultima loja", "última loja", "loja que eu comprei por ultimo", "loja que eu comprei por último")) {
            return "last_store";
        }
        if (containsAny(question, "ultima compra", "última compra", "ultimo pedido", "último pedido", "comprei por ultimo", "comprei por último")) {
            return "last_purchase";
        }
        if (containsAny(question, "gastei", "gasto", "vendi", "vendas", "faturei", "faturamento", "recebi")) {
            return "spent_amount";
        }
        if (containsAny(question, "pago", "pagos", "recebido", "recebidos")) {
            return "paid_amount";
        }
        if (containsAny(question, "pendente", "pendentes")) {
            return "pending_amount";
        }
        if (containsAny(question, "rejeitado", "rejeitados", "recusado", "recusados")) {
            return "rejected_amount";
        }
        if (containsAny(question, "quantos pedidos", "quantas compras", "quantas vendas", "quantidade de pedidos")) {
            return "orders_count";
        }
        if (containsAny(question, "produto mais", "mais vendido", "mais comprado", "top produto")) {
            return "top_product";
        }
        return null;
    }

    private String buildLastStoreReply(CustomerSummaryResponse customerSummary) {
        if (customerSummary.getLastPurchaseStoreId() == null) {
            return "Nao encontrei uma compra recente para identificar a ultima loja.";
        }

        String storeLabel = customerSummary.getLastPurchaseStoreName() != null && !customerSummary.getLastPurchaseStoreName().isBlank()
                ? customerSummary.getLastPurchaseStoreName()
                : "ID " + customerSummary.getLastPurchaseStoreId();

        if (customerSummary.getLastPurchaseAt() == null) {
            return "Sua compra mais recente foi na loja " + storeLabel + ".";
        }

        return "Sua compra mais recente foi na loja " + storeLabel
                + ", em " + customerSummary.getLastPurchaseAt()
                + ", no valor de R$ " + scaleMoney(customerSummary.getLastPurchaseAmount()) + ".";
    }

    private String buildLastPurchaseReply(CustomerSummaryResponse customerSummary) {
        if (customerSummary.getLastPurchaseAt() == null) {
            return "Nao encontrei uma compra recente para responder com seguranca.";
        }

        String products = customerSummary.getLastPurchaseProductNames() == null || customerSummary.getLastPurchaseProductNames().isEmpty()
                ? "sem produtos identificados"
                : String.join(", ", customerSummary.getLastPurchaseProductNames());
        String storeLabel = customerSummary.getLastPurchaseStoreName() != null && !customerSummary.getLastPurchaseStoreName().isBlank()
                ? customerSummary.getLastPurchaseStoreName()
                : defaultValue(customerSummary.getLastPurchaseStoreId() == null ? null : customerSummary.getLastPurchaseStoreId().toString());

        return "Sua compra mais recente foi em " + customerSummary.getLastPurchaseAt()
                + ", na loja " + storeLabel
                + ", no valor de R$ " + scaleMoney(customerSummary.getLastPurchaseAmount())
                + ", com status de pagamento " + defaultValue(customerSummary.getLastPurchasePaymentStatus())
                + ". Produtos: " + products + ".";
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String sanitizeQuestion(String question) {
        if (question == null) {
            return "";
        }
        return question.trim().replaceAll("\\s+", " ");
    }

    private String defaultValue(String value) {
        return value == null || value.isBlank() ? "Nao informado" : value;
    }

    private Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private RuntimeException translateAiException(Exception ex) {
        String message = extractMessage(ex).toLowerCase(Locale.ROOT);

        if (message.contains("rate limit") || message.contains("429") || message.contains("tokens per minute")) {
            return new TooManyRequestsException("O servico de IA atingiu o limite temporario do provedor. Aguarde alguns segundos e tente novamente.");
        }

        return new IllegalStateException("Falha ao processar a resposta do provedor de IA.", ex);
    }

    private String extractMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;

        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                builder.append(current.getMessage()).append(' ');
            }
            current = current.getCause();
        }

        return builder.toString().trim();
    }

    private record QuestionScope(boolean greetingOnly, String period, String intent) {
        private static QuestionScope defaultScope() {
            return new QuestionScope(false, null, null);
        }
    }
}
