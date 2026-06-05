package br.com.signal.signal_sales_service.order.controller;

import br.com.signal.signal_sales_service.order.dto.request.CreateOrderRequest;
import br.com.signal.signal_sales_service.order.dto.response.OrderResponse;
import br.com.signal.signal_sales_service.order.hateoas.OrderModelAssembler;
import br.com.signal.signal_sales_service.order.service.OrderService;
import br.com.signal.signal_sales_service.shared.dto.response.PageResponse;
import br.com.signal.signal_sales_service.sync.dto.request.OrderSyncRequest;
import br.com.signal.signal_sales_service.sync.dto.response.OrderSyncResponse;
import br.com.signal.signal_sales_service.sync.service.OrderSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Criacao, consulta e sincronizacao de pedidos do OffPay.")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final OrderSyncService orderSyncService;
    private final OrderModelAssembler orderModelAssembler;

    @GetMapping("/me")
    @Operation(summary = "Listar meus pedidos", description = "Retorna os pedidos relacionados ao usuario autenticado.")
    public ResponseEntity<List<OrderResponse>> findMyOrders(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(orderService.findMyOrders(authorization));
    }

    @GetMapping("/me/page")
    public ResponseEntity<PageResponse<OrderResponse>> findMyOrdersPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderService.findMyOrdersPage(authorization, page, size));
    }

    @GetMapping("/me/sales")
    @Operation(summary = "Listar minhas vendas", description = "Retorna os pedidos em que o usuario autenticado atua como vendedor.")
    public ResponseEntity<List<OrderResponse>> findMySales(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(orderService.findMySales(authorization));
    }

    @GetMapping("/me/sales/page")
    public ResponseEntity<PageResponse<OrderResponse>> findMySalesPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderService.findMySalesPage(authorization, page, size));
    }

    @GetMapping("/me/purchases")
    @Operation(summary = "Listar minhas compras", description = "Retorna os pedidos em que o usuario autenticado atua como comprador.")
    public ResponseEntity<List<OrderResponse>> findMyPurchases(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(orderService.findMyPurchases(authorization));
    }

    @GetMapping("/me/purchases/page")
    public ResponseEntity<PageResponse<OrderResponse>> findMyPurchasesPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderService.findMyPurchasesPage(authorization, page, size));
    }

    @PostMapping
    @Operation(summary = "Criar pedido online", description = "Cria um novo pedido online usando os dados enviados pelo cliente autenticado.")
    public ResponseEntity<OrderResponse> createOnlineOrder(
            @RequestBody @Valid CreateOrderRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOnlineOrder(request, authorization));
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar pedidos offline", description = "Recebe pedidos criados offline e publica a sincronizacao no backend.")
    public ResponseEntity<OrderSyncResponse> syncOfflineOrders(
            @RequestBody @Valid OrderSyncRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(orderSyncService.syncOfflineOrders(request, authorization));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pedido por id", description = "Retorna os detalhes de um pedido visivel para o usuario autenticado.")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(orderService.findById(id, authorization));
    }

    @GetMapping("/{id}/resource")
    public ResponseEntity<EntityModel<OrderResponse>> findByIdResource(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                orderModelAssembler.toModel(orderService.findById(id, authorization))
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<OrderResponse>> findByStore(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(orderService.findByStore(storeId, authorization));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> findByCustomer(
            @PathVariable UUID customerId,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(orderService.findByCustomer(customerId, authorization));
    }
}
