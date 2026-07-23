package com.training.starter.service;

import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.entity.User;
import com.training.starter.enums.Role;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.DuplicateResourceException;
import com.training.starter.repository.UserRepository;
import com.training.starter.security.JwtTokenProvider;
import com.training.starter.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long REFRESH_TOKEN_TTL = 604800000L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", REFRESH_TOKEN_TTL);
        ReflectionTestUtils.setField(authService, "cookieSecure", false);
    }

    @Test
    void register_validRequest_savesUserStoresRefreshTokenAndReturnsAccessToken() {
        var request = new RegisterRequest("testuser", "test@example.com", "password123", "Test User");
        var response = new MockHttpServletResponse();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(jwtTokenProvider.generateAccessToken("testuser")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refresh-token");

        var result = authService.register(request, response);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.role()).isEqualTo("USER");
        assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=refresh-token", "HttpOnly");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenStore).save("testuser", "refresh-token", REFRESH_TOKEN_TTL);
    }

    @Test
    void register_duplicateUsername_throwsDuplicateResourceException() {
        var request = new RegisterRequest("existing", "test@example.com", "password123", "Test User");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, new MockHttpServletResponse()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentialsIssuesTokens() {
        var user = buildUser("testuser");
        var response = new MockHttpServletResponse();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken("testuser")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refresh-token");

        var result = authService.login(new LoginRequest("testuser", "password123"), response);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=refresh-token");
        verify(refreshTokenStore).save("testuser", "refresh-token", REFRESH_TOKEN_TTL);
    }

    @Test
    void refreshToken_validCookieRotatesTokens() {
        var user = buildUser("testuser");
        var response = new MockHttpServletResponse();

        when(jwtTokenProvider.isRefreshTokenValid("old-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.extractUsername("old-refresh-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(refreshTokenStore.matches("testuser", "old-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("testuser")).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("new-refresh-token");

        var result = authService.refreshToken("old-refresh-token", response);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=new-refresh-token");
        verify(refreshTokenStore).save("testuser", "new-refresh-token", REFRESH_TOKEN_TTL);
    }

    @Test
    void refreshToken_invalidToken_throwsBadRequestException() {
        when(jwtTokenProvider.isRefreshTokenValid("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("bad-token", new MockHttpServletResponse()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void logout_validTokensRevokesRefreshAndBlacklistsAccessToken() {
        var response = new MockHttpServletResponse();

        when(jwtTokenProvider.isRefreshTokenValid("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.extractUsername("refresh-token")).thenReturn("testuser");
        when(jwtTokenProvider.isAccessTokenValid("access-token")).thenReturn(true);
        when(jwtTokenProvider.getRemainingValidityMillis("access-token")).thenReturn(120000L);

        authService.logout("refresh-token", "access-token", response);

        assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=", "Max-Age=0");
        verify(refreshTokenStore).delete("testuser");
        verify(accessTokenBlacklistStore).blacklist("access-token", 120000L);
    }

    private User buildUser(String username) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("encoded")
                .fullName("Test User")
                .role(Role.USER)
                .active(true)
                .build();
    }
}
