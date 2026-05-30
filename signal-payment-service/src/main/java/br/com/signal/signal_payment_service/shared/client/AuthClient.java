package br.com.signal.signal_payment_service.shared.client;

import br.com.signal.signal_payment_service.shared.dto.response.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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
}