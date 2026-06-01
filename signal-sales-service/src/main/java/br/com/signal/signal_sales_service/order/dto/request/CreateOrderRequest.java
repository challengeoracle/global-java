package br.com.signal.signal_sales_service.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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

    @Size(max = 150, message = "Device id must have at most 150 characters")
    private String deviceId;

    @NotEmpty(message = "Items are required")
    private List<@Valid OrderItemRequest> items;
}
