package com.spaceflight.support.app;

import com.spaceflight.support.domain.enums.IncidentSeverity;
import com.spaceflight.support.domain.enums.IncidentStatus;
import com.spaceflight.support.domain.enums.PassengerTier;
import com.spaceflight.support.domain.enums.ResponsibleRole;
import com.spaceflight.support.domain.enums.SupportActionType;
import com.spaceflight.support.domain.model.Incident;
import com.spaceflight.support.domain.model.Passenger;
import com.spaceflight.support.repository.PassengerRepository;
import com.spaceflight.support.service.IncidentService;
import com.spaceflight.support.service.dto.CreateIncidentCommand;

import java.util.UUID;

public final class DemoDataInitializer {

    private final PassengerRepository passengerRepository;
    private final IncidentService incidentService;

    public DemoDataInitializer(PassengerRepository passengerRepository, IncidentService incidentService) {
        this.passengerRepository = passengerRepository;
        this.incidentService = incidentService;
    }

    public void initialize() {
        Passenger jennifer = passengerRepository.save(new Passenger(
                UUID.randomUUID(),
                "Jennifer Monroe",
                PassengerTier.PREMIUM,
                "A1",
                "High-profile passenger who expects premium reassurance and fast response."
        ));
        Passenger ben = passengerRepository.save(new Passenger(
                UUID.randomUUID(),
                "Ben Cooper",
                PassengerTier.STANDARD,
                "B2",
                "First-time flyer with high emotional investment and moderate anxiety."
        ));
        Passenger lina = passengerRepository.save(new Passenger(
                UUID.randomUUID(),
                "Lina Alvarez",
                PassengerTier.STANDARD,
                "C3",
                "Passenger with mild motion sickness history noted in the pre-flight briefing."
        ));
        passengerRepository.save(new Passenger(
                UUID.randomUUID(),
                "Noah Fischer",
                PassengerTier.STANDARD,
                "D4",
                "Young passenger traveling with parents. Needs calm, simple instructions and extra reassurance during stressful phases."
        ));

        Incident normalCase = incidentService.createIncident(new CreateIncidentCommand(
                ben.getId(),
                "Mild nausea during ascent",
                "Passenger reports motion sickness shortly after launch and asks for support.",
                IncidentSeverity.MEDIUM,
                ben.getFullName(),
                ResponsibleRole.PASSENGER
        ));
        incidentService.moveIncident(
                normalCase.getId(),
                IncidentStatus.ASSESSING,
                DemoActors.ONBOARD_SUPPORT_OPERATOR,
                ResponsibleRole.ONBOARD_SUPPORT
        );
        incidentService.applySupportAction(
                normalCase.getId(),
                SupportActionType.MOTION_SICKNESS_PROTOCOL,
                DemoActors.ONBOARD_SUPPORT_OPERATOR
        );
        incidentService.moveIncident(
                normalCase.getId(),
                IncidentStatus.MONITORING,
                DemoActors.ONBOARD_SUPPORT_OPERATOR,
                ResponsibleRole.ONBOARD_SUPPORT
        );

        incidentService.createIncident(new CreateIncidentCommand(
                jennifer.getId(),
                "Stress before observation phase",
                "Passenger requests reassurance before entering the window observation sequence.",
                IncidentSeverity.LOW,
                jennifer.getFullName(),
                ResponsibleRole.PASSENGER
        ));

        Incident emergencyCase = incidentService.createIncident(new CreateIncidentCommand(
                lina.getId(),
                "Abnormal oxygen reading",
                "Passenger reports chest pressure and dizziness. Suit monitor indicates abnormal values.",
                IncidentSeverity.CRITICAL,
                lina.getFullName(),
                ResponsibleRole.PASSENGER
        ));
        incidentService.takeOverEscalatedIncident(
                emergencyCase.getId(),
                DemoActors.BASE_STATION_OPERATOR
        );
    }
}
