package br.com.signal.signal_sales_service.catalog.controller;

import br.com.signal.signal_sales_service.catalog.dto.response.CategoryResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.CategoryWithProductsResponse;
import br.com.signal.signal_sales_service.catalog.dto.request.CreateCategoryRequest;
import br.com.signal.signal_sales_service.catalog.dto.request.UpdateCategoryRequest;
import br.com.signal.signal_sales_service.catalog.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Product Categories", description = "Gestao de categorias do catalogo da loja autenticada.")
@SecurityRequirement(name = "bearerAuth")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria para a loja do vendedor autenticado.")
    public ResponseEntity<CategoryResponse> create(
            @RequestBody @Valid CreateCategoryRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productCategoryService.create(request, authorization));
    }

    @GetMapping("/me")
    @Operation(summary = "Listar minhas categorias", description = "Retorna as categorias pertencentes a loja do vendedor autenticado.")
    public ResponseEntity<List<CategoryResponse>> findMyCategories(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                productCategoryService.findMyCategories(authorization)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria por id", description = "Retorna a categoria da loja autenticada com seus produtos.")
    public ResponseEntity<CategoryWithProductsResponse> findById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                productCategoryService.findMyCategoryById(id, authorization)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza nome e descricao de uma categoria da loja autenticada.")
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
    @Operation(summary = "Desativar categoria", description = "Realiza a desativacao logica da categoria informada.")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        productCategoryService.deactivate(id, authorization);

        return ResponseEntity.noContent().build();
    }
}
