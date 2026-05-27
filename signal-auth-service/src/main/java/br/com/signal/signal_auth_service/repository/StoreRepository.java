package br.com.signal.signal_auth_service.repository;

import br.com.signal.signal_auth_service.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findBySeller_Id(UUID sellerId);
}