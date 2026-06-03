package br.com.signal.signal_sales_service.catalog.repository;

import br.com.signal.signal_sales_service.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrueOrderByNameAsc();

    Page<Product> findByActiveTrueOrderByNameAsc(Pageable pageable);

    List<Product> findByStoreIdAndActiveTrueOrderByNameAsc(UUID storeId);

    Page<Product> findByStoreIdAndActiveTrueOrderByNameAsc(UUID storeId, Pageable pageable);

    List<Product> findByCategory_IdAndActiveTrueOrderByNameAsc(UUID categoryId);

    Page<Product> findByCategory_IdAndActiveTrueOrderByNameAsc(UUID categoryId, Pageable pageable);

    List<Product> findByStoreIdAndCategory_IdOrderByNameAsc(
            UUID storeId,
            UUID categoryId
    );

    List<Product> findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(
            UUID storeId,
            UUID categoryId
    );

    Page<Product> findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(
            UUID storeId,
            UUID categoryId,
            Pageable pageable
    );

    boolean existsByStoreIdAndNameIgnoreCaseAndActiveTrue(
            UUID storeId,
            String name
    );

    Optional<Product> findByStoreIdAndCategory_IdAndNameIgnoreCaseAndActiveTrue(
            UUID storeId,
            UUID categoryId,
            String name
    );
}
