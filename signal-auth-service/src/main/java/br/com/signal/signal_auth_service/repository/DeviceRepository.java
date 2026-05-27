package br.com.signal.signal_auth_service.repository;

import br.com.signal.signal_auth_service.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Optional<Device> findByDeviceId(String deviceId);

    Optional<Device> findByUser_Id(UUID userId);

    boolean existsByDeviceId(String deviceId);
}