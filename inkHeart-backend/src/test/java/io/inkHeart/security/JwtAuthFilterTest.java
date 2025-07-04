package io.inkHeart.security;

import io.inkHeart.entity.CustomUserDetails;
import io.inkHeart.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {
    private JwtAuthFilter jwtAuthFilter;
    @Mock
    private UserDetailsService userService;
    private JwtUtil jwtUtil;
    private String validToken;
    private final String email = "test@example.com";

    @BeforeEach
    void setup() {
        String secret = "JPBXkgSltOTzTiOGLoxvJUS9qbg8Lj7MGcOdvU4hclg=";
        jwtUtil = new JwtUtil(secret);
        jwtAuthFilter = new JwtAuthFilter(jwtUtil, userService);
        validToken = jwtUtil.generateToken(email);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAuthenticationSuccessWhenTokenIsValid() throws ServletException, IOException {
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setId(1L);
        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);
        Mockito.when(userService.loadUserByUsername(email)).thenReturn(customUserDetails);

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