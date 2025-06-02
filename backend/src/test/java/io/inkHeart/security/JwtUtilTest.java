package io.inkHeart.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup(){
        String secret = "JPBXkgSltOTzTiOGLoxvJUS9qbg8Lj7MGcOdvU4hclg=";
        this.jwtUtil = new JwtUtil(secret);
    }

    @Test
    void testGenerateAndValidateToken() {
        String email = "user@example.com";
        String token = jwtUtil.generateToken(email);

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUserName(token).equals(email));
    }

    @Test
    void testReturnFalseForInvalidToken() {
        String invalidToken = "invalid token";
        assertThat(jwtUtil.validateToken(invalidToken)).isFalse();
    }
}