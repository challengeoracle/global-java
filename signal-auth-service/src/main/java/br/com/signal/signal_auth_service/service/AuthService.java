package br.com.signal.signal_auth_service.service;

import br.com.signal.signal_auth_service.dto.AuthResponse;
import br.com.signal.signal_auth_service.dto.LoginRequest;
import br.com.signal.signal_auth_service.dto.RegisterCustomerRequest;
import br.com.signal.signal_auth_service.dto.RegisterSellerRequest;
import br.com.signal.signal_auth_service.dto.UserResponse;
import br.com.signal.signal_auth_service.entity.Device;
import br.com.signal.signal_auth_service.entity.Store;
import br.com.signal.signal_auth_service.entity.User;
import br.com.signal.signal_auth_service.entity.UserRole;
import br.com.signal.signal_auth_service.exception.BadRequestException;
import br.com.signal.signal_auth_service.exception.UnauthorizedException;
import br.com.signal.signal_auth_service.repository.DeviceRepository;
import br.com.signal.signal_auth_service.repository.StoreRepository;
import br.com.signal.signal_auth_service.repository.UserRepository;
import br.com.signal.signal_auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final DeviceRepository deviceRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse registerSeller(RegisterSellerRequest request) {

        validateUserCreation(request.getEmail(), request.getCpf());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(request.getCpf())
                .phone(request.getPhone())
                .role(UserRole.SELLER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Store store = Store.builder()
                .name(request.getStoreName())
                .category(request.getStoreCategory())
                .seller(user)
                .createdAt(LocalDateTime.now())
                .build();

        storeRepository.save(store);

        String offlineToken = UUID.randomUUID().toString();

        Device device = Device.builder()
                .deviceId(request.getDeviceId())
                .offlineToken(offlineToken)
                .offlineExpiresAt(LocalDateTime.now().plusHours(8))
                .active(true)
                .user(user)
                .build();

        deviceRepository.save(device);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(buildUserResponse(user, Optional.of(store)))
                .build();
    }

    public AuthResponse registerCustomer(RegisterCustomerRequest request) {

        validateUserCreation(request.getEmail(), request.getCpf());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(request.getCpf())
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(buildUserResponse(user, Optional.empty()))
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Optional<Store> store = Optional.empty();

        if (user.getRole() == UserRole.SELLER) {
            store = storeRepository.findBySeller_Id(user.getId());
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(buildUserResponse(user, store))
                .build();
    }

    public UserResponse me(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid token"));

        Optional<Store> store = Optional.empty();

        if (user.getRole() == UserRole.SELLER) {
            store = storeRepository.findBySeller_Id(user.getId());
        }

        return buildUserResponse(user, store);
    }

    private void validateUserCreation(String email, String cpf) {

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        if (userRepository.existsByCpf(cpf)) {
            throw new BadRequestException("CPF already registered");
        }
    }

    private UserResponse buildUserResponse(
            User user,
            Optional<Store> store
    ) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .cpf(user.getCpf())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .storeName(store.map(Store::getName).orElse(null))
                .build();
    }
}