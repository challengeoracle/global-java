package br.com.signal.signal_sales_service.catalog.controller;

import br.com.signal.signal_sales_service.catalog.dto.request.CreateProductRequest;
import br.com.signal.signal_sales_service.catalog.dto.request.UpdateProductRequest;
import br.com.signal.signal_sales_service.catalog.dto.response.ProductResponse;
import br.com.signal.signal_sales_service.catalog.service.ProductService;
import br.com.signal.signal_sales_service.shared.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @RequestBody @Valid CreateProductRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request, authorization));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAllActive() {
        return ResponseEntity.ok(
                productService.findAllActive()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponse<ProductResponse>> findAllActivePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.findAllActivePage(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                productService.findById(id)
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductResponse>> findByStore(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
                productService.findByStore(storeId)
        );
    }

    @GetMapping("/store/{storeId}/page")
    public ResponseEntity<PageResponse<ProductResponse>> findByStorePage(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.findByStorePage(storeId, page, size));
    }

    @GetMapping("/store/{storeId}/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> findByStoreAndCategory(
            @PathVariable UUID storeId,
            @PathVariable UUID categoryId
    ) {
        return ResponseEntity.ok(
                productService.findByStoreAndCategory(storeId, categoryId)
        );
    }

    @GetMapping("/store/{storeId}/category/{categoryId}/page")
    public ResponseEntity<PageResponse<ProductResponse>> findByStoreAndCategoryPage(
            @PathVariable UUID storeId,
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.findByStoreAndCategoryPage(storeId, categoryId, page, size));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> findByCategory(
            @PathVariable UUID categoryId
    ) {
        return ResponseEntity.ok(
                productService.findByCategory(categoryId)
        );
    }

    @GetMapping("/category/{categoryId}/page")
    public ResponseEntity<PageResponse<ProductResponse>> findByCategoryPage(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.findByCategoryPage(categoryId, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProductRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                productService.update(id, request, authorization)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        productService.deactivate(id, authorization);

        return ResponseEntity.noContent().build();
    }
}
