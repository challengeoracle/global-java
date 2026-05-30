package br.com.signal.signal_sales_service.sync.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncItemResponse {

    private String localId;

    private String operationId;

    private UUID remoteId;

    private String status;

    private String message;

    private SyncItemStateResponse currentState;

    private LocalDateTime syncedAt;
}
