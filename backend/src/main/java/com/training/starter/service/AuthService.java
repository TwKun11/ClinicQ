package com.training.starter.service;

import com.training.starter.dto.request.LoginRequest;
import com.training.starter.dto.request.RegisterRequest;
import com.training.starter.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request, HttpServletResponse response);

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(String refreshToken, HttpServletResponse response);

    void logout(String refreshToken, String accessToken, HttpServletResponse response);
}
