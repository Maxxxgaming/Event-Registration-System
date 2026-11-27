package com.eventregistration.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generatedTokenContainsUserClaimsAndValidatesAgainstUserDetails() {
        JwtService jwtService = new JwtService(testSecret(), 86_400_000L);
        User user = User.builder()
                .email("person@example.com")
                .password("hashed-password")
                .fullName("Person")
                .role(Role.USER)
                .build();
        user.setId(42L);
        CustomUserDetails details = CustomUserDetails.from(user);

        String token = jwtService.generateToken(user);

        assertEquals("person@example.com", jwtService.extractEmail(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("USER", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, details));
        assertFalse(jwtService.isTokenValid(token, new CustomUserDetails(
                43L, "person@example.com", "hashed-password", "Person", "USER")));
    }

    @Test
    void missingOrShortSecretFailsFast() {
        IllegalStateException missing = assertThrows(
                IllegalStateException.class, () -> new JwtService("", 86_400_000L));
        assertTrue(missing.getMessage().contains("JWT_SECRET"));

        IllegalStateException shortSecret = assertThrows(
                IllegalStateException.class, () -> new JwtService("too-short", 86_400_000L));
        assertTrue(shortSecret.getMessage().contains("32 characters"));
    }

    private String testSecret() {
        return UUID.randomUUID().toString() + UUID.randomUUID();
    }
}
