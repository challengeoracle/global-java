package br.com.signal.signal_sales_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSyncResponse {

    private UUID storeId;
    private LocalDateTime syncedAt;
    private List<OrderSyncItemResponse> results;
}