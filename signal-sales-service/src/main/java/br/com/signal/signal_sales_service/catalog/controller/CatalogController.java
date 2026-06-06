package br.com.signal.signal_sales_service.catalog.controller;

import br.com.signal.signal_sales_service.catalog.dto.response.CatalogResponse;
import br.com.signal.signal_sales_service.catalog.hateoas.CatalogModelAssembler;
import br.com.signal.signal_sales_service.sync.dto.request.CatalogSyncRequest;
import br.com.signal.signal_sales_service.sync.dto.response.CatalogSyncResponse;
import br.com.signal.signal_sales_service.catalog.service.CatalogService;
import br.com.signal.signal_sales_service.sync.service.CatalogSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Consulta e sincronização do catálogo da loja.")
public class CatalogController {

    private final CatalogService catalogService;
    private final CatalogSyncService catalogSyncService;
    private final CatalogModelAssembler catalogModelAssembler;

    @GetMapping("/me")
    @Operation(summary = "Consultar meu catálogo", description = "Retorna o catálogo consolidado da loja do vendedor autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CatalogResponse> findMyCatalog(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                catalogService.findMyCatalog(authorization)
        );
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Consultar catálogo por loja", description = "Retorna o catálogo público de uma loja específica.")
    public ResponseEntity<CatalogResponse> findCatalogByStore(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
                catalogService.findCatalogByStore(storeId)
        );
    }

    @GetMapping("/store/{storeId}/resource")
    @Operation(summary = "Consultar catálogo HATEOAS", description = "Retorna o catálogo público de uma loja no formato de recurso HATEOAS.")
    public ResponseEntity<EntityModel<CatalogResponse>> findCatalogByStoreResource(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
                catalogModelAssembler.toModel(catalogService.findCatalogByStore(storeId))
        );
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar catálogo", description = "Recebe alterações offline e sincroniza categorias e produtos do catálogo.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CatalogSyncResponse> syncCatalog(
            @RequestBody @Valid CatalogSyncRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                catalogSyncService.syncCatalog(request, authorization)
        );
    }
}
