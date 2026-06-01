package br.com.signal.signal_sales_service.sync.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSyncRequest {

    @Size(max = 150, message = "Device id must have at most 150 characters")
    private String deviceId;

    @NotEmpty(message = "Changes are required")
    private List<@Valid CatalogSyncItemRequest> changes;
}
