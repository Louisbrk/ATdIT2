package com.spaceflight.support.domain.model;

import com.spaceflight.support.domain.enums.IncidentSeverity;
import com.spaceflight.support.domain.enums.IncidentStatus;
import com.spaceflight.support.domain.enums.ResponsibleRole;
import com.spaceflight.support.domain.enums.SupportActionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Mutable domain entity for an in-flight support incident.
 * The service layer applies business rules before changing the incident state.
 */
public class Incident {

    private final UUID id;
    private final UUID passengerId;
    private final String title;
    private final String description;
    private final IncidentSeverity severity;
    private final LocalDateTime createdAt;
    private final List<IncidentActionLog> actionLog;

    private IncidentStatus status;
    private ResponsibleRole responsibleRole;
    private SupportActionType selectedSupportAction;
    private LocalDateTime updatedAt;

    public Incident(
            UUID id,
            UUID passengerId,
            String title,
            String description,
            IncidentSeverity severity,
            IncidentStatus status,
            ResponsibleRole responsibleRole
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.passengerId = Objects.requireNonNull(passengerId, "passengerId must not be null");
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description.trim();
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.responsibleRole = Objects.requireNonNull(responsibleRole, "responsibleRole must not be null");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
        this.actionLog = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPassengerId() {
        return passengerId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public ResponsibleRole getResponsibleRole() {
        return responsibleRole;
    }

    public SupportActionType getSelectedSupportAction() {
        return selectedSupportAction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<IncidentActionLog> getActionLog() {
        return Collections.unmodifiableList(actionLog);
    }

    public boolean isResolved() {
        return status == IncidentStatus.RESOLVED;
    }

    public void updateStatus(IncidentStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void updateResponsibleRole(ResponsibleRole responsibleRole) {
        this.responsibleRole = Objects.requireNonNull(responsibleRole, "responsibleRole must not be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSelectedSupportAction(SupportActionType supportActionType) {
        this.selectedSupportAction = Objects.requireNonNull(supportActionType, "supportActionType must not be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void appendAction(String actorName, ResponsibleRole actorRole, String description) {
        actionLog.add(new IncidentActionLog(
                UUID.randomUUID(),
                LocalDateTime.now(),
                requireText(actorName, "actorName"),
                Objects.requireNonNull(actorRole, "actorRole must not be null"),
                requireText(description, "description")
        ));
        this.updatedAt = LocalDateTime.now();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
