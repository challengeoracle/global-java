package br.com.signal.signal_sales_service;

import br.com.signal.signal_sales_service.shared.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class})
@EnableFeignClients
public class SignalSalesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignalSalesServiceApplication.class, args);
	}
}