package br.com.signal.signal_sales_service.sync.dto.request;

import br.com.signal.signal_sales_service.sync.entity.enums.CatalogSyncOperation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSyncItemRequest {

    private String operationId;

    private UUID productId;

    @NotNull(message = "Operation is required")
    private CatalogSyncOperation operation;

    private UUID categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stockQuantity;

    private Integer quantityDelta;

    @NotNull(message = "Local updated date is required")
    private LocalDateTime localUpdatedAt;
}