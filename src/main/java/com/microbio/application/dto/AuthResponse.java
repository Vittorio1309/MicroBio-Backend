package com.microbio.application.dto;

public record AuthResponse(
    Boolean success,
    String message,
    String username,
    String token,
    String role
) {
    /**
     * Construtor simplificado para resposta sem token
     */
    public AuthResponse(Boolean success, String message, String username) {
        this(success, message, username, null, null);
    }
}

