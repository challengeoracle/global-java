package br.com.signal.signal_sales_service.sync.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncItemStateResponse {

    private String orderStatus;

    private String paymentStatus;

    private String syncStatus;

    private BigDecimal totalAmount;

    private UUID productId;

    private UUID categoryId;

    private Integer stockQuantity;

    private String operation;

    private Boolean active;
}
