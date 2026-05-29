package br.com.signal.signal_sales_service.controller;

import br.com.signal.signal_sales_service.dto.CreateProductRequest;
import br.com.signal.signal_sales_service.dto.ProductResponse;
import br.com.signal.signal_sales_service.dto.UpdateProductRequest;
import br.com.signal.signal_sales_service.service.ProductService;
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

    @GetMapping("/store/{storeId}/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> findByStoreAndCategory(
            @PathVariable UUID storeId,
            @PathVariable UUID categoryId
    ) {
        return ResponseEntity.ok(
                productService.findByStoreAndCategory(storeId, categoryId)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> findByCategory(
            @PathVariable UUID categoryId
    ) {
        return ResponseEntity.ok(
                productService.findByCategory(categoryId)
        );
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