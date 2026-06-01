package br.com.signal.signal_analytics_ai_service.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightAskRequest {

    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question must have at most 500 characters")
    private String question;
}
