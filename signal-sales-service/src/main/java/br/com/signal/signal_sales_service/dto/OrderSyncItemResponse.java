package br.com.signal.signal_sales_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSyncItemResponse {

    private String localOrderId;

    private UUID orderId;

    private String status;

    private String message;

    private String orderStatus;

    private String paymentStatus;

    private String syncStatus;

    private BigDecimal totalAmount;
}