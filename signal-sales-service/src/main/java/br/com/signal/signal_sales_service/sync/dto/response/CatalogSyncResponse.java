package br.com.signal.signal_sales_service.sync.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSyncResponse {

    private UUID storeId;
    private LocalDateTime syncedAt;
    private List<SyncItemResponse> results;
}