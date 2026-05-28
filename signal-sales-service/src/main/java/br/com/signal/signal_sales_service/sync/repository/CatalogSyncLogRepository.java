package br.com.signal.signal_sales_service.sync.repository;

import br.com.signal.signal_sales_service.sync.entity.CatalogSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CatalogSyncLogRepository extends JpaRepository<CatalogSyncLog, UUID> {

    Optional<CatalogSyncLog> findByOperationId(String operationId);
}