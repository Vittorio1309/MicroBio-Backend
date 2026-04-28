package com.microbio.application.dto;

public record LoginRequest(
    String username,
    String password
) {
}

