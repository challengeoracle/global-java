package br.com.signal.signal_analytics_ai_service.ai.knowledge;

import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    private static final String BASE_PATH = "classpath:knowledge/runtime/";
    private static final List<String> BASE_FILES = List.of("core-rules.md");
    private static final Map<String, String> FILE_TITLES = Map.of(
            "core-rules.md", "Regras centrais do OffPay",
            "seller-rules.md", "Regras do vendedor",
            "customer-rules.md", "Regras do cliente",
            "payment-rules.md", "Regras de pagamento e carteira",
            "sync-rules.md", "Regras de sincronizacao",
            "product-rules.md", "Regras de catalogo e estoque"
    );

    private final ResourceLoader resourceLoader;

    public List<KnowledgeSnippet> retrieve(AuthUserResponse authUser, String question) {
        return retrieveByRole(authUser == null ? null : authUser.getRole(), question);
    }

    public List<KnowledgeSnippet> retrieveByRole(String role, String question) {
        List<String> candidateFiles = new ArrayList<>(BASE_FILES);

        if ("SELLER".equalsIgnoreCase(role)) {
            candidateFiles.add("seller-rules.md");
        }

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            candidateFiles.add("customer-rules.md");
        }

        candidateFiles.add("payment-rules.md");
        candidateFiles.add("sync-rules.md");
        candidateFiles.add("product-rules.md");

        String normalizedQuestion = normalize(question);
        Set<String> questionTerms = extractTerms(normalizedQuestion);

        return candidateFiles.stream()
                .distinct()
                .map(file -> buildSnippet(file, questionTerms))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(KnowledgeSnippet::score).reversed())
                .limit(3)
                .toList();
    }

    public List<KnowledgeSource> listAvailableSources() {
        return FILE_TITLES.entrySet().stream()
                .map(entry -> new KnowledgeSource(stripExtension(entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparing(KnowledgeSource::title))
                .toList();
    }

    public String readSource(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            return "";
        }

        String fileName = documentId.endsWith(".md") ? documentId : documentId + ".md";
        return readFile(fileName);
    }

    public String renderContext(List<KnowledgeSnippet> snippets) {
        if (snippets.isEmpty()) {
            return "Nenhum documento adicional foi recuperado.";
        }

        StringBuilder builder = new StringBuilder();

        for (KnowledgeSnippet snippet : snippets) {
            builder.append("- Fonte: ").append(snippet.title()).append(" (").append(snippet.source()).append(")\n");
            builder.append(snippet.content()).append("\n\n");
        }

        return builder.toString().trim();
    }

    private KnowledgeSnippet buildSnippet(String fileName, Set<String> questionTerms) {
        String content = readFile(fileName);

        if (content.isBlank()) {
            return null;
        }

        int score = scoreContent(normalize(content), questionTerms, fileName);

        return new KnowledgeSnippet(
                fileName,
                FILE_TITLES.getOrDefault(fileName, fileName),
                content,
                score
        );
    }

    private int scoreContent(String normalizedContent, Set<String> questionTerms, String fileName) {
        int score = 0;

        for (String term : questionTerms) {
            if (normalizedContent.contains(term)) {
                score += 3;
            }
        }

        if (fileName.contains("payment") && containsAny(questionTerms, "pagamento", "saldo", "carteira", "credito", "debito", "pendente")) {
            score += 5;
        }

        if (fileName.contains("sync") && containsAny(questionTerms, "sync", "sincronizacao", "offline", "online", "servidor")) {
            score += 5;
        }

        if (fileName.contains("product") && containsAny(questionTerms, "produto", "estoque", "catalogo", "venda", "compra")) {
            score += 5;
        }

        return score;
    }

    private boolean containsAny(Set<String> terms, String... expectedTerms) {
        for (String expectedTerm : expectedTerms) {
            if (terms.contains(expectedTerm)) {
                return true;
            }
        }

        return false;
    }

    private Set<String> extractTerms(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(value.split("\\s+"))
                .map(String::trim)
                .filter(term -> term.length() >= 3)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    private String readFile(String fileName) {
        try {
            Resource resource = resourceLoader.getResource(BASE_PATH + fileName);

            if (!resource.exists()) {
                return "";
            }

            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex < 0 ? fileName : fileName.substring(0, dotIndex);
    }

    public record KnowledgeSource(String id, String title) {
    }
}
