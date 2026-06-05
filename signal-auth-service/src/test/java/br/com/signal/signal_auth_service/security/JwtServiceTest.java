package br.com.signal.signal_auth_service.security;

import br.com.signal.signal_auth_service.config.JwtProperties;
import br.com.signal.signal_auth_service.entity.User;
import br.com.signal.signal_auth_service.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("Qm9hU29ydGVDb21PcHJvamV0b0dsb2JhbEZJQVAtMjAyNg==");
        properties.setExpirationMinutes(120L);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateTokenShouldEmbedUsername() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("seller@offpay.com")
                .role(UserRole.SELLER)
                .build();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("seller@offpay.com");
    }

    @Test
    void isTokenValidShouldReturnTrueForMatchingUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("customer@offpay.com")
                .role(UserRole.CUSTOMER)
                .build();

        String token = jwtService.generateToken(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("customer@offpay.com")
                .password("irrelevant")
                .roles("CUSTOMER")
                .build();

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }
}
