package br.com.signal.signal_payment_service;

import br.com.signal.signal_payment_service.shared.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class})
@EnableFeignClients
@EnableScheduling
public class SignalPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignalPaymentServiceApplication.class, args);
	}
}
