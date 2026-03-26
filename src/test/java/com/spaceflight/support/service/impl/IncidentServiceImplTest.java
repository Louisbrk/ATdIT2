package com.spaceflight.support.service.impl;

import com.spaceflight.support.app.DemoActors;
import com.spaceflight.support.domain.enums.IncidentSeverity;
import com.spaceflight.support.domain.enums.IncidentStatus;
import com.spaceflight.support.domain.enums.PassengerTier;
import com.spaceflight.support.domain.enums.ResponsibleRole;
import com.spaceflight.support.domain.enums.SupportActionType;
import com.spaceflight.support.domain.exception.InvalidIncidentOperationException;
import com.spaceflight.support.domain.model.Incident;
import com.spaceflight.support.domain.model.Passenger;
import com.spaceflight.support.repository.IncidentRepository;
import com.spaceflight.support.repository.PassengerRepository;
import com.spaceflight.support.repository.inmemory.InMemoryIncidentRepository;
import com.spaceflight.support.repository.inmemory.InMemoryPassengerRepository;
import com.spaceflight.support.service.IncidentService;
import com.spaceflight.support.service.SupportAvailabilityService;
import com.spaceflight.support.service.dto.CreateIncidentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentServiceImplTest {

    private IncidentService incidentService;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        PassengerRepository passengerRepository = new InMemoryPassengerRepository();
        IncidentRepository incidentRepository = new InMemoryIncidentRepository();
        SupportAvailabilityService supportAvailabilityService = new SupportAvailabilityServiceImpl(true);

        passenger = passengerRepository.save(new Passenger(
                UUID.randomUUID(),
                "Ben Cooper",
                PassengerTier.STANDARD,
                "B2",
                "First-time flyer who may become anxious during the trip."
        ));

        incidentService = new IncidentServiceImpl(
                incidentRepository,
                passengerRepository,
                supportAvailabilityService
        );
    }

    @Test
    void createsIncidentInNewStatus() {
        Incident incident = createIncident(IncidentSeverity.LOW, "Passenger feels stressed");

        assertEquals(IncidentStatus.NEW, incident.getStatus());
        assertEquals(ResponsibleRole.ONBOARD_SUPPORT, incident.getResponsibleRole());
        assertEquals(1, incident.getActionLog().size());
    }

    @Test
    void automaticallyEscalatesCriticalIncidents() {
        Incident incident = createIncident(IncidentSeverity.CRITICAL, "Passenger collapses");

        assertEquals(IncidentStatus.ESCALATED, incident.getStatus());
        assertEquals(ResponsibleRole.BASE_STATION, incident.getResponsibleRole());
        assertTrue(incident.getActionLog().stream()
                .anyMatch(log -> log.getDescription().contains("severity is CRITICAL")));
    }

    @Test
    void escalatesOpenIncidentsWhenOnboardSupportBecomesUnavailable() {
        Incident incident = createIncident(IncidentSeverity.MEDIUM, "Passenger feels nauseous");

        incidentService.updateOnboardSupportAvailability(
                false,
                DemoActors.BASE_STATION_OPERATOR,
                ResponsibleRole.BASE_STATION
        );

        Incident updatedIncident = incidentService.getIncidentById(incident.getId());
        assertEquals(IncidentStatus.ESCALATED, updatedIncident.getStatus());
        assertEquals(ResponsibleRole.BASE_STATION, updatedIncident.getResponsibleRole());
        assertTrue(updatedIncident.getActionLog().stream()
                .anyMatch(log -> log.getDescription().contains("Fallback escalation")));
    }

    @Test
    void allowsAndRejectsWorkflowTransitions() {
        Incident incident = createIncident(IncidentSeverity.MEDIUM, "Passenger reports disorientation");

        Incident assessingIncident = incidentService.moveIncident(
                incident.getId(),
                IncidentStatus.ASSESSING,
                DemoActors.ONBOARD_SUPPORT_OPERATOR,
                ResponsibleRole.ONBOARD_SUPPORT
        );

        assertEquals(IncidentStatus.ASSESSING, assessingIncident.getStatus());

        assertThrows(
                InvalidIncidentOperationException.class,
                () -> incidentService.moveIncident(
                        incident.getId(),
                        IncidentStatus.NEW,
                        DemoActors.ONBOARD_SUPPORT_OPERATOR,
                        ResponsibleRole.ONBOARD_SUPPORT
                )
        );
    }

    @Test
    void recordsActionLogEntriesForSupportHandling() {
        Incident incident = createIncident(IncidentSeverity.MEDIUM, "Passenger reports dizziness");

        incidentService.moveIncident(
                incident.getId(),
                IncidentStatus.ASSESSING,
                DemoActors.ONBOARD_SUPPORT_OPERATOR,
                ResponsibleRole.ONBOARD_SUPPORT
        );
        incidentService.applySupportAction(
                incident.getId(),
                SupportActionType.GUIDED_BREATHING,
                DemoActors.ONBOARD_SUPPORT_OPERATOR
        );

        Incident updatedIncident = incidentService.getIncidentById(incident.getId());
        assertEquals(3, updatedIncident.getActionLog().size());
        assertTrue(updatedIncident.getActionLog().stream()
                .anyMatch(log -> log.getDescription().contains("Guided Breathing")));
    }

    @Test
    void resolvesIncidentAfterMonitoring() {
        Incident incident = createIncident(IncidentSeverity.MEDIUM, "Passenger reports motion sickness");

        incidentService.moveIncident(
                incident.getId(),
                IncidentStatus.ASSESSING,
                DemoActors.ONBOARD_SUPPORT_OPERATOR,
                ResponsibleRole.ONBOARD_SUPPORT
        );
        incidentService.moveIncident(
                incident.getId(),
                IncidentStatus.MONITORING,
                DemoActors.ONBOARD_SUPPORT_OPERATOR,
                ResponsibleRole.ONBOARD_SUPPORT
        );

        Incident resolvedIncident = incidentService.resolveIncident(
                incident.getId(),
                "Passenger stabilized after guided intervention.",
                DemoActors.ONBOARD_SUPPORT_OPERATOR,
                ResponsibleRole.ONBOARD_SUPPORT
        );

        assertEquals(IncidentStatus.RESOLVED, resolvedIncident.getStatus());
        assertTrue(resolvedIncident.getActionLog().stream()
                .anyMatch(log -> log.getDescription().contains("Resolution note")));
    }

    private Incident createIncident(IncidentSeverity severity, String title) {
        return incidentService.createIncident(new CreateIncidentCommand(
                passenger.getId(),
                title,
                "Demo incident description",
                severity,
                passenger.getFullName(),
                ResponsibleRole.PASSENGER
        ));
    }
}
