package br.com.signal.signal_auth_service.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;

    private String name;

    private String email;

    private String cpf;

    private String phone;

    private String role;

    private String storeName;
}