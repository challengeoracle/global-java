package br.com.signal.signal_analytics_ai_service.ai.knowledge;

import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    private static final String SOURCE_NAME = "offpay-rules-pdf";
    private static final String SOURCE_TITLE = "Regras operacionais do OffPay";

    private final KnowledgeChunkRepository repository;

    @Value("${knowledge.retrieval.max-chunks:3}")
    private int maxChunks;

    @Cacheable(cacheNames = "knowledgeRetrieval", key = "T(java.util.Objects).toString(#authUser == null ? null : #authUser.role, 'UNKNOWN') + ':' + T(java.util.Objects).toString(#question, '')")
    public List<KnowledgeSnippet> retrieve(AuthUserResponse authUser, String question) {
        return retrieveInternal(authUser, question);
    }

    @Cacheable(cacheNames = "knowledgeRetrieval", key = "T(java.util.Objects).toString(#role, 'UNKNOWN') + ':' + T(java.util.Objects).toString(#question, '')")
    public List<KnowledgeSnippet> retrieveByRole(String role, String question) {
        AuthUserResponse authUser = AuthUserResponse.builder().role(role).build();
        return retrieveInternal(authUser, question);
    }

    public List<KnowledgeSource> listAvailableSources() {
        return List.of(new KnowledgeSource(SOURCE_NAME, SOURCE_TITLE));
    }

    public String readSource(String documentId) {
        if (!SOURCE_NAME.equals(documentId)) {
            return "";
        }

        return repository.findTop50BySourceNameOrderByChunkIndexAsc(SOURCE_NAME).stream()
                .map(KnowledgeChunk::getContent)
                .reduce("", (left, right) -> left.isBlank() ? right : left + "\n\n" + right);
    }

    public String renderContext(List<KnowledgeSnippet> snippets) {
        if (snippets.isEmpty()) {
            return "Nenhuma regra adicional encontrada.";
        }

        StringBuilder builder = new StringBuilder();

        for (KnowledgeSnippet snippet : snippets) {
            builder.append("- Fonte: ").append(snippet.title()).append('\n');
            builder.append(snippet.content()).append("\n\n");
        }

        return builder.toString().trim();
    }

    private Set<String> extractTerms(String normalizedQuestion) {
        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return Set.of("offline", "pagamento");
        }

        Set<String> terms = new LinkedHashSet<>();

        for (String part : normalizedQuestion.split("\\s+")) {
            if (part.length() >= 4) {
                terms.add(part);
            }
        }

        return terms.isEmpty() ? Set.of(normalizedQuestion) : terms;
    }

    private int computeScore(KnowledgeChunk chunk, String term, AuthUserResponse authUser) {
        int score = 3;

        if (chunk.getNormalizedContent().contains(term)) {
            score += 5;
        }

        if (authUser != null && authUser.isSeller() && chunk.getNormalizedContent().contains("vendedor")) {
            score += 2;
        }

        if (authUser != null && authUser.isCustomer() && chunk.getNormalizedContent().contains("cliente")) {
            score += 2;
        }

        return score;
    }

    private List<KnowledgeSnippet> retrieveInternal(AuthUserResponse authUser, String question) {
        String normalizedQuestion = PdfKnowledgeIngestionService.normalize(question);
        Set<String> terms = extractTerms(normalizedQuestion);
        List<KnowledgeSnippet> snippets = new ArrayList<>();

        for (String term : terms) {
            List<KnowledgeChunk> matches = repository.searchByTerm(term, PageRequest.of(0, maxChunks));

            for (KnowledgeChunk match : matches) {
                snippets.add(new KnowledgeSnippet(
                        match.getSourceName(),
                        match.getTitle(),
                        match.getContent(),
                        computeScore(match, term, authUser)
                ));
            }
        }

        if (snippets.isEmpty()) {
            List<KnowledgeChunk> fallback = repository.findTop50BySourceNameOrderByChunkIndexAsc(SOURCE_NAME)
                    .stream()
                    .limit(Math.max(1, maxChunks))
                    .toList();

            return fallback.stream()
                    .map(chunk -> new KnowledgeSnippet(chunk.getSourceName(), chunk.getTitle(), chunk.getContent(), 1))
                    .toList();
        }

        return snippets.stream()
                .distinct()
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(Math.max(1, maxChunks))
                .toList();
    }

    public record KnowledgeSource(String id, String title) {
    }
}
