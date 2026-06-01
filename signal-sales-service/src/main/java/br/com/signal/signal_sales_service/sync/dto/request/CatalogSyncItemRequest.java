package br.com.signal.signal_sales_service.sync.dto.request;

import br.com.signal.signal_sales_service.sync.entity.enums.CatalogSyncOperation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 100, message = "Operation id must have at most 100 characters")
    private String operationId;

    private UUID productId;

    @NotNull(message = "Operation is required")
    private CatalogSyncOperation operation;

    private UUID categoryId;

    @Size(max = 120, message = "Name must have at most 120 characters")
    private String name;

    @Size(max = 255, message = "Description must have at most 255 characters")
    private String description;

    private BigDecimal price;

    private Integer stockQuantity;

    private Integer quantityDelta;

    @NotNull(message = "Local updated date is required")
    private LocalDateTime localUpdatedAt;
}
