package com.eventregistration.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.eventregistration.dto.ApiResponse;
import com.eventregistration.dto.AuthResponse;
import com.eventregistration.dto.LoginRequest;
import com.eventregistration.dto.RegisterRequest;
import com.eventregistration.dto.UserSummaryResponse;
import com.eventregistration.security.CustomUserDetails;
import com.eventregistration.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Create an account and authenticate")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authService.register(request), "Account registered successfully."));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a bearer token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request), "Logged in successfully."));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current user's profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> currentUser(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.getCurrentUser(principal.id()), "Profile retrieved successfully."));
    }
}
