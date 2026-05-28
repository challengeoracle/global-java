package br.com.signal.signal_sales_service.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSyncItemResponse {

    private UUID productId;
    private String status;
    private String message;
    private Integer currentStockQuantity;
}