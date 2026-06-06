package br.com.signal.signal_analytics_ai_service.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "OffPay Analytics AI Service API",
                version = "v1",
                description = "Serviço de análises e insights com IA do OffPay. Responsável por resumos, gráficos e perguntas sobre a operação.",
                contact = @Contact(name = "Equipe OffPay", email = "devops@offpay.local"),
                license = @License(name = "Academic Use Only")
        ),
        servers = {
                @Server(url = "http://localhost:8084", description = "Ambiente local"),
                @Server(url = "https://app-offpay-analytics-rm559728.azurewebsites.net", description = "Ambiente Azure")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
