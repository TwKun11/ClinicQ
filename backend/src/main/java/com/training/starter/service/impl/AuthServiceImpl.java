package com.training.starter.service.impl;

import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.dto.response.AuthResponse;
import com.training.starter.entity.User;
import com.training.starter.enums.Role;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.DuplicateResourceException;
import com.training.starter.repository.UserRepository;
import com.training.starter.security.JwtTokenProvider;
import com.training.starter.service.AccessTokenBlacklistStore;
import com.training.starter.service.AuthService;
import com.training.starter.service.RefreshTokenStore;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenStore refreshTokenStore;
    private final AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.auth.cookie-secure:false}")
    private boolean cookieSecure;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.USER)
                .active(true)
                .build();

        userRepository.save(user);

        return issueTokens(user, response);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        return issueTokens(user, response);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!refreshTokenStore.matches(username, refreshToken)) {
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

    private AuthResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        refreshTokenStore.save(user.getUsername(), refreshToken, refreshTokenExpiration);
        setRefreshCookie(response, refreshToken);

        return new AuthResponse(accessToken, user.getUsername(), user.getRole().name());
    }

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
