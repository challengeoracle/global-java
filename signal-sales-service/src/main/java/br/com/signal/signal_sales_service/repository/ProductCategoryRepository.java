package br.com.signal.signal_sales_service.repository;

import br.com.signal.signal_sales_service.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    List<ProductCategory> findByStoreIdAndActiveTrueOrderByNameAsc(UUID storeId);

    boolean existsByStoreIdAndNameIgnoreCaseAndActiveTrue(
            UUID storeId,
            String name
    );
}