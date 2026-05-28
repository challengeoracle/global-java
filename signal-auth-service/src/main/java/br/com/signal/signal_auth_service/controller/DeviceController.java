package br.com.signal.signal_auth_service.controller;

import br.com.signal.signal_auth_service.dto.DeviceStatusResponse;
import br.com.signal.signal_auth_service.dto.OfflineActivationResponse;
import br.com.signal.signal_auth_service.dto.UpdateDeviceRequest;
import br.com.signal.signal_auth_service.service.DeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @deprecated Legacy device/offline-token endpoints. Prefer JWT identity via /auth/me.
 *             Sales sync does not require offline activation.
 */
@Deprecated
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
@Tag(name = "Device (legacy)", description = "Legacy offline device management. Not required for sales.")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/me")
    public ResponseEntity<DeviceStatusResponse> getMyDevice(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                deviceService.getMyDevice(userDetails.getUsername())
        );
    }

    @PostMapping("/offline/activate")
    public ResponseEntity<OfflineActivationResponse> activateOfflineMode(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                deviceService.activateOfflineMode(userDetails.getUsername())
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<DeviceStatusResponse> updateMyDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateDeviceRequest request
    ) {
        return ResponseEntity.ok(
                deviceService.updateMyDevice(
                        userDetails.getUsername(),
                        request
                )
        );
    }
}
