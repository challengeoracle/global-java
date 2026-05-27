package br.com.signal.signal_auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivateOfflineRequest {

    @NotBlank
    private String deviceId;
}