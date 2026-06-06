package br.com.signal.signal_auth_service.controller;

import br.com.signal.signal_auth_service.dto.AuthResponse;
import br.com.signal.signal_auth_service.dto.LoginRequest;
import br.com.signal.signal_auth_service.dto.RegisterCustomerRequest;
import br.com.signal.signal_auth_service.dto.RegisterSellerRequest;
import br.com.signal.signal_auth_service.dto.StoreResponse;
import br.com.signal.signal_auth_service.dto.UserResponse;
import br.com.signal.signal_auth_service.hateoas.UserModelAssembler;
import br.com.signal.signal_auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de cadastro, login e consulta do perfil autenticado.")
public class AuthController {

    private final AuthService authService;
    private final UserModelAssembler userModelAssembler;

    @PostMapping("/register/seller")
    @Operation(summary = "Cadastrar vendedor", description = "Cria um novo vendedor e a loja vinculada, retornando o JWT inicial.")
    public ResponseEntity<AuthResponse> registerSeller(
            @Valid @RequestBody RegisterSellerRequest request
    ) {
        return ResponseEntity.ok(authService.registerSeller(request));
    }

    @PostMapping("/register/customer")
    @Operation(summary = "Cadastrar cliente", description = "Cria um novo cliente e retorna o JWT inicial.")
    public ResponseEntity<AuthResponse> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request
    ) {
        return ResponseEntity.ok(authService.registerCustomer(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica um usuario existente e retorna um novo JWT.")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar perfil autenticado", description = "Retorna os dados do usuario autenticado a partir do JWT enviado.")
    public ResponseEntity<EntityModel<UserResponse>> me(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse response = authService.me(userDetails.getUsername());

        return ResponseEntity.ok(userModelAssembler.toModel(response));
    }

    @GetMapping("/stores/{storeId}")
    @Operation(summary = "Consultar loja por id", description = "Retorna os dados basicos de uma loja a partir do id.")
    public ResponseEntity<StoreResponse> findStoreById(
            @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(authService.findStoreById(storeId));
    }
}
