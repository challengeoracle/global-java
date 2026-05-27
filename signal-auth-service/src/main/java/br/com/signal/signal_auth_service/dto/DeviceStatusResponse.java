package br.com.signal.signal_auth_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceStatusResponse {

    private String deviceId;

    private Boolean active;

    private Boolean offlineEnabled;

    private Boolean expired;

    private LocalDateTime offlineExpiresAt;
}