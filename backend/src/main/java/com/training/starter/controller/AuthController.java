package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.dto.request.ChangePasswordRequest;
import com.training.starter.dto.request.ForgotPasswordRequest;
import com.training.starter.dto.request.GoogleLoginRequest;
import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.dto.request.ResetPasswordRequest;
import com.training.starter.dto.response.AuthResponse;
import com.training.starter.dto.response.UserResponse;
import com.training.starter.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and refresh tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new pending user and send verification email")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        return ApiResponse.success("Registration pending. Please verify your email.", authService.register(request, response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return ApiResponse.success("Login successful", authService.login(request, response));
    }

    @PostMapping("/google")
    @Operation(summary = "Login with Google ID token")
    public ApiResponse<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletResponse response) {
        return ApiResponse.success("Google login successful", authService.googleLogin(request, response));
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

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        return ApiResponse.success("Current user loaded", authService.me(authentication.getName()));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify registered email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        URI location = UriComponentsBuilder.fromUriString(authService.loginUrl())
                .queryParam("verified", "true")
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("If the email exists, a reset link has been sent", null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using email token")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password reset successful", null);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for current user")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ApiResponse.success("Password changed successfully", null);
    }

    private String extractBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
