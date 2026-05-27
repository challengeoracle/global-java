package br.com.signal.signal_auth_service.service;

import br.com.signal.signal_auth_service.config.OfflineProperties;
import br.com.signal.signal_auth_service.dto.DeviceStatusResponse;
import br.com.signal.signal_auth_service.dto.OfflineActivationResponse;
import br.com.signal.signal_auth_service.entity.Device;
import br.com.signal.signal_auth_service.entity.User;
import br.com.signal.signal_auth_service.entity.UserRole;
import br.com.signal.signal_auth_service.exception.ForbiddenException;
import br.com.signal.signal_auth_service.exception.NotFoundException;
import br.com.signal.signal_auth_service.exception.UnauthorizedException;
import br.com.signal.signal_auth_service.repository.DeviceRepository;
import br.com.signal.signal_auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final OfflineProperties offlineProperties;

    public OfflineActivationResponse activateOfflineMode(
            String userEmail,
            String deviceId
    ) {
        User user = findSellerByEmail(userEmail);

        Device device = deviceRepository
                .findByUser_IdAndDeviceId(user.getId(), deviceId)
                .orElseGet(() -> Device.builder()
                        .deviceId(deviceId)
                        .user(user)
                        .active(true)
                        .build()
                );

        device.setOfflineToken(UUID.randomUUID().toString());
        device.setOfflineExpiresAt(generateExpirationDate());
        device.setActive(true);

        deviceRepository.save(device);

        return buildOfflineActivationResponse(device);
    }

    public OfflineActivationResponse renewOfflineMode(
            String userEmail,
            String deviceId
    ) {
        User user = findSellerByEmail(userEmail);

        Device device = deviceRepository
                .findByUser_IdAndDeviceId(user.getId(), deviceId)
                .orElseThrow(() ->
                        new NotFoundException("Device not found")
                );

        if (!device.getActive()) {
            throw new ForbiddenException("Device is not active");
        }

        device.setOfflineToken(UUID.randomUUID().toString());
        device.setOfflineExpiresAt(generateExpirationDate());

        deviceRepository.save(device);

        return buildOfflineActivationResponse(device);
    }

    public DeviceStatusResponse getDeviceStatus(
            String userEmail,
            String deviceId
    ) {
        User user = findSellerByEmail(userEmail);

        Device device = deviceRepository
                .findByUser_IdAndDeviceId(user.getId(), deviceId)
                .orElseThrow(() ->
                        new NotFoundException("Device not found")
                );

        return buildDeviceStatusResponse(device);
    }

    public List<DeviceStatusResponse> getDevices(
            String userEmail
    ) {
        User user = findSellerByEmail(userEmail);

        return deviceRepository.findAllByUser_Id(user.getId())
                .stream()
                .map(this::buildDeviceStatusResponse)
                .toList();
    }

    private User findSellerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid token")
                );

        if (user.getRole() != UserRole.SELLER) {
            throw new ForbiddenException(
                    "Only sellers can access device resources"
            );
        }

        return user;
    }

    private LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusHours(
                offlineProperties.getSessionExpirationHours()
        );
    }

    private OfflineActivationResponse buildOfflineActivationResponse(
            Device device
    ) {
        return OfflineActivationResponse.builder()
                .deviceId(device.getDeviceId())
                .offlineToken(device.getOfflineToken())
                .offlineExpiresAt(device.getOfflineExpiresAt())
                .active(device.getActive())
                .build();
    }

    private DeviceStatusResponse buildDeviceStatusResponse(
            Device device
    ) {
        boolean expired = device.getOfflineExpiresAt() == null
                || device.getOfflineExpiresAt().isBefore(LocalDateTime.now());

        boolean offlineEnabled = Boolean.TRUE.equals(device.getActive())
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