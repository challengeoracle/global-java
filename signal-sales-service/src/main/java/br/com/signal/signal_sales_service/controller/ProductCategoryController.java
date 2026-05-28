package br.com.signal.signal_sales_service.controller;

import br.com.signal.signal_sales_service.dto.CategoryResponse;
import br.com.signal.signal_sales_service.dto.CategoryWithProductsResponse;
import br.com.signal.signal_sales_service.dto.CreateCategoryRequest;
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
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productCategoryService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAllActive() {
        return ResponseEntity.ok(
                productCategoryService.findAllActive()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryWithProductsResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                productCategoryService.findByIdWithProducts(id)
        );
    }
}