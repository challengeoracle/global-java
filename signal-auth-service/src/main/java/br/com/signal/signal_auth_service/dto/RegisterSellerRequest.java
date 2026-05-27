package br.com.signal.signal_auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterSellerRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String cpf;

    @NotBlank
    private String phone;

    @NotBlank
    private String storeName;

    @NotBlank
    private String storeCategory;

    @NotBlank
    private String deviceId;
}