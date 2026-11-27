package com.eventregistration.integration;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.eventregistration.entity.Event;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;
import com.eventregistration.repository.EventRepository;
import com.eventregistration.repository.UserRepository;
import com.eventregistration.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    private static final String MYSQL_IMAGE = "mysql:8.4";

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(MYSQL_IMAGE)
            .withDatabaseName("event_registration_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.update("DELETE FROM registrations");
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM users");
    }

    protected User createUser(String email, Role role) {
        return userRepository.saveAndFlush(User.builder()
                .fullName("Test " + role.name())
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role)
                .build());
    }

    protected String tokenFor(User user) {
        return jwtService.generateToken(user);
    }

    protected Event createEvent(String title, EventStatus status, LocalDateTime start,
                                LocalDateTime deadline, int capacity) {
        return eventRepository.saveAndFlush(Event.builder()
                .title(title)
                .description("Integration test event")
                .location("Test Hall")
                .startDateTime(start)
                .endDateTime(start.plusHours(2))
                .registrationDeadline(deadline)
                .capacity(capacity)
                .status(status)
                .build());
    }

    protected Event publishedEvent(String title, int capacity) {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        return createEvent(title, EventStatus.PUBLISHED, start, start.minusHours(1), capacity);
    }
}
