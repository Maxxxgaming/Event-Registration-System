package com.eventregistration.integration;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.eventregistration.entity.Event;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.entity.Role;
import com.eventregistration.entity.User;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventIT extends AbstractIntegrationTest {

    @Test
    void adminCanCreateUpdatePublishAndCancelEvent() throws Exception {
        User admin = createUser("event-admin@example.com", Role.ADMIN);
        String token = tokenFor(admin);
        String createBody = eventBody("Initial title", LocalDateTime.now().plusDays(3));
        String response = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).path("data").path("id").asLong();

        mockMvc.perform(put("/api/events/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventBody("Updated title", LocalDateTime.now().plusDays(4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated title"));

        mockMvc.perform(patch("/api/events/{id}/status", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"PUBLISHED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(delete("/api/events/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void regularUserCannotCreateEvents() throws Exception {
        User user = createUser("event-user@example.com", Role.USER);
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventBody("Forbidden event", LocalDateTime.now().plusDays(3))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void publicListIncludesOnlyPublishedEventsAndSupportsSearchAndPagination() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        createEvent("Concert One", EventStatus.PUBLISHED, start, start.minusHours(1), 10);
        createEvent("Concert Two", EventStatus.PUBLISHED, start.plusDays(1), start, 10);
        createEvent("Hidden Concert", EventStatus.DRAFT, start.plusDays(2), start.plusDays(1), 10);
        createEvent("Community Workshop", EventStatus.PUBLISHED, start.plusDays(3), start.plusDays(2), 10);

        mockMvc.perform(get("/api/events").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(3));

        mockMvc.perform(get("/api/events").param("search", "workshop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("Community Workshop"));

        mockMvc.perform(get("/api/events").param("search", "hidden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void validationFailuresUseErrorResponseWithFieldErrors() throws Exception {
        User admin = createUser("validation-admin@example.com", Role.ADMIN);
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","location":"","startDateTime":null,"endDateTime":null,"registrationDeadline":null,"capacity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").isNotEmpty());
    }

    private String eventBody(String title, LocalDateTime start) {
        return """
                {
                  "title":"%s",
                  "description":"Test description",
                  "location":"Test Hall",
                  "startDateTime":"%s",
                  "endDateTime":"%s",
                  "registrationDeadline":"%s",
                  "capacity":10
                }
                """.formatted(title, start, start.plusHours(2), start.minusHours(1));
    }
}
