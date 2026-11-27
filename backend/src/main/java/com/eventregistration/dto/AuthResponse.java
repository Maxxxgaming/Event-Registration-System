package com.eventregistration.dto;

public record AuthResponse(String token, String tokenType, long expiresIn, UserSummaryResponse user) {
}
