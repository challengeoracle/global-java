package br.com.signal.signal_sales_service.service;

import br.com.signal.signal_sales_service.dto.CategoryResponse;
import br.com.signal.signal_sales_service.dto.CategoryWithProductsResponse;
import br.com.signal.signal_sales_service.dto.CreateCategoryRequest;
import br.com.signal.signal_sales_service.dto.ProductResponse;
import br.com.signal.signal_sales_service.entity.Product;
import br.com.signal.signal_sales_service.entity.ProductCategory;
import br.com.signal.signal_sales_service.exception.BadRequestException;
import br.com.signal.signal_sales_service.exception.NotFoundException;
import br.com.signal.signal_sales_service.repository.ProductCategoryRepository;
import br.com.signal.signal_sales_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;

    public CategoryResponse create(CreateCategoryRequest request) {

        String categoryName = request.getName().trim();

        if (productCategoryRepository.existsByNameIgnoreCase(categoryName)) {
            throw new BadRequestException("Category already exists");
        }

        ProductCategory category = ProductCategory.builder()
                .name(categoryName)
                .description(request.getDescription())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        productCategoryRepository.save(category);

        return toCategoryResponse(category);
    }

    public List<CategoryResponse> findAllActive() {
        return productCategoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public CategoryResponse findById(UUID id) {
        ProductCategory category = findCategoryEntityById(id);

        return toCategoryResponse(category);
    }

    public CategoryWithProductsResponse findByIdWithProducts(UUID id) {
        ProductCategory category = findCategoryEntityById(id);

        List<ProductResponse> products = productRepository
                .findByCategory_IdAndActiveTrueOrderByNameAsc(id)
                .stream()
                .map(this::toProductResponse)
                .toList();

        return CategoryWithProductsResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .products(products)
                .build();
    }

    private ProductCategory findCategoryEntityById(UUID id) {
        return productCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    private CategoryResponse toCategoryResponse(ProductCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .storeId(product.getStoreId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
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