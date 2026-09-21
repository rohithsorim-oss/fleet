package com.sorim.fleetmanagement.service;

import com.sorim.fleetmanagement.dto.request.LoginRequest;
import com.sorim.fleetmanagement.dto.request.RegisterRequest;
import com.sorim.fleetmanagement.dto.response.AuthResponse;
import com.sorim.fleetmanagement.dto.response.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser();
}
