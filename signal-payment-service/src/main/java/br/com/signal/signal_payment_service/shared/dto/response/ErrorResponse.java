package br.com.signal.signal_payment_service.shared.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private Integer status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}