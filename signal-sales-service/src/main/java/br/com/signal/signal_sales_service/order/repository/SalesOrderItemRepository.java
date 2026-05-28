package br.com.signal.signal_sales_service.order.repository;

import br.com.signal.signal_sales_service.order.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, UUID> {
}