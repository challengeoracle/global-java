package br.com.signal.signal_sales_service.order.repository;

import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    List<SalesOrder> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    List<SalesOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<SalesOrder> findByStoreIdOrCustomerIdOrderByCreatedAtDesc(UUID storeId, UUID customerId);

    Optional<SalesOrder> findByLocalOrderId(String localOrderId);
}