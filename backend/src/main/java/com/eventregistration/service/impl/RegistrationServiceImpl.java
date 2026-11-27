package com.eventregistration.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import com.eventregistration.dto.RegistrationResponse;
import com.eventregistration.entity.Event;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.entity.Registration;
import com.eventregistration.entity.RegistrationStatus;
import com.eventregistration.entity.User;
import com.eventregistration.exception.BadRequestException;
import com.eventregistration.exception.ConflictException;
import com.eventregistration.exception.ResourceNotFoundException;
import com.eventregistration.repository.EventRepository;
import com.eventregistration.repository.RegistrationRepository;
import com.eventregistration.repository.UserRepository;
import com.eventregistration.service.RegistrationService;
import com.eventregistration.util.ApiMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final ApiMapper apiMapper;

    @Override
    public RegistrationResponse registerForEvent(Long eventId, Long userId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("This event is not open for registration.");
        }
        if (LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new BadRequestException("The registration deadline for this event has passed.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Registration existing = registrationRepository.findByUserIdAndEventId(userId, eventId).orElse(null);
        if (existing != null && existing.getStatus() == RegistrationStatus.REGISTERED) {
            throw new ConflictException("You are already registered for this event.");
        }

        long registeredCount = registrationRepository.countByEventIdAndStatus(
                eventId, RegistrationStatus.REGISTERED);
        if (registeredCount >= event.getCapacity()) {
            throw new ConflictException("This event is full.");
        }

        Registration registration;
        if (existing == null) {
            registration = Registration.builder()
                    .user(user)
                    .event(event)
                    .status(RegistrationStatus.REGISTERED)
                    .build();
        } else {
            registration = existing;
            registration.setStatus(RegistrationStatus.REGISTERED);
            registration.setRegisteredAt(Instant.now());
        }
        return apiMapper.toRegistrationResponse(registrationRepository.save(registration));
    }

    @Override
    public RegistrationResponse cancelRegistration(Long registrationId, Long userId, boolean admin) {
        Registration registration = findRegistration(registrationId);
        if (!admin && !registration.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own registration.");
        }
        if (!LocalDateTime.now().isBefore(registration.getEvent().getStartDateTime())) {
            throw new BadRequestException("Registration cannot be cancelled after the event has started.");
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ConflictException("This registration has already been cancelled.");
        }
        registration.setStatus(RegistrationStatus.CANCELLED);
        return apiMapper.toRegistrationResponse(registrationRepository.save(registration));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found.");
        }
        return registrationRepository.findByUserId(userId).stream()
                .map(apiMapper::toRegistrationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found.");
        }
        return registrationRepository.findByEventId(eventId).stream()
                .map(apiMapper::toRegistrationResponse)
                .toList();
    }

    private Registration findRegistration(Long registrationId) {
        return registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found."));
    }
}
