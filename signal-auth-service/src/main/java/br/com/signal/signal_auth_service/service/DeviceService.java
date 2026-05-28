package br.com.signal.signal_auth_service.service;

import br.com.signal.signal_auth_service.config.OfflineProperties;
import br.com.signal.signal_auth_service.dto.DeviceStatusResponse;
import br.com.signal.signal_auth_service.dto.OfflineActivationResponse;
import br.com.signal.signal_auth_service.dto.UpdateDeviceRequest;
import br.com.signal.signal_auth_service.entity.Device;
import br.com.signal.signal_auth_service.entity.User;
import br.com.signal.signal_auth_service.entity.UserRole;
import br.com.signal.signal_auth_service.exception.BadRequestException;
import br.com.signal.signal_auth_service.exception.ForbiddenException;
import br.com.signal.signal_auth_service.exception.UnauthorizedException;
import br.com.signal.signal_auth_service.repository.DeviceRepository;
import br.com.signal.signal_auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * @deprecated Legacy device/offline-token management. Auth identity flows use JWT + /auth/me.
 *             Offline activation is not required for sales sync.
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final OfflineProperties offlineProperties;

    public OfflineActivationResponse activateOfflineMode(String userEmail) {
        User user = findSellerByEmail(userEmail);
        Device device = findOrCreateDevice(user);

        device.setOfflineToken(UUID.randomUUID().toString());
        device.setOfflineExpiresAt(generateExpirationDate());
        device.setActive(true);

        deviceRepository.save(device);

        return buildOfflineActivationResponse(device);
    }

    public DeviceStatusResponse getMyDevice(String userEmail) {
        User user = findSellerByEmail(userEmail);

        return deviceRepository.findByUser_Id(user.getId())
                .map(this::buildDeviceStatusResponse)
                .orElseGet(() -> DeviceStatusResponse.builder()
                        .deviceId(null)
                        .active(false)
                        .offlineEnabled(false)
                        .expired(true)
                        .offlineExpiresAt(null)
                        .build());
    }

    public DeviceStatusResponse updateMyDevice(
            String userEmail,
            UpdateDeviceRequest request
    ) {
        User user = findSellerByEmail(userEmail);
        String deviceId = request.getDeviceId().trim();

        Device device = deviceRepository.findByUser_Id(user.getId())
                .orElseGet(() -> Device.builder()
                        .user(user)
                        .offlineToken(null)
                        .offlineExpiresAt(null)
                        .active(true)
                        .build());

        String currentDeviceId = device.getDeviceId();

        boolean deviceIdChanged = currentDeviceId == null || !deviceId.equals(currentDeviceId);

        if (deviceIdChanged && deviceRepository.existsByDeviceId(deviceId)) {
            throw new BadRequestException("Device already registered");
        }

        device.setDeviceId(deviceId);
        device.setOfflineToken(null);
        device.setOfflineExpiresAt(null);
        device.setActive(true);

        deviceRepository.save(device);

        return buildDeviceStatusResponse(device);
    }

    private User findSellerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid token"));

        if (user.getRole() != UserRole.SELLER) {
            throw new ForbiddenException("Only sellers can access device resources");
        }

        return user;
    }

    private Device findOrCreateDevice(User user) {
        Optional<Device> existing = deviceRepository.findByUser_Id(user.getId());

        if (existing.isPresent()) {
            return existing.get();
        }

        throw new BadRequestException(
                "Device id is not registered. Use PATCH /device/me to register a device first."
        );
    }

    private LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusHours(
                offlineProperties.getSessionExpirationHours()
        );
    }

    private OfflineActivationResponse buildOfflineActivationResponse(Device device) {
        return OfflineActivationResponse.builder()
                .deviceId(device.getDeviceId())
                .offlineToken(device.getOfflineToken())
                .offlineExpiresAt(device.getOfflineExpiresAt())
                .active(device.getActive())
                .build();
    }

    private DeviceStatusResponse buildDeviceStatusResponse(Device device) {
        boolean expired = device.getOfflineExpiresAt() == null
                || device.getOfflineExpiresAt().isBefore(LocalDateTime.now());

        boolean offlineEnabled = Boolean.TRUE.equals(device.getActive())
                && device.getOfflineToken() != null
                && !expired;

        return DeviceStatusResponse.builder()
                .deviceId(device.getDeviceId())
                .active(device.getActive())
                .offlineEnabled(offlineEnabled)
                .expired(expired)
                .offlineExpiresAt(device.getOfflineExpiresAt())
                .build();
    }
}
