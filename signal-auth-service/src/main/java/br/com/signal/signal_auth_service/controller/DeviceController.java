package br.com.signal.signal_auth_service.controller;

import br.com.signal.signal_auth_service.dto.DeviceStatusResponse;
import br.com.signal.signal_auth_service.dto.OfflineActivationResponse;
import br.com.signal.signal_auth_service.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/{deviceId}/offline/activate")
    public ResponseEntity<OfflineActivationResponse> activateOfflineMode(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String deviceId
    ) {
        return ResponseEntity.ok(
                deviceService.activateOfflineMode(
                        userDetails.getUsername(),
                        deviceId
                )
        );
    }

    @PostMapping("/{deviceId}/offline/renew")
    public ResponseEntity<OfflineActivationResponse> renewOfflineMode(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String deviceId
    ) {
        return ResponseEntity.ok(
                deviceService.renewOfflineMode(
                        userDetails.getUsername(),
                        deviceId
                )
        );
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceStatusResponse> getDeviceStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String deviceId
    ) {
        return ResponseEntity.ok(
                deviceService.getDeviceStatus(
                        userDetails.getUsername(),
                        deviceId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<DeviceStatusResponse>> getDevices(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                deviceService.getDevices(
                        userDetails.getUsername()
                )
        );
    }
}