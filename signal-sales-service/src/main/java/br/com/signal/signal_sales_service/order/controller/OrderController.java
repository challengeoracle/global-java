package br.com.signal.signal_sales_service.order.controller;

import br.com.signal.signal_sales_service.order.dto.request.CreateOrderRequest;
import br.com.signal.signal_sales_service.order.dto.response.OrderResponse;
import br.com.signal.signal_sales_service.sync.dto.request.OrderSyncRequest;
import br.com.signal.signal_sales_service.sync.dto.response.OrderSyncResponse;
import br.com.signal.signal_sales_service.order.service.OrderService;
import br.com.signal.signal_sales_service.sync.service.OrderSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderSyncService orderSyncService;

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> findMyOrders(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                orderService.findMyOrders(authorization)
        );
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOnlineOrder(
            @RequestBody @Valid CreateOrderRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOnlineOrder(request, authorization));
    }

    @PostMapping("/sync")
    public ResponseEntity<OrderSyncResponse> syncOfflineOrders(
            @RequestBody @Valid OrderSyncRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                orderSyncService.syncOfflineOrders(request, authorization)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                orderService.findById(id, authorization)
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<OrderResponse>> findByStore(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                orderService.findByStore(storeId, authorization)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> findByCustomer(
            @PathVariable UUID customerId,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                orderService.findByCustomer(customerId, authorization)
        );
    }
}