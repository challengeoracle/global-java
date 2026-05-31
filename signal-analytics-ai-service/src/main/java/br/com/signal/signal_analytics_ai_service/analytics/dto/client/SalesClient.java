package br.com.signal.signal_analytics_ai_service.analytics.client;

import br.com.signal.signal_analytics_ai_service.analytics.dto.client.OrderClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
        name = "sales-service",
        url = "${services.sales.url}"
)
public interface SalesClient {

    @GetMapping("/order/me")
    List<OrderClientResponse> getMyOrders(
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/order/me/sales")
    List<OrderClientResponse> getMySales(
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/order/me/purchases")
    List<OrderClientResponse> getMyPurchases(
            @RequestHeader("Authorization") String authorization
    );
}