package com.eventregistration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventregistration.entity.Registration;
import com.eventregistration.entity.RegistrationStatus;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    @EntityGraph(attributePaths = {"event", "user"})
    List<Registration> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"event", "user"})
    List<Registration> findByEventId(Long eventId);

    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    @Query("""
            SELECT registration.event.id AS eventId, COUNT(registration.id) AS registeredCount
            FROM Registration registration
            WHERE registration.event.id IN :eventIds
              AND registration.status = :status
            GROUP BY registration.event.id
            """)
    List<EventRegistrationCount> countByEventIdsAndStatus(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") RegistrationStatus status
    );

    interface EventRegistrationCount {
        Long getEventId();

        Long getRegisteredCount();
    }
}
