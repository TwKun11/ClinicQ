package com.training.starter.service;

import com.training.starter.dto.request.ChangePasswordRequest;
import com.training.starter.dto.request.ForgotPasswordRequest;
import com.training.starter.dto.request.GoogleLoginRequest;
import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.dto.request.ResetPasswordRequest;
import com.training.starter.dto.response.AuthResponse;
import com.training.starter.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request, HttpServletResponse response);

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse googleLogin(GoogleLoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(String refreshToken, HttpServletResponse response);

    void logout(String refreshToken, String accessToken, HttpServletResponse response);

    UserResponse me(String email);

    void verifyEmail(String token);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    String loginUrl();
}
