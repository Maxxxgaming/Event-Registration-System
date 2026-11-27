CREATE INDEX idx_events_status_start_date_time
    ON events (status, start_date_time);

CREATE INDEX idx_registrations_event_status
    ON registrations (event_id, status);

DROP INDEX idx_registrations_event_id ON registrations;
