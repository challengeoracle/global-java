package br.com.signal.signal_sales_service.sync.repository;

import br.com.signal.signal_sales_service.sync.entity.OrderSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderSyncLogRepository extends JpaRepository<OrderSyncLog, UUID> {
}