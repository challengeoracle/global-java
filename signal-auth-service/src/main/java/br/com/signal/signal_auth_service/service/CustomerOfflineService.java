package br.com.signal.signal_auth_service.service;

import br.com.signal.signal_auth_service.config.OfflineProperties;
import br.com.signal.signal_auth_service.dto.CustomerOfflineActivationResponse;
import br.com.signal.signal_auth_service.dto.CustomerOfflineStatusResponse;
import br.com.signal.signal_auth_service.entity.CustomerOfflineSession;
import br.com.signal.signal_auth_service.entity.User;
import br.com.signal.signal_auth_service.entity.UserRole;
import br.com.signal.signal_auth_service.exception.ForbiddenException;
import br.com.signal.signal_auth_service.exception.NotFoundException;
import br.com.signal.signal_auth_service.exception.UnauthorizedException;
import br.com.signal.signal_auth_service.repository.CustomerOfflineSessionRepository;
import br.com.signal.signal_auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @deprecated Legacy customer offline session tokens. Auth identity flows use JWT + /auth/me.
 *             Offline session activation is not required for sales operations.
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class CustomerOfflineService {

    private final UserRepository userRepository;
    private final CustomerOfflineSessionRepository customerOfflineSessionRepository;
    private final OfflineProperties offlineProperties;

    public CustomerOfflineActivationResponse activateOfflineSession(
            String userEmail
    ) {
        User customer = findCustomerByEmail(userEmail);

        CustomerOfflineSession session = customerOfflineSessionRepository
                .findByUser_Id(customer.getId())
                .orElseGet(() -> CustomerOfflineSession.builder()
                        .user(customer)
                        .createdAt(LocalDateTime.now())
                        .build()
                );

        session.setSessionToken(UUID.randomUUID().toString());
        session.setExpiresAt(generateExpirationDate());
        session.setActive(true);

        customerOfflineSessionRepository.save(session);

        return CustomerOfflineActivationResponse.builder()
                .sessionToken(session.getSessionToken())
                .expiresAt(session.getExpiresAt())
                .active(session.getActive())
                .build();
    }

    public CustomerOfflineStatusResponse getOfflineSessionStatus(
            String userEmail
    ) {
        User customer = findCustomerByEmail(userEmail);

        CustomerOfflineSession session = customerOfflineSessionRepository
                .findByUser_Id(customer.getId())
                .orElseThrow(() ->
                        new NotFoundException("Customer offline session not found")
                );

        return buildStatusResponse(session);
    }

    private User findCustomerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid token")
                );

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ForbiddenException(
                    "Only customers can access customer offline resources"
            );
        }

        return user;
    }

    private LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusHours(
                offlineProperties.getSessionExpirationHours()
        );
    }

    private CustomerOfflineStatusResponse buildStatusResponse(
            CustomerOfflineSession session
    ) {
        boolean expired = session.getExpiresAt()
                .isBefore(LocalDateTime.now());

        boolean offlineEnabled = Boolean.TRUE.equals(session.getActive())
                && !expired;

        return CustomerOfflineStatusResponse.builder()
                .active(session.getActive())
                .offlineEnabled(offlineEnabled)
                .expired(expired)
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
