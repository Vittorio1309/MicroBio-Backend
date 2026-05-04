package com.microbio.application.dto;

public record AuthResponse(
    Boolean success,
    String message,
    String username,
    String token,
    String role
) {}
