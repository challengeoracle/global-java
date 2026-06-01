package br.com.signal.signal_analytics_ai_service.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightAskRequest {

    @NotBlank(message = "Question is required")
    private String question;
}