package br.com.signal.signal_analytics_ai_service.ai.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightAskResponse {

    private String answer;
    private String source;
    private String model;
}