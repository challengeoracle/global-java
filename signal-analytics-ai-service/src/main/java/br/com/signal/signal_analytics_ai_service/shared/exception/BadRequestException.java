package br.com.signal.signal_analytics_ai_service.shared.exception;


public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}