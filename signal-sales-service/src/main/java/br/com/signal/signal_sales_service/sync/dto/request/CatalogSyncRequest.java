package br.com.signal.signal_sales_service.sync.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSyncRequest {

    private String deviceId;

    @NotEmpty(message = "Changes are required")
    private List<@Valid CatalogSyncItemRequest> changes;
}