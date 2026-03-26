package com.spaceflight.support.domain.model;

import com.spaceflight.support.domain.enums.ResponsibleRole;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class IncidentActionLog {

    private final UUID id;
    private final LocalDateTime timestamp;
    private final String actorName;
    private final ResponsibleRole actorRole;
    private final String description;

    public IncidentActionLog(UUID id, LocalDateTime timestamp, String actorName, ResponsibleRole actorRole, String description) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.actorName = requireText(actorName, "actorName");
        this.actorRole = Objects.requireNonNull(actorRole, "actorRole must not be null");
        this.description = requireText(description, "description");
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getActorName() {
        return actorName;
    }

    public ResponsibleRole getActorRole() {
        return actorRole;
    }

    public String getDescription() {
        return description;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
