package com.eventregistration.controller;

import java.time.LocalDateTime;

import com.eventregistration.config.SecurityConfig;
import com.eventregistration.dto.AuthResponse;
import com.eventregistration.dto.EventRequest;
import com.eventregistration.dto.EventResponse;
import com.eventregistration.dto.LoginRequest;
import com.eventregistration.dto.RegisterRequest;
import com.eventregistration.dto.RegistrationResponse;
import com.eventregistration.dto.UserSummaryResponse;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.entity.Role;
import com.eventregistration.exception.ConflictException;
import com.eventregistration.exception.GlobalExceptionHandler;
import com.eventregistration.exception.ResourceNotFoundException;
import com.eventregistration.security.CustomUserDetails;
import com.eventregistration.security.CustomUserDetailsService;
import com.eventregistration.security.JsonAccessDeniedHandler;
import com.eventregistration.security.JsonAuthenticationEntryPoint;
import com.eventregistration.security.JwtAuthenticationFilter;
import com.eventregistration.security.JwtService;
import com.eventregistration.service.AuthService;
import com.eventregistration.service.EventService;
import com.eventregistration.service.RegistrationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        EventController.class,
        RegistrationController.class,
        AdminEventController.class
})
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class
})
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void registrationSuccessReturnsCreatedAndUserRole() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse(Role.USER));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {"fullName":"Test User","email":"test@example.com","password":"pass1234"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    void loginSuccessReturnsBearerToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse(Role.USER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {"email":"test@example.com","password":"pass1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("An account with this email already exists."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {"fullName":"Test User","email":"test@example.com","password":"pass1234"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {"email":"test@example.com","password":"wrong123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/registrations/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Please log in to continue."))
                .andExpect(jsonPath("$.errorCode").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void userCannotCallAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/events")
                        .with(user("member").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You do not have permission to perform this action."));
    }

    @Test
    void adminCanCreateEvent() throws Exception {
        EventResponse event = new EventResponse(
                2L, "Test Event", null, "Hall", LocalDateTime.parse("2099-06-01T10:00:00"),
                LocalDateTime.parse("2099-06-01T11:00:00"), LocalDateTime.parse("2099-06-01T09:00:00"),
                20, EventStatus.DRAFT, 1L, null, null, 0, 20
        );
        when(eventService.createEvent(any(EventRequest.class), eq(1L))).thenReturn(event);

        mockMvc.perform(post("/api/events")
                        .with(authentication(authenticationFor(1L, Role.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "title":"Test Event",
                                  "location":"Hall",
                                  "startDateTime":"2099-06-01T10:00:00",
                                  "endDateTime":"2099-06-01T11:00:00",
                                  "registrationDeadline":"2099-06-01T09:00:00",
                                  "capacity":20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdBy").value(1));
    }

    @Test
    void authenticatedUserCanRegisterForEvent() throws Exception {
        RegistrationResponse registration = new RegistrationResponse(
                7L, 3L, "Public Event", LocalDateTime.parse("2099-06-01T10:00:00"),
                "Hall", 1L, "Test User", com.eventregistration.entity.RegistrationStatus.REGISTERED, null
        );
        when(registrationService.registerForEvent(3L, 1L)).thenReturn(registration);

        mockMvc.perform(post("/api/events/3/registrations")
                        .with(authentication(authenticationFor(1L, Role.USER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    void userCannotCancelAnotherUsersRegistration() throws Exception {
        when(registrationService.cancelRegistration(5L, 1L, false))
                .thenThrow(new AccessDeniedException("Not the owner"));

        mockMvc.perform(delete("/api/registrations/5")
                        .with(authentication(authenticationFor(1L, Role.USER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void validationFailureReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "title": "",
                                  "location": "Community Hall",
                                  "startDateTime": "2027-06-01T10:00:00",
                                  "endDateTime": "2027-06-01T11:00:00",
                                  "registrationDeadline": "2027-06-01T09:00:00",
                                  "capacity": 20
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    void unknownEventReturnsNotFoundErrorResponse() throws Exception {
        when(eventService.getEventById(404L))
                .thenThrow(new ResourceNotFoundException("Event not found."));

        mockMvc.perform(get("/api/events/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/events/404"));
    }

    @Test
    void malformedJsonReturnsFriendlyBadRequest() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void unsupportedMethodReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(patch("/api/events/3")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"));
    }

    private AuthResponse authResponse(Role role) {
        return new AuthResponse(
                "test.jwt.token",
                "Bearer",
                86_400,
                new UserSummaryResponse(1L, "Test User", "test@example.com", role)
        );
    }

    private UsernamePasswordAuthenticationToken authenticationFor(Long userId, Role role) {
        CustomUserDetails principal = new CustomUserDetails(
                userId,
                "test@example.com",
                "not-used",
                "Test User",
                role.name()
        );
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }
}
