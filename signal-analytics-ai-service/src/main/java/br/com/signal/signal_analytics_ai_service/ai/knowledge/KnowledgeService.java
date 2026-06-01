package br.com.signal.signal_analytics_ai_service.ai.knowledge;

import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private static final String BASE_PATH = "classpath:knowledge/runtime/";

    private final ResourceLoader resourceLoader;

    public String buildKnowledgeContext(AuthUserResponse authUser, String question) {
        List<String> files = new ArrayList<>();

        files.add("core-rules.md");

        if (authUser.isSeller()) {
            files.add("seller-rules.md");
        }

        if (authUser.isCustomer()) {
            files.add("customer-rules.md");
        }

        String normalizedQuestion = normalize(question);

        if (containsAny(normalizedQuestion,
                "pagamento", "pago", "paga", "recusado", "saldo", "carteira",
                "pendente", "liberar", "crédito", "credito", "débito", "debito")) {
            files.add("payment-rules.md");
        }

        if (containsAny(normalizedQuestion,
                "sincronização", "sincronizacao", "sincronizar", "offline",
                "online", "pendente", "servidor", "sync")) {
            files.add("sync-rules.md");
        }

        if (containsAny(normalizedQuestion,
                "produto", "produtos", "vendeu", "vendido", "comprou",
                "comprado", "mais saiu", "mais comprei", "catálogo", "catalogo",
                "estoque")) {
            files.add("product-rules.md");
        }

        return readFiles(files);
    }

    private String readFiles(List<String> files) {
        StringBuilder builder = new StringBuilder();

        for (String file : files.stream().distinct().toList()) {
            builder.append(readFile(file)).append("\n\n");
        }

        return builder.toString().trim();
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

        return value
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }

        return false;
    }
}