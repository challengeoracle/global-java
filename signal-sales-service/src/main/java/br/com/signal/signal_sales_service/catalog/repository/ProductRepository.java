package br.com.signal.signal_sales_service.catalog.repository;

import br.com.signal.signal_sales_service.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrueOrderByNameAsc();

    List<Product> findByStoreIdAndActiveTrueOrderByNameAsc(UUID storeId);

    List<Product> findByCategory_IdAndActiveTrueOrderByNameAsc(UUID categoryId);

    List<Product> findByStoreIdAndCategory_IdOrderByNameAsc(
            UUID storeId,
            UUID categoryId
    );

    List<Product> findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(
            UUID storeId,
            UUID categoryId
    );

    boolean existsByStoreIdAndNameIgnoreCaseAndActiveTrue(
            UUID storeId,
            String name
    );
}