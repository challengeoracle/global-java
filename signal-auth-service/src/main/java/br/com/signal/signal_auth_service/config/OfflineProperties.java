package br.com.signal.signal_auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "offline")
public class OfflineProperties {

    private Long sessionExpirationHours;
}