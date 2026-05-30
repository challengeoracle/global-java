package br.com.signal.signal_payment_service.shared.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserResponse {

    private UUID id;
    private String name;
    private String email;
    private String cpf;
    private String phone;
    private String role;
    private UUID storeId;
    private String storeName;
    private String deviceId;

    public boolean isSeller() {
        return "SELLER".equals(role);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }
}