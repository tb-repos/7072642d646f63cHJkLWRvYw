package com.tourbhook.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.UserRepository;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FilterChain filterChain;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void blockedUser_isRejectedWithForbidden_andNeverReachesFilterChain() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository, objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        request.setRequestURI("/api/v1/places/place-1/reviews");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.isRefreshToken("valid-token")).thenReturn(false);
        when(jwtService.extractSubject("valid-token")).thenReturn("user-1");

        User blockedUser = User.builder().id("user-1").blocked(true).build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(blockedUser));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("permanently blocked");
        verify(filterChain, never()).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void nonBlockedUser_isAuthenticatedAndAllowedThrough() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository, objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.isRefreshToken("valid-token")).thenReturn(false);
        when(jwtService.extractSubject("valid-token")).thenReturn("user-1");

        User activeUser = User.builder().id("user-1").blocked(false).build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(activeUser));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("user-1");
    }

    @Test
    void requestWithNoAuthorizationHeader_passesThroughUnauthenticated() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository, objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findById(anyString());
    }
}