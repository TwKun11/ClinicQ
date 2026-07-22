package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.dto.response.AuthResponse;
import com.training.starter.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and refresh tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        return ApiResponse.success("Registration successful", authService.register(request, response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return ApiResponse.success("Login successful", authService.login(request, response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ApiResponse<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        return ApiResponse.success("Token refreshed", authService.refreshToken(refreshToken, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ApiResponse<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            HttpServletResponse response) {
        authService.logout(refreshToken, extractBearerToken(authorization), response);
        return ApiResponse.success("Logout successful", null);
    }

    private String extractBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
