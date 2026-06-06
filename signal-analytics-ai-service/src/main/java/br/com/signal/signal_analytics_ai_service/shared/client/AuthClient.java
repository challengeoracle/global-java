package br.com.signal.signal_analytics_ai_service.shared.client;

import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.StoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "auth-service",
        url = "${auth.service-url}"
)
public interface AuthClient {

    @GetMapping("/auth/me")
    AuthUserResponse me(
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/auth/stores/{storeId}")
    StoreResponse findStoreById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("storeId") String storeId
    );
}
