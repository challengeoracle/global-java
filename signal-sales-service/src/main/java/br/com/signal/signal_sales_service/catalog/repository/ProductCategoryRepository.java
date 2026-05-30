package br.com.signal.signal_sales_service.catalog.repository;

import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    List<ProductCategory> findByActiveTrueOrderByNameAsc();

    List<ProductCategory> findByStoreIdAndActiveTrueOrderByNameAsc(UUID storeId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByStoreIdAndNameIgnoreCaseAndActiveTrue(UUID storeId, String name);
}