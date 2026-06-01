package br.com.signal.signal_sales_service.sync.dto.request;

import br.com.signal.signal_sales_service.order.dto.request.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "Local order id must have at most 100 characters")
    private String localOrderId;

    private UUID customerId;

    @NotNull(message = "Offline created date is required")
    private LocalDateTime offlineCreatedAt;

    @NotEmpty(message = "Items are required")
    private List<@Valid OrderItemRequest> items;
}
