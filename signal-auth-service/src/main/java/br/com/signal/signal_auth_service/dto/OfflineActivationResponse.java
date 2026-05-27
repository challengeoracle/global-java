package br.com.signal.signal_auth_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineActivationResponse {

    private String deviceId;
    private String offlineToken;
    private LocalDateTime offlineExpiresAt;
    private Boolean active;
}