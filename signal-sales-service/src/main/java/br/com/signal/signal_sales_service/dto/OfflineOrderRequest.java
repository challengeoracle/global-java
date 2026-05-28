package br.com.signal.signal_sales_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineOrderRequest {

    @NotBlank(message = "Local order id is required")
    private String localOrderId;

    private UUID customerId;

    @NotNull(message = "Offline created date is required")
    private LocalDateTime offlineCreatedAt;

    @NotEmpty(message = "Items are required")
    private List<@Valid OrderItemRequest> items;
}