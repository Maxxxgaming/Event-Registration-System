CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    start_date_time DATETIME NOT NULL,
    end_date_time DATETIME NOT NULL,
    registration_deadline DATETIME NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT fk_events_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_events_start_date_time_status (start_date_time, status),
    INDEX idx_events_created_by (created_by)
) ENGINE=InnoDB;

CREATE TABLE registrations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_registrations PRIMARY KEY (id),
    CONSTRAINT uk_registrations_user_event UNIQUE (user_id, event_id),
    CONSTRAINT fk_registrations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_registrations_event FOREIGN KEY (event_id) REFERENCES events (id),
    INDEX idx_registrations_event_id (event_id)
) ENGINE=InnoDB;
