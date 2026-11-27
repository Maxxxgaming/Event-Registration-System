package com.eventregistration.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventregistration.dto.LoginRequest;
import com.eventregistration.dto.RegisterRequest;
import com.eventregistration.dto.UserSummaryResponse;
import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;
import com.eventregistration.exception.ConflictException;
import com.eventregistration.repository.UserRepository;
import com.eventregistration.security.CustomUserDetails;
import com.eventregistration.security.JwtService;
import com.eventregistration.util.ApiMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApiMapper apiMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository, passwordEncoder, authenticationManager, jwtService, apiMapper);
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndAlwaysCreatesUserRole() {
        when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("signed.token");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);
        when(apiMapper.toUserSummaryResponse(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            return new UserSummaryResponse(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRole());
        });

        var response = authService.register(
                new RegisterRequest("Person", " Person@Example.com ", "pass1234"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("person@example.com", saved.getEmail());
        assertEquals(Role.USER, saved.getRole());
        assertNotEquals("pass1234", saved.getPassword());
        assertTrue(passwordEncoder.matches("pass1234", saved.getPassword()));
        assertEquals("signed.token", response.token());
        assertEquals(86_400L, response.expiresIn());
        assertEquals(Role.USER, response.user().role());
    }

    @Test
    void registerRejectsDuplicateNormalizedEmail() {
        when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(
                new RegisterRequest("Person", "PERSON@example.com", "pass1234")));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginNormalizesEmailAndReturnsTokenForAuthenticatedUser() {
        User user = User.builder()
                .fullName("Person")
                .email("person@example.com")
                .password(passwordEncoder.encode("pass1234"))
                .role(Role.USER)
                .build();
        user.setId(12L);
        CustomUserDetails details = CustomUserDetails.from(user);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                details, null, details.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("signed.token");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);
        when(apiMapper.toUserSummaryResponse(user))
                .thenReturn(new UserSummaryResponse(12L, "Person", "person@example.com", Role.USER));

        var response = authService.login(new LoginRequest("PERSON@EXAMPLE.COM", "pass1234"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertEquals("person@example.com", tokenCaptor.getValue().getPrincipal());
        assertEquals("signed.token", response.token());
    }

    @Test
    void loginPropagatesBadCredentialsForUniformUnauthorizedResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(
                new LoginRequest("person@example.com", "wrong123")));
    }
}
