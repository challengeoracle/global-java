package br.com.signal.signal_payment_service.shared.service;

import br.com.signal.signal_payment_service.shared.client.AuthClient;
import br.com.signal.signal_payment_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_payment_service.shared.exception.BadRequestException;
import br.com.signal.signal_payment_service.shared.exception.ForbiddenException;
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

        if (!authUser.isSeller()) {
            throw new ForbiddenException("Only sellers can perform this action");
        }

        if (authUser.getStoreId() == null) {
            throw new BadRequestException("Seller does not have a store");
        }

        return authUser;
    }

    public AuthUserResponse requireCustomer(String authorization) {
        AuthUserResponse authUser = me(authorization);

        if (!authUser.isCustomer()) {
            throw new ForbiddenException("Only customers can perform this action");
        }

        return authUser;
    }

    public AuthUserResponse requireCustomerOrSeller(String authorization) {
        AuthUserResponse authUser = me(authorization);

        if (!authUser.isCustomer() && !authUser.isSeller()) {
            throw new ForbiddenException("Invalid user role");
        }

        return authUser;
    }
}