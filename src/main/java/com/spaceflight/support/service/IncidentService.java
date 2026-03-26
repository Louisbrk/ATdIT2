package com.spaceflight.support.service;

import com.spaceflight.support.domain.enums.IncidentStatus;
import com.spaceflight.support.domain.enums.ResponsibleRole;
import com.spaceflight.support.domain.enums.SupportActionType;
import com.spaceflight.support.domain.model.Incident;
import com.spaceflight.support.service.dto.CreateIncidentCommand;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for the core in-flight support use case.
 * It contains workflow rules, escalation logic, and action logging.
 */
public interface IncidentService {

    List<Incident> getAllIncidents();

    Incident getIncidentById(UUID incidentId);

    /**
     * Creates a new incident and automatically escalates it when the rules require it.
     */
    Incident createIncident(CreateIncidentCommand command);

    /**
     * Moves an incident to another workflow status if the transition is valid.
     */
    Incident moveIncident(UUID incidentId, IncidentStatus targetStatus, String actorName, ResponsibleRole actorRole);

    Incident applySupportAction(UUID incidentId, SupportActionType supportActionType, String actorName);

    Incident escalateIncident(UUID incidentId, String reason, String actorName, ResponsibleRole actorRole);

    Incident takeOverEscalatedIncident(UUID incidentId, String operatorName);

    Incident resolveIncident(UUID incidentId, String resolutionNote, String actorName, ResponsibleRole actorRole);

    boolean canMoveIncident(UUID incidentId, IncidentStatus targetStatus);

    boolean isOnboardSupportAvailable();

    void updateOnboardSupportAvailability(boolean available, String actorName, ResponsibleRole actorRole);
}
