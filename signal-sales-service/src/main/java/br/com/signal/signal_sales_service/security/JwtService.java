package br.com.signal.signal_sales_service.security;

import br.com.signal.signal_sales_service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public AuthUser extractAuthUser(String token) {
        Claims claims = extractAllClaims(token);

        validateExpiration(claims);

        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        String userId = claims.get("userId", String.class);

        if (email == null || email.isBlank()) {
            throw new JwtException("Invalid token subject");
        }

        if (role == null || role.isBlank()) {
            throw new JwtException("Invalid token role");
        }

        if (userId == null || userId.isBlank()) {
            throw new JwtException("Invalid token userId");
        }

        Collection<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new AuthUser(
                UUID.fromString(userId),
                email,
                role,
                authorities
        );
    }

    public boolean isTokenValid(String token) {
        Claims claims = extractAllClaims(token);
        return !isTokenExpired(claims);
    }

    private void validateExpiration(Claims claims) {
        if (isTokenExpired(claims)) {
            throw new JwtException("Token expired");
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                jwtProperties.getSecret()
        );

        return Keys.hmacShaKeyFor(keyBytes);
    }
}