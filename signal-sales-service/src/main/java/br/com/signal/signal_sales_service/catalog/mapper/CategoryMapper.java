package br.com.signal.signal_sales_service.catalog.mapper;

import br.com.signal.signal_sales_service.catalog.dto.response.CategoryResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.CategoryWithProductsResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.ProductResponse;
import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;

import java.util.List;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(ProductCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .storeId(category.getStoreId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public static CategoryWithProductsResponse toWithProductsResponse(
            ProductCategory category,
            List<ProductResponse> products
    ) {
        return CategoryWithProductsResponse.builder()
                .id(category.getId())
                .storeId(category.getStoreId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .products(products)
                .build();
    }

    public static List<ProductResponse> toProductResponses(List<Product> products) {
        return products.stream()
                .map(ProductMapper::toResponse)
                .toList();
    }
}
