package br.com.signal.signal_sales_service.shared.service;

import br.com.signal.signal_sales_service.shared.client.AuthClient;
import br.com.signal.signal_sales_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_sales_service.shared.exception.BadRequestException;
import br.com.signal.signal_sales_service.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthIdentityService {

    private final AuthClient authClient;

    public AuthUserResponse me(String authorization) {
        return authClient.me(authorization);
    }

    public AuthUserResponse requireSeller(String authorization) {
        AuthUserResponse authUser = me(authorization);

        if (!"SELLER".equals(authUser.getRole())) {
            throw new ForbiddenException("Only sellers can perform this action");
        }

        if (authUser.getStoreId() == null) {
            throw new BadRequestException("Seller does not have a store");
        }

        return authUser;
    }

    public AuthUserResponse requireCustomer(String authorization) {
        AuthUserResponse authUser = me(authorization);

        if (!"CUSTOMER".equals(authUser.getRole())) {
            throw new ForbiddenException("Only customers can perform this action");
        }

        return authUser;
    }
}
