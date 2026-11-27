package com.eventregistration.service;

import org.springframework.data.domain.Page;

import com.eventregistration.dto.EventRequest;
import com.eventregistration.dto.EventResponse;
import com.eventregistration.entity.EventStatus;

public interface EventService {

    EventResponse createEvent(EventRequest request, Long creatorId);

    EventResponse updateEvent(Long eventId, EventRequest request);

    EventResponse getEventById(Long eventId);

    EventResponse cancelEvent(Long eventId);

    EventResponse updateStatus(Long eventId, EventStatus status);

    Page<EventResponse> getPublishedEvents(int page, int size, String search);

    Page<EventResponse> getAllEvents(int page, int size, EventStatus status);
}
