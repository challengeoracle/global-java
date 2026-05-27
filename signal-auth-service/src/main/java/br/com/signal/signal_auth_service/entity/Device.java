package br.com.signal.signal_auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_DEVICES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String deviceId;

    @Column(nullable = true)
    private String offlineToken;

    @Column(nullable = true)
    private LocalDateTime offlineExpiresAt;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
}