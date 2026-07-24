package com.training.starter.service.impl;

import com.training.starter.dto.request.ChangePasswordRequest;
import com.training.starter.dto.request.ForgotPasswordRequest;
import com.training.starter.dto.request.GoogleLoginRequest;
import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.dto.request.ResetPasswordRequest;
import com.training.starter.dto.response.AuthResponse;
import com.training.starter.dto.response.UserResponse;
import com.training.starter.entity.User;
import com.training.starter.enums.Role;
import com.training.starter.enums.UserStatus;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.DuplicateResourceException;
import com.training.starter.exception.ResourceNotFoundException;
import com.training.starter.mapper.UserMapper;
import com.training.starter.repository.UserRepository;
import com.training.starter.security.JwtTokenProvider;
import com.training.starter.service.AccessTokenBlacklistStore;
import com.training.starter.service.AuthTokenStore;
import com.training.starter.service.AuthService;
import com.training.starter.service.EmailService;
import com.training.starter.service.RefreshTokenStore;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final AccessTokenBlacklistStore accessTokenBlacklistStore;
    private final AuthTokenStore authTokenStore;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final RestClient googleRestClient = RestClient.create("https://oauth2.googleapis.com");

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.auth.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.public-url:http://localhost:8080}")
    private String publicUrl;

    @Value("${app.frontend.login-url:http://localhost:4200/login}")
    private String loginUrl;

    @Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
    private String resetPasswordUrl;

    @Value("${app.auth.email-verification-ttl-minutes:30}")
    private long emailVerificationTtlMinutes;

    @Value("${app.auth.password-reset-ttl-minutes:30}")
    private long passwordResetTtlMinutes;

    @Value("${app.google.client-id:488480628324-uv5dt3j8rsf7ud80n1ok3tgek2be5t4b.apps.googleusercontent.com}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        User user = User.builder()
                .username(request.username())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.USER)
                .status(UserStatus.PENDING)
                .active(false)
                .build();

        user = userRepository.save(user);
        sendVerificationEmail(user);

        return new AuthResponse(null, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        requireActive(user);

        return issueTokens(user, response);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request, HttpServletResponse response) {
        GoogleTokenInfo tokenInfo = verifyGoogleCredential(request.credential());
        String email = normalizeEmail(tokenInfo.email());

        User user = userRepository.findByEmail(email)
                .map(existing -> activateGoogleVerifiedUser(existing, tokenInfo))
                .orElseGet(() -> createGoogleUser(tokenInfo, email));

        requireActive(user);
        return issueTokens(user, response);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String email = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        requireActive(user);
        if (!refreshTokenStore.matches(email, refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        return issueTokens(user, response);
    }

    @Override
    public void logout(String refreshToken, String accessToken, HttpServletResponse response) {
        if (refreshToken != null && jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            refreshTokenStore.delete(jwtTokenProvider.extractUsername(refreshToken));
        }
        if (accessToken != null && jwtTokenProvider.isAccessTokenValid(accessToken)) {
            accessTokenBlacklistStore.blacklist(accessToken, jwtTokenProvider.getRemainingValidityMillis(accessToken));
        }
        clearRefreshCookie(response);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        requireActive(user);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        Long userId = authTokenStore.consume("verify-email", token);
        if (userId == null) {
            throw new BadRequestException("Verification token is invalid or expired");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User account is inactive");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .ifPresent(this::sendPasswordResetEmail);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Long userId = authTokenStore.consume("reset-password", request.token());
        if (userId == null) {
            throw new BadRequestException("Password reset token is invalid or expired");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        requireActive(user);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenStore.delete(user.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        requireActive(user);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenStore.delete(user.getEmail());
    }

    @Override
    public String loginUrl() {
        return loginUrl;
    }

    private AuthResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        refreshTokenStore.save(user.getEmail(), refreshToken, refreshTokenExpiration);
        setRefreshCookie(response, refreshToken);

        return new AuthResponse(accessToken, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    private GoogleTokenInfo verifyGoogleCredential(String credential) {
        try {
            Map<?, ?> body = googleRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/tokeninfo")
                            .queryParam("id_token", credential)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (body == null || !googleClientId.equals(body.get("aud"))) {
                throw new BadCredentialsException("Invalid Google credential");
            }
            if (!"true".equals(String.valueOf(body.get("email_verified")))) {
                throw new BadCredentialsException("Google email is not verified");
            }
            Object email = body.get("email");
            if (email == null || email.toString().isBlank()) {
                throw new BadCredentialsException("Google email is missing");
            }
            Object name = body.get("name");
            return new GoogleTokenInfo(
                    email.toString(),
                    name == null || name.toString().isBlank() ? email.toString() : name.toString()
            );
        } catch (RestClientException ex) {
            throw new BadCredentialsException("Invalid Google credential");
        }
    }

    private User activateGoogleVerifiedUser(User user, GoogleTokenInfo tokenInfo) {
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User account is inactive");
        }
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
            user.setActive(true);
            if (user.getFullName() == null || user.getFullName().isBlank()) {
                user.setFullName(tokenInfo.name());
            }
            return userRepository.save(user);
        }
        return user;
    }

    private User createGoogleUser(GoogleTokenInfo tokenInfo, String email) {
        User user = User.builder()
                .username(uniqueUsernameFromEmail(email))
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .fullName(tokenInfo.name())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private String uniqueUsernameFromEmail(String email) {
        String localPart = email.substring(0, email.indexOf('@'))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");
        String base = localPart.isBlank() ? "google-user" : localPart;
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private void requireActive(User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            throw new BadRequestException("Please verify your email before logging in");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User account is inactive");
        }
    }

    private void sendVerificationEmail(User user) {
        String token = authTokenStore.create("verify-email", user.getId(), Duration.ofMinutes(emailVerificationTtlMinutes));
        String link = publicUrl + "/api/v1/auth/verify-email?token=" + token;
        emailService.sendHtml(user.getEmail(), "Verify your ClinicQ account", """
                <div style="font-family:Arial,sans-serif;line-height:1.5">
                    <h2>Verify your ClinicQ account</h2>
                    <p>Your account is pending email verification.</p>
                    <p><a href="%s" style="display:inline-block;padding:12px 18px;background:#2563eb;color:white;text-decoration:none;border-radius:6px">Verify email</a></p>
                </div>
                """.formatted(link));
    }

    private void sendPasswordResetEmail(User user) {
        String token = authTokenStore.create("reset-password", user.getId(), Duration.ofMinutes(passwordResetTtlMinutes));
        String link = resetPasswordUrl + "?token=" + token;
        emailService.sendHtml(user.getEmail(), "Reset your ClinicQ password", """
                <div style="font-family:Arial,sans-serif;line-height:1.5">
                    <h2>Reset your ClinicQ password</h2>
                    <p>Click the button below to set a new password.</p>
                    <p><a href="%s" style="display:inline-block;padding:12px 18px;background:#2563eb;color:white;text-decoration:none;border-radius:6px">Reset password</a></p>
                    <p>If you did not request this, ignore this email.</p>
                </div>
                """.formatted(link));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private record GoogleTokenInfo(String email, String name) {}

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
