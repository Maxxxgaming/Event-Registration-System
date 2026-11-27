package com.eventregistration.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.eventregistration.entity.Event;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.entity.RegistrationStatus;
import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;
import com.eventregistration.repository.RegistrationRepository;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationIT extends AbstractIntegrationTest {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Test
    void registersSuccessfullyAndRejectsDuplicateRegistration() throws Exception {
        Event event = publishedEvent("Registration Event", 5);
        User user = createUser("attendee@example.com", Role.USER);
        String token = tokenFor(user);

        mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("REGISTERED"))
                .andExpect(jsonPath("$.data.eventTitle").value("Registration Event"));

        mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You are already registered for this event."));
    }

    @Test
    void rejectsFullEvents() throws Exception {
        Event event = publishedEvent("Full Event", 1);
        User first = createUser("first@example.com", Role.USER);
        User second = createUser("second@example.com", Role.USER);
        mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                        .header("Authorization", "Bearer " + tokenFor(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                        .header("Authorization", "Bearer " + tokenFor(second)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This event is full."));
    }

    @Test
    void rejectsPassedDeadlineAndUnpublishedEvents() throws Exception {
        LocalDateTime future = LocalDateTime.now().plusDays(2);
        Event deadlinePassed = createEvent(
                "Closed Event", EventStatus.PUBLISHED, future, LocalDateTime.now().minusMinutes(1), 5);
        Event unpublished = createEvent(
                "Draft Event", EventStatus.DRAFT, future, future.minusHours(1), 5);
        User user = createUser("deadline-user@example.com", Role.USER);
        String token = tokenFor(user);

        mockMvc.perform(post("/api/events/{eventId}/registrations", deadlinePassed.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The registration deadline for this event has passed."));

        mockMvc.perform(post("/api/events/{eventId}/registrations", unpublished.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This event is not open for registration."));
    }

    @Test
    void ownerCanCancelAndReRegisterReactivatesTheSameRow() throws Exception {
        Event event = publishedEvent("Reactivation Event", 5);
        User user = createUser("reactivate@example.com", Role.USER);
        String token = tokenFor(user);
        String registrationResponse = mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long firstId = objectMapper.readTree(registrationResponse).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/registrations/{id}", firstId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        String reactivated = mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("REGISTERED"))
                .andReturn().getResponse().getContentAsString();
        long reactivatedId = objectMapper.readTree(reactivated).path("data").path("id").asLong();

        assertEquals(firstId, reactivatedId);
        assertEquals(1, registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.REGISTERED));
    }

    @Test
    void userCannotCancelAnotherUsersRegistration() throws Exception {
        Event event = publishedEvent("Owner Event", 5);
        User owner = createUser("owner@example.com", Role.USER);
        User other = createUser("other@example.com", Role.USER);
        String body = mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                        .header("Authorization", "Bearer " + tokenFor(owner)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long registrationId = objectMapper.readTree(body).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + tokenFor(other)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action."));
    }

    @Test
    void concurrentRequestsNeverOverbookEventCapacity() throws Exception {
        Event event = publishedEvent("Concurrent Event", 5);
        int requestCount = 20;
        User[] users = new User[requestCount];
        for (int index = 0; index < requestCount; index++) {
            users[index] = createUser("concurrent-" + index + "@example.com", Role.USER);
        }

        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Integer>> results = new ArrayList<>(requestCount);
            for (int index = 0; index < requestCount; index++) {
                User user = users[index];
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Concurrency test start timed out.");
                    }
                    return mockMvc.perform(post("/api/events/{eventId}/registrations", event.getId())
                                    .header("Authorization", "Bearer " + tokenFor(user)))
                            .andReturn().getResponse().getStatus();
                }));
            }

            if (!ready.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Concurrency test workers did not become ready.");
            }
            start.countDown();
            int successful = 0;
            for (Future<Integer> result : results) {
                int responseStatus = result.get(60, TimeUnit.SECONDS);
                assertTrue(responseStatus == 201 || responseStatus == 409,
                        "Unexpected response status from concurrent registration: " + responseStatus);
                if (responseStatus == 201) successful++;
            }

            assertEquals(5, successful);
            assertEquals(5, registrationRepository.countByEventIdAndStatus(
                    event.getId(), RegistrationStatus.REGISTERED));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
