package br.com.signal.signal_auth_service;

import br.com.signal.signal_auth_service.config.JwtProperties;
import br.com.signal.signal_auth_service.config.OfflineProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, OfflineProperties.class})
public class SignalAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignalAuthServiceApplication.class, args);
	}
}