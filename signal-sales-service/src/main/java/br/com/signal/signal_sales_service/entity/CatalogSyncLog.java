package br.com.signal.signal_sales_service.entity;

import br.com.signal.signal_sales_service.entity.enums.CatalogSyncOperation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_CATALOG_SYNC_LOGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "STORE_ID", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @Column(name = "DEVICE_ID", length = 150)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION", nullable = false, length = 50)
    private CatalogSyncOperation operation;

    @Column(name = "QUANTITY_DELTA")
    private Integer quantityDelta;

    @Column(name = "PRODUCT_NAME", length = 120)
    private String productName;

    @Column(name = "CATEGORY_ID")
    private UUID categoryId;

    @Column(name = "PRICE", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "STOCK_QUANTITY")
    private Integer stockQuantity;

    @Column(name = "LOCAL_UPDATED_AT", nullable = false)
    private LocalDateTime localUpdatedAt;

    @Column(name = "SYNCED_AT", nullable = false)
    private LocalDateTime syncedAt;

    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @Column(name = "MESSAGE", length = 255)
    private String message;
}