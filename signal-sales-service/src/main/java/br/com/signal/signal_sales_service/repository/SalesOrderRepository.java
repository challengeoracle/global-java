package br.com.signal.signal_sales_service.repository;

import br.com.signal.signal_sales_service.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    List<SalesOrder> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    List<SalesOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    boolean existsByDeviceIdAndLocalOrderId(String deviceId, String localOrderId);

    Optional<SalesOrder> findByDeviceIdAndLocalOrderId(String deviceId, String localOrderId);
}