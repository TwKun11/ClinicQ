package com.training.starter.service;

import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.dto.response.UserResponse;
import com.training.starter.entity.User;
import com.training.starter.enums.Role;
import com.training.starter.enums.UserStatus;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.DuplicateResourceException;
import com.training.starter.mapper.UserMapper;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Mock
    private AuthTokenStore authTokenStore;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", REFRESH_TOKEN_TTL);
        ReflectionTestUtils.setField(authService, "cookieSecure", false);
        ReflectionTestUtils.setField(authService, "publicUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "loginUrl", "http://localhost:4200/login");
        ReflectionTestUtils.setField(authService, "resetPasswordUrl", "http://localhost:4200/reset-password");
        ReflectionTestUtils.setField(authService, "emailVerificationTtlMinutes", 30L);
        ReflectionTestUtils.setField(authService, "passwordResetTtlMinutes", 30L);
    }

    @Test
    void register_validRequest_savesPendingUserAndSendsVerificationEmail() {
        var request = new RegisterRequest("testuser", "test@example.com", "password123", "Test User");
        var response = new MockHttpServletResponse();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(authTokenStore.create(eq("verify-email"), eq(1L), any(Duration.class))).thenReturn("verify-token");

        var result = authService.register(request, response);

        assertThat(result.accessToken()).isNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.role()).isEqualTo("USER");
        assertThat(response.getHeader("Set-Cookie")).isNull();
        verify(userRepository).save(any(User.class));
        verify(emailService).sendHtml(eq("test@example.com"), eq("Verify your ClinicQ account"), any(String.class));
        verify(refreshTokenStore, never()).save(any(), any(), anyLong());
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

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("testuser@example.com")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser@example.com")).thenReturn("refresh-token");

        var result = authService.login(new LoginRequest("testuser@example.com", "password123"), response);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=refresh-token");
        verify(refreshTokenStore).save("testuser@example.com", "refresh-token", REFRESH_TOKEN_TTL);
    }

    @Test
    void login_pendingUser_throwsBadRequestException() {
        var user = buildUser("testuser");
        user.setStatus(UserStatus.PENDING);
        user.setActive(false);

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("testuser@example.com", "password123"),
                new MockHttpServletResponse()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("verify your email");
    }

    @Test
    void login_invalidPassword_throwsBadCredentialsException() {
        var user = buildUser("testuser");

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("testuser@example.com", "wrong"),
                new MockHttpServletResponse()))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_validCookieRotatesTokens() {
        var user = buildUser("testuser");
        var response = new MockHttpServletResponse();

        when(jwtTokenProvider.isRefreshTokenValid("old-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.extractUsername("old-refresh-token")).thenReturn("testuser@example.com");
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenStore.matches("testuser@example.com", "old-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("testuser@example.com")).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser@example.com")).thenReturn("new-refresh-token");

        var result = authService.refreshToken("old-refresh-token", response);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=new-refresh-token");
        verify(refreshTokenStore).save("testuser@example.com", "new-refresh-token", REFRESH_TOKEN_TTL);
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
        when(jwtTokenProvider.extractUsername("refresh-token")).thenReturn("testuser@example.com");
        when(jwtTokenProvider.isAccessTokenValid("access-token")).thenReturn(true);
        when(jwtTokenProvider.getRemainingValidityMillis("access-token")).thenReturn(120000L);

        authService.logout("refresh-token", "access-token", response);

        assertThat(response.getHeader("Set-Cookie")).contains("refresh_token=", "Max-Age=0");
        verify(refreshTokenStore).delete("testuser@example.com");
        verify(accessTokenBlacklistStore).blacklist("access-token", 120000L);
    }

    @Test
    void verifyEmail_validTokenActivatesUser() {
        var user = buildUser("testuser");
        user.setStatus(UserStatus.PENDING);
        user.setActive(false);

        when(authTokenStore.consume("verify-email", "verify-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.verifyEmail("verify-token");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void me_activeUserReturnsMappedResponse() {
        var user = buildUser("testuser");
        var mapped = new UserResponse(1L, "testuser", "testuser@example.com", "Test User",
                "USER", "ACTIVE", true, null);

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(mapped);

        var result = authService.me("testuser@example.com");

        assertThat(result.email()).isEqualTo("testuser@example.com");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    private User buildUser(String username) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("encoded")
                .fullName("Test User")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();
    }
}
