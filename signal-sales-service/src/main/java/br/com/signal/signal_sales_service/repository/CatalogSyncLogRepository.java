package br.com.signal.signal_sales_service.repository;

import br.com.signal.signal_sales_service.entity.CatalogSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CatalogSyncLogRepository extends JpaRepository<CatalogSyncLog, UUID> {
}