package br.com.signal.signal_sales_service.controller;

import br.com.signal.signal_sales_service.dto.CatalogResponse;
import br.com.signal.signal_sales_service.dto.CatalogSyncRequest;
import br.com.signal.signal_sales_service.dto.CatalogSyncResponse;
import br.com.signal.signal_sales_service.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<CatalogResponse> findCatalogByStore(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
                catalogService.findCatalogByStore(storeId)
        );
    }

    @PostMapping("/sync")
    public ResponseEntity<CatalogSyncResponse> syncCatalog(
            @RequestBody @Valid CatalogSyncRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                catalogService.syncCatalog(request, authorization)
        );
    }
}