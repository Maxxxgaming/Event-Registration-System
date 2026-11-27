package com.eventregistration.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventregistration.entity.Event;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.entity.Registration;
import com.eventregistration.entity.RegistrationStatus;
import com.eventregistration.entity.User;
import com.eventregistration.exception.BadRequestException;
import com.eventregistration.exception.ConflictException;
import org.springframework.security.access.AccessDeniedException;
import com.eventregistration.repository.EventRepository;
import com.eventregistration.repository.RegistrationRepository;
import com.eventregistration.repository.UserRepository;
import com.eventregistration.util.ApiMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApiMapper apiMapper;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void registerForEventRejectsFullEvent() {
        Event event = publishedEvent();
        User user = user(7L);
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserIdAndEventId(7L, 1L)).thenReturn(Optional.empty());
        when(registrationRepository.countByEventIdAndStatus(1L, RegistrationStatus.REGISTERED)).thenReturn(10L);

        assertThrows(ConflictException.class, () -> registrationService.registerForEvent(1L, 7L));
        verify(registrationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerForEventRejectsDuplicateActiveRegistration() {
        Event event = publishedEvent();
        User user = user(7L);
        Registration activeRegistration = Registration.builder()
                .id(22L)
                .event(event)
                .user(user)
                .status(RegistrationStatus.REGISTERED)
                .build();
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserIdAndEventId(7L, 1L))
                .thenReturn(Optional.of(activeRegistration));

        assertThrows(ConflictException.class, () -> registrationService.registerForEvent(1L, 7L));
        verify(registrationRepository, never()).countByEventIdAndStatus(1L, RegistrationStatus.REGISTERED);
        verify(registrationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerForEventRejectsPassedDeadline() {
        Event event = publishedEvent();
        event.setRegistrationDeadline(LocalDateTime.now().minusMinutes(1));
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));

        assertThrows(BadRequestException.class, () -> registrationService.registerForEvent(1L, 7L));
        verify(userRepository, never()).findById(7L);
    }

    @Test
    void cancelRegistrationRejectsCancellationAfterEventStarts() {
        Event event = publishedEvent();
        event.setStartDateTime(LocalDateTime.now().minusMinutes(1));
        Registration registration = Registration.builder()
                .id(22L)
                .event(event)
                .user(user(7L))
                .status(RegistrationStatus.REGISTERED)
                .build();
        when(registrationRepository.findById(22L)).thenReturn(Optional.of(registration));

        assertThrows(BadRequestException.class, () -> registrationService.cancelRegistration(22L, 7L, false));
        verify(registrationRepository, never()).save(registration);
    }

    @Test
    void userCannotCancelAnotherUsersRegistration() {
        Event event = publishedEvent();
        Registration registration = Registration.builder()
                .id(22L)
                .event(event)
                .user(user(8L))
                .status(RegistrationStatus.REGISTERED)
                .build();
        when(registrationRepository.findById(22L)).thenReturn(Optional.of(registration));

        assertThrows(AccessDeniedException.class,
                () -> registrationService.cancelRegistration(22L, 7L, false));
        verify(registrationRepository, never()).save(registration);
    }

    private Event publishedEvent() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        Event event = Event.builder()
                .status(EventStatus.PUBLISHED)
                .startDateTime(start)
                .endDateTime(start.plusHours(1))
                .registrationDeadline(start.minusHours(1))
                .capacity(10)
                .build();
        event.setId(1L);
        return event;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setFullName("Test User");
        return user;
    }
}
