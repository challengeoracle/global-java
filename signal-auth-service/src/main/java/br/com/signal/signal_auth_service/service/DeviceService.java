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
import br.com.signal.signal_auth_service.exception.NotFoundException;
import br.com.signal.signal_auth_service.exception.UnauthorizedException;
import br.com.signal.signal_auth_service.repository.DeviceRepository;
import br.com.signal.signal_auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final OfflineProperties offlineProperties;

    public OfflineActivationResponse activateOfflineMode(String userEmail) {
        User user = findSellerByEmail(userEmail);
        Device device = findDeviceByUser(user);

        device.setOfflineToken(UUID.randomUUID().toString());
        device.setOfflineExpiresAt(generateExpirationDate());
        device.setActive(true);

        deviceRepository.save(device);

        return buildOfflineActivationResponse(device);
    }

    public DeviceStatusResponse getMyDevice(String userEmail) {
        User user = findSellerByEmail(userEmail);
        Device device = findDeviceByUser(user);

        return buildDeviceStatusResponse(device);
    }

    public DeviceStatusResponse updateMyDevice(
            String userEmail,
            UpdateDeviceRequest request
    ) {
        User user = findSellerByEmail(userEmail);
        Device device = findDeviceByUser(user);

        if (!device.getDeviceId().equals(request.getDeviceId())
                && deviceRepository.existsByDeviceId(request.getDeviceId())) {
            throw new BadRequestException("Device already registered");
        }

        device.setDeviceId(request.getDeviceId());
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

    private Device findDeviceByUser(User user) {
        return deviceRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Device not found"));
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