package br.com.signal.signal_sales_service.catalog.mapper;

import br.com.signal.signal_sales_service.catalog.dto.response.CatalogCategoryResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.CatalogProductResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.CatalogResponse;
import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static CatalogResponse toResponse(UUID storeId, List<ProductCategory> categories, List<Product> products) {
        List<CatalogCategoryResponse> categoryResponses = categories.stream()
                .map(category -> {
                    List<CatalogProductResponse> categoryProducts = products.stream()
                            .filter(product -> product.getCategory().getId().equals(category.getId()))
                            .map(CatalogMapper::toProductResponse)
                            .toList();

                    return CatalogCategoryResponse.builder()
                            .id(category.getId())
                            .storeId(category.getStoreId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .active(category.getActive())
                            .createdAt(category.getCreatedAt())
                            .updatedAt(category.getUpdatedAt())
                            .products(categoryProducts)
                            .build();
                })
                .toList();

        return CatalogResponse.builder()
                .storeId(storeId)
                .syncedAt(LocalDateTime.now())
                .categories(categoryResponses)
                .build();
    }

    public static CatalogProductResponse toProductResponse(Product product) {
        return CatalogProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
