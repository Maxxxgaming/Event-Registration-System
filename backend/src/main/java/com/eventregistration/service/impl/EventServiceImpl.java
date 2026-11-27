package com.eventregistration.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventregistration.dto.EventRequest;
import com.eventregistration.dto.EventResponse;
import com.eventregistration.entity.Event;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.entity.RegistrationStatus;
import com.eventregistration.exception.BadRequestException;
import com.eventregistration.exception.ResourceNotFoundException;
import com.eventregistration.repository.EventRepository;
import com.eventregistration.repository.RegistrationRepository;
import com.eventregistration.repository.UserRepository;
import com.eventregistration.service.EventService;
import com.eventregistration.util.ApiMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private static final int MAX_PAGE_SIZE = 100;

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final ApiMapper apiMapper;

    @Override
    public EventResponse createEvent(EventRequest request, Long creatorId) {
        validateEventDates(request, true);

        Event event = Event.builder()
                .title(request.title().trim())
                .description(request.description())
                .location(request.location().trim())
                .startDateTime(request.startDateTime())
                .endDateTime(request.endDateTime())
                .registrationDeadline(request.registrationDeadline())
                .capacity(request.capacity())
                .status(EventStatus.DRAFT)
                .createdBy(userRepository.findById(creatorId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found.")))
                .build();
        return toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse updateEvent(Long eventId, EventRequest request) {
        validateEventDates(request, false);
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
        long registeredCount = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
        if (request.capacity() < registeredCount) {
            throw new BadRequestException("Capacity cannot be lower than the number of registered attendees.");
        }

        event.setTitle(request.title().trim());
        event.setDescription(request.description());
        event.setLocation(request.location().trim());
        event.setStartDateTime(request.startDateTime());
        event.setEndDateTime(request.endDateTime());
        event.setRegistrationDeadline(request.registrationDeadline());
        event.setCapacity(request.capacity());
        return toResponse(eventRepository.save(event), registeredCount);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId) {
        return toResponse(findEvent(eventId));
    }

    @Override
    public EventResponse cancelEvent(Long eventId) {
        Event event = findEvent(eventId);
        event.setStatus(EventStatus.CANCELLED);
        return toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse updateStatus(Long eventId, EventStatus status) {
        if (status == null) {
            throw new BadRequestException("Event status is required.");
        }
        Event event = findEvent(eventId);
        event.setStatus(status);
        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getPublishedEvents(int page, int size, String search) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
        String keyword = search == null || search.isBlank() ? null : search.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDateTime"));
        Page<Event> events = eventRepository.findPublishedEvents(keyword, pageable);
        Map<Long, Long> counts = registeredCounts(events.getContent());
        return events.map(event -> toResponse(event, counts.getOrDefault(event.getId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(int page, int size, EventStatus status) {
        Pageable pageable = createPageable(page, size);
        Page<Event> events = status == null
                ? eventRepository.findAll(pageable)
                : eventRepository.findByStatus(status, pageable);
        Map<Long, Long> counts = registeredCounts(events.getContent());
        return events.map(event -> toResponse(event, counts.getOrDefault(event.getId(), 0L)));
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDateTime"));
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
    }

    private EventResponse toResponse(Event event) {
        long registeredCount = registrationRepository.countByEventIdAndStatus(
                event.getId(), RegistrationStatus.REGISTERED);
        return toResponse(event, registeredCount);
    }

    private EventResponse toResponse(Event event, long registeredCount) {
        return apiMapper.toEventResponse(event, registeredCount);
    }

    private Map<Long, Long> registeredCounts(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        return registrationRepository.countByEventIdsAndStatus(eventIds, RegistrationStatus.REGISTERED).stream()
                .collect(Collectors.toMap(
                        RegistrationRepository.EventRegistrationCount::getEventId,
                        RegistrationRepository.EventRegistrationCount::getRegisteredCount
                ));
    }

    private void validateEventDates(EventRequest request, boolean creating) {
        if (creating && !request.startDateTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Event start date and time must be in the future.");
        }
        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new BadRequestException("Event end date and time must be after the start date and time.");
        }
        if (request.registrationDeadline().isAfter(request.startDateTime())) {
            throw new BadRequestException("Registration deadline must be before or equal to the event start.");
        }
    }
}
