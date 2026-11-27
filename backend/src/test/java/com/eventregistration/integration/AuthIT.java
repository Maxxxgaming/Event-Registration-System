package com.eventregistration.integration;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIT extends AbstractIntegrationTest {

    private static final String TEST_JWT_SECRET = "test-only-secret-with-at-least-32-characters";

    @Test
    void registerLoginAndCurrentUserReturnSafeUserSummary() throws Exception {
        String registerBody = """
                {"fullName":"Taylor User","email":"Taylor@Example.com","password":"secure123"}
                """;
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.email").value("taylor@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(response).path("data").path("token").asText();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"TAYLOR@example.com","password":"secure123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.fullName").value("Taylor User"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("taylor@example.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String body = """
                {"fullName":"Taylor User","email":"taylor@example.com","password":"secure123"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Another User","email":"TAYLOR@example.com","password":"secure456"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));
    }

    @Test
    void invalidCredentialsReturnUniformUnauthorizedResponse() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Taylor User","email":"taylor@example.com","password":"secure123"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"taylor@example.com","password":"wrong123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void invalidAndExpiredTokensAreUnauthorized() throws Exception {
        User user = createUser("token-user@example.com", Role.USER);
        String expired = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Please log in to continue."));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
