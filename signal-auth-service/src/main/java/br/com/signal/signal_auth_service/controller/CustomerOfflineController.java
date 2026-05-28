package br.com.signal.signal_auth_service.controller;

import br.com.signal.signal_auth_service.dto.CustomerOfflineActivationResponse;
import br.com.signal.signal_auth_service.dto.CustomerOfflineStatusResponse;
import br.com.signal.signal_auth_service.service.CustomerOfflineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/offline")
@RequiredArgsConstructor
public class CustomerOfflineController {

    private final CustomerOfflineService customerOfflineService;

    @PostMapping("/activate")
    public ResponseEntity<CustomerOfflineActivationResponse> activateOfflineSession(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                customerOfflineService.activateOfflineSession(
                        userDetails.getUsername()
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerOfflineStatusResponse> getOfflineSessionStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                customerOfflineService.getOfflineSessionStatus(
                        userDetails.getUsername()
                )
        );
    }
}