package br.com.signal.signal_auth_service.controller;

import br.com.signal.signal_auth_service.dto.AuthResponse;
import br.com.signal.signal_auth_service.dto.LoginRequest;
import br.com.signal.signal_auth_service.dto.RegisterCustomerRequest;
import br.com.signal.signal_auth_service.dto.RegisterSellerRequest;
import br.com.signal.signal_auth_service.dto.UserResponse;
import br.com.signal.signal_auth_service.hateoas.UserModelAssembler;
import br.com.signal.signal_auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserModelAssembler userModelAssembler;

    @PostMapping("/register/seller")
    public ResponseEntity<AuthResponse> registerSeller(
            @Valid @RequestBody RegisterSellerRequest request
    ) {
        return ResponseEntity.ok(authService.registerSeller(request));
    }

    @PostMapping("/register/customer")
    public ResponseEntity<AuthResponse> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request
    ) {
        return ResponseEntity.ok(authService.registerCustomer(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<EntityModel<UserResponse>> me(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse response = authService.me(userDetails.getUsername());

        return ResponseEntity.ok(userModelAssembler.toModel(response));
    }
}