package br.com.signal.signal_sales_service.controller;

import br.com.signal.signal_sales_service.dto.CategoryResponse;
import br.com.signal.signal_sales_service.dto.CategoryWithProductsResponse;
import br.com.signal.signal_sales_service.dto.CreateCategoryRequest;
import br.com.signal.signal_sales_service.dto.UpdateCategoryRequest;
import br.com.signal.signal_sales_service.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @RequestBody @Valid CreateCategoryRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productCategoryService.create(request, authorization));
    }

    @GetMapping("/me")
    public ResponseEntity<List<CategoryResponse>> findMyCategories(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                productCategoryService.findMyCategories(authorization)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryWithProductsResponse> findById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                productCategoryService.findMyCategoryById(id, authorization)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCategoryRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                productCategoryService.update(id, request, authorization)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        productCategoryService.deactivate(id, authorization);

        return ResponseEntity.noContent().build();
    }
}