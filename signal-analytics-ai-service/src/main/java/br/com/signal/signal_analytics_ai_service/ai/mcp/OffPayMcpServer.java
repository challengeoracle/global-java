package br.com.signal.signal_analytics_ai_service.ai.mcp;

import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeRetrievalService;
import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeSnippet;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OffPayMcpServer {

    private final KnowledgeRetrievalService knowledgeRetrievalService;

    @McpTool(name = "search_offpay_runtime_knowledge", description = "Busca regras operacionais do OffPay sobre offline-first, sincronização, catálogo, estoque e pagamentos.")
    public String searchOffPayRuntimeKnowledge(
            @McpArg(name = "role", description = "Papel do usuário: SELLER ou CUSTOMER.", required = false) String role,
            @McpArg(name = "question", description = "Pergunta ou tema para recuperar conhecimento.", required = true) String question
    ) {
        List<KnowledgeSnippet> snippets = knowledgeRetrievalService.retrieveByRole(role, question);
        return knowledgeRetrievalService.renderContext(snippets);
    }

    @McpTool(name = "list_offpay_runtime_documents", description = "Lista os documentos de conhecimento disponíveis no runtime do OffPay.")
    public List<KnowledgeRetrievalService.KnowledgeSource> listOffPayRuntimeDocuments() {
        return knowledgeRetrievalService.listAvailableSources();
    }

    @McpResource(uri = "resource://offpay/knowledge/{documentId}", name = "offpay_runtime_knowledge_document", description = "Recupera o conteúdo integral de um documento de conhecimento do OffPay.")
    public String readOffPayKnowledgeDocument(
            @McpArg(name = "documentId", description = "Identificador do documento sem a extensão .md.", required = true) String documentId
    ) {
        return knowledgeRetrievalService.readSource(documentId);
    }
}
