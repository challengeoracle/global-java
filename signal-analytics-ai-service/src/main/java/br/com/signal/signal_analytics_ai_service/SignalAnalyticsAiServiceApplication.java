package br.com.signal.signal_analytics_ai_service;

import br.com.signal.signal_analytics_ai_service.shared.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class})
@EnableFeignClients
public class SignalAnalyticsAiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignalAnalyticsAiServiceApplication.class, args);
	}
}