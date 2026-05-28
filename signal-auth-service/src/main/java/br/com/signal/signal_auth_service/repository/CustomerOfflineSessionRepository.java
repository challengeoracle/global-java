package br.com.signal.signal_auth_service.repository;

import br.com.signal.signal_auth_service.entity.CustomerOfflineSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerOfflineSessionRepository extends JpaRepository<CustomerOfflineSession, UUID> {

    Optional<CustomerOfflineSession> findByUser_Id(UUID userId);

    boolean existsBySessionToken(String sessionToken);
}