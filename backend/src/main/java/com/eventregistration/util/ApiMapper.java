package com.eventregistration.util;

import org.springframework.stereotype.Component;

import com.eventregistration.dto.EventResponse;
import com.eventregistration.dto.RegistrationResponse;
import com.eventregistration.dto.UserSummaryResponse;
import com.eventregistration.entity.Event;
import com.eventregistration.entity.Registration;
import com.eventregistration.entity.User;

@Component
public class ApiMapper {

    public EventResponse toEventResponse(Event event, long registeredCount) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getRegistrationDeadline(),
                event.getCapacity(),
                event.getStatus(),
                event.getCreatedBy() == null ? null : event.getCreatedBy().getId(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                registeredCount,
                Math.max(0, (long) event.getCapacity() - registeredCount)
        );
    }

    public RegistrationResponse toRegistrationResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getEvent().getStartDateTime(),
                registration.getEvent().getLocation(),
                registration.getUser().getId(),
                registration.getUser().getFullName(),
                registration.getStatus(),
                registration.getRegisteredAt()
        );
    }

    public UserSummaryResponse toUserSummaryResponse(User user) {
        return new UserSummaryResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
