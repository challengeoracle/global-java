package br.com.signal.signal_sales_service.order.repository;

import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    List<SalesOrder> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Page<SalesOrder> findByStoreIdOrderByCreatedAtDesc(UUID storeId, Pageable pageable);

    List<SalesOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Page<SalesOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    List<SalesOrder> findByStoreIdOrCustomerIdOrderByCreatedAtDesc(UUID storeId, UUID customerId);

    Page<SalesOrder> findByStoreIdOrCustomerIdOrderByCreatedAtDesc(UUID storeId, UUID customerId, Pageable pageable);

    Optional<SalesOrder> findByLocalOrderId(String localOrderId);
}
