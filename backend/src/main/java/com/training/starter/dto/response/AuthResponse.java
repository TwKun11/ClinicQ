package com.training.starter.dto.response;

public record AuthResponse(
        String accessToken,
        String username,
        String role
) {}
