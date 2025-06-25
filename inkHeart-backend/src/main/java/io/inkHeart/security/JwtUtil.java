package io.inkHeart.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hr
    private final SecretKey key;
    public JwtUtil(@Value("${JWT_SECRET}") String jwtSecret) {
        byte[] decodedKey = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }
    // Generate JWT token with subject email
    public String generateToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(EXPIRATION_TIME)))
                .signWith(key)
                .compact();
    }

    // Validate the token signature and expiration
    public boolean validateToken(String token) {
        try {
            getJwtParser().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extract the subject(email) from the token
    public String extractUserName(String token) {
        return getJwtParser().parseSignedClaims(token).getPayload().getSubject();
    }

    private JwtParser getJwtParser() {
        return Jwts.parser().verifyWith(key).build();
    }
}
