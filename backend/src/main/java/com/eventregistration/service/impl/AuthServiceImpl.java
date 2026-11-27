package com.eventregistration.service.impl;

import java.util.Locale;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventregistration.dto.AuthResponse;
import com.eventregistration.dto.LoginRequest;
import com.eventregistration.dto.RegisterRequest;
import com.eventregistration.dto.UserSummaryResponse;
import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;
import com.eventregistration.exception.ConflictException;
import com.eventregistration.exception.ResourceNotFoundException;
import com.eventregistration.repository.UserRepository;
import com.eventregistration.security.CustomUserDetails;
import com.eventregistration.security.JwtService;
import com.eventregistration.service.AuthService;
import com.eventregistration.util.ApiMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApiMapper apiMapper;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists.");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        return authResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(details.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return authResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return apiMapper.toUserSummaryResponse(user);
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationMs() / 1_000,
                apiMapper.toUserSummaryResponse(user)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
