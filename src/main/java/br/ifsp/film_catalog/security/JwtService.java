package br.ifsp.film_catalog.security;

import java.time.Instant;

import br.ifsp.film_catalog.model.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder encoder) {
        this.jwtEncoder = encoder;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        long expire = 3600L;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expire))
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
