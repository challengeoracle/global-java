package br.com.signal.signal_sales_service.shared.exception;


public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}