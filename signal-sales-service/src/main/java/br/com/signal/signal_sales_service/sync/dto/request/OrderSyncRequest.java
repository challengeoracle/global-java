package br.com.signal.signal_sales_service.sync.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSyncRequest {

    @NotBlank(message = "Device id is required")
    private String deviceId;

    @NotEmpty(message = "Orders are required")
    private List<@Valid OfflineOrderRequest> orders;
}