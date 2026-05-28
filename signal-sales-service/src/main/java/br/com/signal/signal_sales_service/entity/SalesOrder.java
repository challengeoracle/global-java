package br.com.signal.signal_sales_service.entity;

import br.com.signal.signal_sales_service.entity.enums.OrderStatus;
import br.com.signal.signal_sales_service.entity.enums.PaymentStatus;
import br.com.signal.signal_sales_service.entity.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "TB_ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "LOCAL_ORDER_ID", length = 100)
    private String localOrderId;

    @Column(name = "STORE_ID", nullable = false)
    private UUID storeId;

    @Column(name = "CUSTOMER_ID")
    private UUID customerId;

    @Column(name = "SELLER_ID")
    private UUID sellerId;

    @Column(name = "DEVICE_ID", length = 150)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false, length = 40)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS", nullable = false, length = 40)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "SYNC_STATUS", nullable = false, length = 40)
    private SyncStatus syncStatus;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "OFFLINE_CREATED_AT")
    private LocalDateTime offlineCreatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderItem> items = new ArrayList<>();
}