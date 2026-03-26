package com.spaceflight.support.service.impl;

import com.spaceflight.support.app.DemoActors;
import com.spaceflight.support.domain.enums.IncidentSeverity;
import com.spaceflight.support.domain.enums.IncidentStatus;
import com.spaceflight.support.domain.enums.ResponsibleRole;
import com.spaceflight.support.domain.enums.SupportActionType;
import com.spaceflight.support.domain.exception.EntityNotFoundException;
import com.spaceflight.support.domain.exception.InvalidIncidentOperationException;
import com.spaceflight.support.domain.model.Incident;
import com.spaceflight.support.repository.IncidentRepository;
import com.spaceflight.support.repository.PassengerRepository;
import com.spaceflight.support.service.IncidentService;
import com.spaceflight.support.service.SupportAvailabilityService;
import com.spaceflight.support.service.dto.CreateIncidentCommand;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Default service implementation for the incident workflow.
 * It keeps process logic out of the JavaFX layer and centralizes exception handling.
 */
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final PassengerRepository passengerRepository;
    private final SupportAvailabilityService supportAvailabilityService;
    private final Logger logger = Logger.getLogger(IncidentServiceImpl.class.getName());

    public IncidentServiceImpl(
            IncidentRepository incidentRepository,
            PassengerRepository passengerRepository,
            SupportAvailabilityService supportAvailabilityService
    ) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "incidentRepository must not be null");
        this.passengerRepository = Objects.requireNonNull(passengerRepository, "passengerRepository must not be null");
        this.supportAvailabilityService = Objects.requireNonNull(
                supportAvailabilityService,
                "supportAvailabilityService must not be null"
        );
    }

    @Override
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .sorted(Comparator.comparing(Incident::getUpdatedAt).reversed())
                .toList();
    }

    @Override
    public Incident getIncidentById(UUID incidentId) {
        return findIncident(incidentId);
    }

    @Override
    public Incident createIncident(CreateIncidentCommand command) {
        validateCreateIncidentCommand(command);
        passengerRepository.findById(command.passengerId())
                .orElseThrow(() -> new EntityNotFoundException("Passenger not found: " + command.passengerId()));

        Incident incident = new Incident(
                UUID.randomUUID(),
                command.passengerId(),
                command.title().trim(),
                normalizeOptionalText(command.description()),
                command.severity(),
                IncidentStatus.NEW,
                ResponsibleRole.ONBOARD_SUPPORT
        );
        incident.appendAction(
                sanitizeActorName(command.reportedByName()),
                command.reportedByRole(),
                "Incident reported: " + incident.getTitle() + "."
        );

        if (shouldAutoEscalate(command.severity())) {
            String reason = command.severity() == IncidentSeverity.CRITICAL
                    ? "Automatic escalation because severity is CRITICAL."
                    : "Automatic escalation because onboard support is unavailable.";
            applyEscalation(incident, reason, DemoActors.SYSTEM_ACTOR, ResponsibleRole.SYSTEM);
        }

        incidentRepository.save(incident);
        logger.info(() -> "Created incident " + incident.getId() + " for passenger " + command.passengerId());
        return incident;
    }

    @Override
    public Incident moveIncident(UUID incidentId, IncidentStatus targetStatus, String actorName, ResponsibleRole actorRole) {
        Incident incident = findIncident(incidentId);
        IncidentStatus sanitizedTargetStatus = Objects.requireNonNull(targetStatus, "targetStatus must not be null");
        ResponsibleRole sanitizedActorRole = Objects.requireNonNull(actorRole, "actorRole must not be null");
        String sanitizedActorName = sanitizeActorName(actorName);

        if (sanitizedTargetStatus == IncidentStatus.ESCALATED) {
            return escalateIncident(incidentId, "Manual escalation requested.", sanitizedActorName, sanitizedActorRole);
        }
        if (sanitizedTargetStatus == IncidentStatus.RESOLVED) {
            return resolveIncident(
                    incidentId,
                    "Resolved via workflow board.",
                    sanitizedActorName,
                    sanitizedActorRole
            );
        }
        if (!canMoveIncident(incidentId, sanitizedTargetStatus)) {
            throw new InvalidIncidentOperationException(
                    "Invalid transition from " + incident.getStatus().getDisplayName()
                            + " to " + sanitizedTargetStatus.getDisplayName() + "."
            );
        }

        validateActorCanHandleIncident(incident, sanitizedActorRole, sanitizedTargetStatus);
        IncidentStatus previousStatus = incident.getStatus();
        incident.updateStatus(sanitizedTargetStatus);
        incident.updateResponsibleRole(determineResponsibleRole(incident, sanitizedTargetStatus));
        incident.appendAction(
                sanitizedActorName,
                sanitizedActorRole,
                "Status changed from " + previousStatus.getDisplayName()
                        + " to " + sanitizedTargetStatus.getDisplayName() + "."
        );

        incidentRepository.save(incident);
        logger.info(() -> "Moved incident " + incidentId + " to " + sanitizedTargetStatus);
        return incident;
    }

    @Override
    public Incident applySupportAction(UUID incidentId, SupportActionType supportActionType, String actorName) {
        Incident incident = findIncident(incidentId);
        Objects.requireNonNull(supportActionType, "supportActionType must not be null");
        String sanitizedActorName = sanitizeActorName(actorName);

        if (!EnumSet.of(IncidentStatus.ASSESSING, IncidentStatus.MONITORING).contains(incident.getStatus())) {
            throw new InvalidIncidentOperationException(
                    "Support actions can only be applied while assessing or monitoring an incident."
            );
        }
        if (incident.getResponsibleRole() != ResponsibleRole.ONBOARD_SUPPORT) {
            throw new InvalidIncidentOperationException("Only onboard support can apply support actions.");
        }
        if (!supportAvailabilityService.isOnboardSupportAvailable()) {
            throw new InvalidIncidentOperationException(
                    "Onboard support is unavailable. Escalate the incident to base station instead."
            );
        }

        incident.updateSelectedSupportAction(supportActionType);
        incident.appendAction(
                sanitizedActorName,
                ResponsibleRole.ONBOARD_SUPPORT,
                "Support action selected: " + supportActionType.getDisplayName() + "."
        );

        incidentRepository.save(incident);
        logger.info(() -> "Applied support action " + supportActionType + " to incident " + incidentId);
        return incident;
    }

    @Override
    public Incident escalateIncident(UUID incidentId, String reason, String actorName, ResponsibleRole actorRole) {
        Incident incident = findIncident(incidentId);
        String sanitizedReason = requireText(reason, "reason");
        String sanitizedActorName = sanitizeActorName(actorName);
        ResponsibleRole sanitizedActorRole = Objects.requireNonNull(actorRole, "actorRole must not be null");

        if (incident.isResolved()) {
            throw new InvalidIncidentOperationException("Resolved incidents cannot be escalated.");
        }
        if (sanitizedActorRole == ResponsibleRole.PASSENGER) {
            throw new InvalidIncidentOperationException("Passengers cannot escalate incidents directly.");
        }

        applyEscalation(incident, sanitizedReason, sanitizedActorName, sanitizedActorRole);
        incidentRepository.save(incident);
        logger.info(() -> "Escalated incident " + incidentId);
        return incident;
    }

    @Override
    public Incident takeOverEscalatedIncident(UUID incidentId, String operatorName) {
        Incident incident = findIncident(incidentId);
        String sanitizedOperatorName = sanitizeActorName(operatorName);

        if (incident.getStatus() != IncidentStatus.ESCALATED) {
            throw new InvalidIncidentOperationException("Only escalated incidents can be taken over by base station.");
        }

        incident.updateResponsibleRole(ResponsibleRole.BASE_STATION);
        incident.appendAction(
                sanitizedOperatorName,
                ResponsibleRole.BASE_STATION,
                "Base station operator took over the incident."
        );

        incidentRepository.save(incident);
        logger.info(() -> "Base station took over incident " + incidentId);
        return incident;
    }

    @Override
    public Incident resolveIncident(UUID incidentId, String resolutionNote, String actorName, ResponsibleRole actorRole) {
        Incident incident = findIncident(incidentId);
        String sanitizedResolutionNote = requireText(resolutionNote, "resolutionNote");
        String sanitizedActorName = sanitizeActorName(actorName);
        ResponsibleRole sanitizedActorRole = Objects.requireNonNull(actorRole, "actorRole must not be null");

        if (!IncidentWorkflowRules.canTransition(incident.getStatus(), IncidentStatus.RESOLVED)) {
            throw new InvalidIncidentOperationException(
                    "Incidents in status " + incident.getStatus().getDisplayName() + " cannot be resolved yet."
            );
        }
        if (incident.getResponsibleRole() == ResponsibleRole.ONBOARD_SUPPORT
                && !supportAvailabilityService.isOnboardSupportAvailable()) {
            throw new InvalidIncidentOperationException(
                    "Onboard support is unavailable. The incident must be escalated first."
            );
        }

        validateActorCanHandleIncident(incident, sanitizedActorRole, IncidentStatus.RESOLVED);
        IncidentStatus previousStatus = incident.getStatus();
        incident.updateStatus(IncidentStatus.RESOLVED);
        incident.updateResponsibleRole(incident.getResponsibleRole() == ResponsibleRole.BASE_STATION
                ? ResponsibleRole.BASE_STATION
                : sanitizedActorRole);
        incident.appendAction(
                sanitizedActorName,
                sanitizedActorRole,
                "Status changed from " + previousStatus.getDisplayName()
                        + " to Resolved. Resolution note: " + sanitizedResolutionNote
        );

        incidentRepository.save(incident);
        logger.info(() -> "Resolved incident " + incidentId);
        return incident;
    }

    @Override
    public boolean canMoveIncident(UUID incidentId, IncidentStatus targetStatus) {
        Incident incident = findIncident(incidentId);
        IncidentStatus sanitizedTargetStatus = Objects.requireNonNull(targetStatus, "targetStatus must not be null");

        if (incident.getStatus() == sanitizedTargetStatus) {
            return false;
        }
        if (!IncidentWorkflowRules.canTransition(incident.getStatus(), sanitizedTargetStatus)) {
            return false;
        }
        return incident.getResponsibleRole() == ResponsibleRole.BASE_STATION
                || supportAvailabilityService.isOnboardSupportAvailable()
                || sanitizedTargetStatus == IncidentStatus.ESCALATED;
    }

    @Override
    public boolean isOnboardSupportAvailable() {
        return supportAvailabilityService.isOnboardSupportAvailable();
    }

    @Override
    public void updateOnboardSupportAvailability(boolean available, String actorName, ResponsibleRole actorRole) {
        String sanitizedActorName = sanitizeActorName(actorName);
        ResponsibleRole sanitizedActorRole = Objects.requireNonNull(actorRole, "actorRole must not be null");
        boolean previousAvailability = supportAvailabilityService.isOnboardSupportAvailable();

        supportAvailabilityService.setOnboardSupportAvailable(available);
        logger.info(() -> "Onboard support availability updated from " + previousAvailability + " to " + available);

        if (previousAvailability && !available) {
            incidentRepository.findAll().stream()
                    .filter(incident -> !incident.isResolved())
                    .filter(incident -> incident.getResponsibleRole() != ResponsibleRole.BASE_STATION)
                    .forEach(incident -> {
                        applyEscalation(
                                incident,
                                "Fallback escalation because onboard support is unavailable.",
                                sanitizedActorName,
                                sanitizedActorRole
                        );
                        incidentRepository.save(incident);
                    });
        }
    }

    private Incident findIncident(UUID incidentId) {
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found: " + incidentId));
    }

    private boolean shouldAutoEscalate(IncidentSeverity severity) {
        return severity == IncidentSeverity.CRITICAL || !supportAvailabilityService.isOnboardSupportAvailable();
    }

    private void validateCreateIncidentCommand(CreateIncidentCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.passengerId(), "passengerId must not be null");
        requireText(command.title(), "title");
        Objects.requireNonNull(command.severity(), "severity must not be null");
        Objects.requireNonNull(command.reportedByRole(), "reportedByRole must not be null");
    }

    private void validateActorCanHandleIncident(
            Incident incident,
            ResponsibleRole actorRole,
            IncidentStatus targetStatus
    ) {
        if (actorRole == ResponsibleRole.SYSTEM) {
            return;
        }
        if (targetStatus == IncidentStatus.ESCALATED) {
            if (actorRole == ResponsibleRole.PASSENGER) {
                throw new InvalidIncidentOperationException("Passengers cannot escalate workflow status.");
            }
            return;
        }
        if (incident.getResponsibleRole() == ResponsibleRole.BASE_STATION && actorRole != ResponsibleRole.BASE_STATION) {
            throw new InvalidIncidentOperationException("This incident is currently handled by base station.");
        }
        if (incident.getResponsibleRole() == ResponsibleRole.ONBOARD_SUPPORT
                && actorRole != ResponsibleRole.ONBOARD_SUPPORT) {
            throw new InvalidIncidentOperationException("This incident is currently handled by onboard support.");
        }
    }

    private ResponsibleRole determineResponsibleRole(Incident incident, IncidentStatus targetStatus) {
        if (targetStatus == IncidentStatus.ESCALATED || incident.getResponsibleRole() == ResponsibleRole.BASE_STATION) {
            return ResponsibleRole.BASE_STATION;
        }
        return ResponsibleRole.ONBOARD_SUPPORT;
    }

    private void applyEscalation(Incident incident, String reason, String actorName, ResponsibleRole actorRole) {
        String sanitizedReason = requireText(reason, "reason");
        IncidentStatus previousStatus = incident.getStatus();

        if (previousStatus != IncidentStatus.ESCALATED) {
            if (!IncidentWorkflowRules.canTransition(previousStatus, IncidentStatus.ESCALATED)) {
                throw new InvalidIncidentOperationException(
                        "Incidents in status " + previousStatus.getDisplayName() + " cannot be escalated."
                );
            }
            incident.updateStatus(IncidentStatus.ESCALATED);
            incident.updateResponsibleRole(ResponsibleRole.BASE_STATION);
            incident.appendAction(
                    actorName,
                    actorRole,
                    "Status changed from " + previousStatus.getDisplayName() + " to Escalated. Reason: " + sanitizedReason
            );
            return;
        }

        incident.updateResponsibleRole(ResponsibleRole.BASE_STATION);
        incident.appendAction(
                actorName,
                actorRole,
                "Escalation note recorded. Reason: " + sanitizedReason
        );
    }

    private String sanitizeActorName(String actorName) {
        if (actorName == null || actorName.isBlank()) {
            return DemoActors.SYSTEM_ACTOR;
        }
        return actorName.trim();
    }

    private String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidIncidentOperationException(fieldName + " must not be blank.");
        }
        return value.trim();
    }
}
