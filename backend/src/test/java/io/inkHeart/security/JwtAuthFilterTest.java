package io.inkHeart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthFilterTest {
    private JwtAuthFilter jwtAuthFilter;
    private JwtUtil jwtUtil;
    private String validToken;
    private final String email = "test@exmaple.com";

    @BeforeEach
    void setup() {
        String secret = "JPBXkgSltOTzTiOGLoxvJUS9qbg8Lj7MGcOdvU4hclg=";
        jwtUtil = new JwtUtil(secret);
        jwtAuthFilter = new JwtAuthFilter(jwtUtil);
        validToken = jwtUtil.generateToken(email);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAuthenticationSuccessWhenTokenIsValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        var response = new MockHttpServletResponse();
        var filterChain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);
    }

    @Test
    void testAuthenticationIsNotSetWhenTokenIsMissing() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var filterChain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testAuthenticationIsNotSetWhenTokenIsInvalid() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + "Invalid token");
        var response = new MockHttpServletResponse();
        var filterChain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    }
}