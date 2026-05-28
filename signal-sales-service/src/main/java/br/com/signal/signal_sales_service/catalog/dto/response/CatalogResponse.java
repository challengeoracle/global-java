package br.com.signal.signal_sales_service.catalog.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogResponse {

    private UUID storeId;
    private LocalDateTime syncedAt;
    private List<CatalogCategoryResponse> categories;
}