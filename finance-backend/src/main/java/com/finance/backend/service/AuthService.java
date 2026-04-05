package com.finance.backend.service;

import com.finance.backend.dto.request.LoginRequest;
import com.finance.backend.dto.request.RegisterRequest;
import com.finance.backend.dto.response.AuthResponse;

/**
 * Handles user registration and JWT-based authentication.
 */
public interface AuthService {

    /**
     * Registers a new user, persists credentials (BCrypt), and issues a JWT.
     *
     * @param request validated registration payload including desired role
     * @return token and basic profile fields for the client
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Validates email/password and returns a JWT for subsequent API calls.
     *
     * @param request login credentials
     * @return token and basic profile fields for the client
     */
    AuthResponse login(LoginRequest request);
}
