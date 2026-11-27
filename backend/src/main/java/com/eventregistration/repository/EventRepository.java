package com.eventregistration.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventregistration.entity.Event;
import com.eventregistration.entity.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Override
    @EntityGraph(attributePaths = "createdBy")
    Page<Event> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "createdBy")
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "createdBy")
    @Query("""
            SELECT event
            FROM Event event
            WHERE event.status = com.eventregistration.entity.EventStatus.PUBLISHED
              AND (:search IS NULL
                   OR LOWER(event.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(event.location) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Event> findPublishedEvents(@Param("search") String search, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT event FROM Event event WHERE event.id = :eventId")
    Optional<Event> findByIdForUpdate(@Param("eventId") Long eventId);
}
