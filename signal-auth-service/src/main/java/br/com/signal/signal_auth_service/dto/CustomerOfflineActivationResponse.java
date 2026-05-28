package br.com.signal.signal_auth_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOfflineActivationResponse {

    private String sessionToken;

    private LocalDateTime expiresAt;

    private Boolean active;
}