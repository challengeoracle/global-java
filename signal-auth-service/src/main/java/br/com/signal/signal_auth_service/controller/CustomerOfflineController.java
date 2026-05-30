package br.com.signal.signal_auth_service.controller;

import br.com.signal.signal_auth_service.dto.CustomerOfflineActivationResponse;
import br.com.signal.signal_auth_service.dto.CustomerOfflineStatusResponse;
import br.com.signal.signal_auth_service.service.CustomerOfflineService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @deprecated Legacy customer offline session endpoints. Prefer JWT identity via /auth/me.
 *             Sales operations do not require offline session activation.
 */
@Deprecated
@RestController
@RequestMapping("/customer/offline")
@RequiredArgsConstructor
@Tag(name = "Customer Offline (legacy)", description = "Legacy offline session tokens. Not required for sales.")
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
