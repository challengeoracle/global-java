package br.com.signal.signal_sales_service.dto;

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