package br.com.signal.signal_analytics_ai_service.shared.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {

    private UUID id;
    private String name;
    private String category;
}
