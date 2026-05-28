package br.com.signal.signal_sales_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_ORDER_SYNC_LOGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "STORE_ID", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    private SalesOrder order;

    @Column(name = "LOCAL_ORDER_ID", length = 100)
    private String localOrderId;

    @Column(name = "DEVICE_ID", length = 150)
    private String deviceId;

    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @Column(name = "MESSAGE", length = 255)
    private String message;

    @Column(name = "SYNCED_AT", nullable = false)
    private LocalDateTime syncedAt;
}