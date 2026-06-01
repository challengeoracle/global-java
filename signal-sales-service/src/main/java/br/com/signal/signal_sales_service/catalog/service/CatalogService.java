package br.com.signal.signal_sales_service.catalog.service;

import br.com.signal.signal_sales_service.catalog.dto.response.CatalogCategoryResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.CatalogProductResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.CatalogResponse;
import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;
import br.com.signal.signal_sales_service.catalog.mapper.CatalogMapper;
import br.com.signal.signal_sales_service.catalog.repository.ProductCategoryRepository;
import br.com.signal.signal_sales_service.catalog.repository.ProductRepository;
import br.com.signal.signal_sales_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_sales_service.shared.service.AuthIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final AuthIdentityService authIdentityService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public CatalogResponse findMyCatalog(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        return findCatalogByStore(authUser.getStoreId());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "catalogByStore", key = "#storeId")
    public CatalogResponse findCatalogByStore(UUID storeId) {
        List<ProductCategory> categories = productCategoryRepository
                .findByStoreIdAndActiveTrueOrderByNameAsc(storeId);
        Map<UUID, List<CatalogProductResponse>> productsByCategoryId = productRepository
                .findByStoreIdAndActiveTrueOrderByNameAsc(storeId)
                .stream()
                .collect(Collectors.groupingBy(
                        product -> product.getCategory().getId(),
                        Collectors.mapping(CatalogMapper::toProductResponse, Collectors.toList())
                ));

        List<CatalogCategoryResponse> categoryResponses = categories.stream()
                .map(category -> {
                    List<CatalogProductResponse> products = productsByCategoryId.getOrDefault(category.getId(), Collections.emptyList());

                    return CatalogCategoryResponse.builder()
                            .id(category.getId())
                            .storeId(category.getStoreId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .active(category.getActive())
                            .createdAt(category.getCreatedAt())
                            .updatedAt(category.getUpdatedAt())
                            .products(products)
                            .build();
                })
                .toList();

        return CatalogResponse.builder()
                .storeId(storeId)
                .syncedAt(LocalDateTime.now())
                .categories(categoryResponses)
                .build();
    }
}
