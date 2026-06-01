package br.com.signal.signal_analytics_ai_service.shared.service;

import br.com.signal.signal_analytics_ai_service.shared.client.AuthClient;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_analytics_ai_service.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthIdentityService {

    private final AuthClient authClient;

    public AuthUserResponse me(String authorization) {
        return authClient.me(authorization);
    }

    public AuthUserResponse requireCustomerOrSeller(String authorization) {
        AuthUserResponse authUser = me(authorization);

        if (!authUser.isCustomer() && !authUser.isSeller()) {
            throw new ForbiddenException("Invalid user role");
        }

        return authUser;
    }

    public AuthUserResponse requireSeller(String authorization) {
        AuthUserResponse authUser = me(authorization);

        if (!authUser.isSeller()) {
            throw new ForbiddenException("Only sellers can access this resource");
        }

        return authUser;
    }

    public AuthUserResponse requireCustomer(String authorization) {
        AuthUserResponse authUser = me(authorization);

        if (!authUser.isCustomer()) {
            throw new ForbiddenException("Only customers can access this resource");
        }

        return authUser;
    }
}