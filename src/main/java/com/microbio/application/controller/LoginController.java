package com.microbio.application.controller;

/**
 * LoginController foi removido.
 * O login é feito via REST API em AuthController (/api/auth/login).
 * Não utilizamos Thymeleaf para autenticação.
 *
 * ENDPOINTS DE AUTENTICAÇÃO:
 * POST /api/auth/login - Login com token JWT
 * POST /api/auth/logout - Logout
 * GET /api/auth/me - Informações do usuário autenticado
 */
@Deprecated(since = "1.0", forRemoval = true)
public class LoginController {
    // Deprecated
}

