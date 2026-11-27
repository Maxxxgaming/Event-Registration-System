package com.eventregistration.config;

import java.util.Locale;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;
import com.eventregistration.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final Environment environment;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = environment.getProperty("ADMIN_EMAIL");
        String adminPassword = environment.getProperty("ADMIN_PASSWORD");
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("Admin account seeding skipped because ADMIN_EMAIL or ADMIN_PASSWORD is not configured.");
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Admin account seeding skipped because the configured admin account already exists.");
            return;
        }

        User admin = User.builder()
                .fullName("Administrator")
                .email(normalizedEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Configured admin account was created.");
    }
}
