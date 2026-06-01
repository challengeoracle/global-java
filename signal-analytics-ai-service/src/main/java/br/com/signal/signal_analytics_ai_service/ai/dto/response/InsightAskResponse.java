package br.com.signal.signal_analytics_ai_service.ai.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightAskResponse {

    private String answer;
    private String source;
    private String model;
    private List<String> sources;
    private List<String> capabilities;
}
