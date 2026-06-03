package br.com.signal.signal_sales_service.catalog.controller;

import br.com.signal.signal_sales_service.catalog.dto.response.CatalogResponse;
import br.com.signal.signal_sales_service.catalog.hateoas.CatalogModelAssembler;
import br.com.signal.signal_sales_service.sync.dto.request.CatalogSyncRequest;
import br.com.signal.signal_sales_service.sync.dto.response.CatalogSyncResponse;
import br.com.signal.signal_sales_service.catalog.service.CatalogService;
import br.com.signal.signal_sales_service.sync.service.CatalogSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final CatalogSyncService catalogSyncService;
    private final CatalogModelAssembler catalogModelAssembler;

    @GetMapping("/me")
    public ResponseEntity<CatalogResponse> findMyCatalog(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                catalogService.findMyCatalog(authorization)
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<CatalogResponse> findCatalogByStore(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
                catalogService.findCatalogByStore(storeId)
        );
    }

    @GetMapping("/store/{storeId}/resource")
    public ResponseEntity<EntityModel<CatalogResponse>> findCatalogByStoreResource(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
                catalogModelAssembler.toModel(catalogService.findCatalogByStore(storeId))
        );
    }

    @PostMapping("/sync")
    public ResponseEntity<CatalogSyncResponse> syncCatalog(
            @RequestBody @Valid CatalogSyncRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                catalogSyncService.syncCatalog(request, authorization)
        );
    }
}
