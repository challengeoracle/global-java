package br.com.signal.signal_sales_service.controller;

import br.com.signal.signal_sales_service.dto.CreateOrderRequest;
import br.com.signal.signal_sales_service.dto.OrderResponse;
import br.com.signal.signal_sales_service.dto.OrderSyncRequest;
import br.com.signal.signal_sales_service.dto.OrderSyncResponse;
import br.com.signal.signal_sales_service.service.OrderService;
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
                orderService.syncOfflineOrders(request, authorization)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                orderService.findById(id)
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<OrderResponse>> findByStore(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
                orderService.findByStore(storeId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> findByCustomer(
            @PathVariable UUID customerId
    ) {
        return ResponseEntity.ok(
                orderService.findByCustomer(customerId)
        );
    }
}