package com.eventregistration.service;

import com.eventregistration.dto.AuthResponse;
import com.eventregistration.dto.LoginRequest;
import com.eventregistration.dto.RegisterRequest;
import com.eventregistration.dto.UserSummaryResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserSummaryResponse getCurrentUser(Long userId);
}
