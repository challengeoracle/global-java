package br.com.signal.signal_sales_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    private UUID storeId;

    private String deviceId;

    @NotEmpty(message = "Items are required")
    private List<@Valid OrderItemRequest> items;
}