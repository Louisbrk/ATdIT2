package com.spaceflight.support.app;

import com.spaceflight.support.repository.IncidentRepository;
import com.spaceflight.support.repository.PassengerRepository;
import com.spaceflight.support.repository.inmemory.InMemoryIncidentRepository;
import com.spaceflight.support.repository.inmemory.InMemoryPassengerRepository;
import com.spaceflight.support.service.IncidentService;
import com.spaceflight.support.service.PassengerService;
import com.spaceflight.support.service.SupportAvailabilityService;
import com.spaceflight.support.service.impl.IncidentServiceImpl;
import com.spaceflight.support.service.impl.PassengerServiceImpl;
import com.spaceflight.support.service.impl.SupportAvailabilityServiceImpl;

public final class ApplicationBootstrap {

    public DashboardContext bootstrap() {
        LoggingConfig.configure();

        PassengerRepository passengerRepository = new InMemoryPassengerRepository();
        IncidentRepository incidentRepository = new InMemoryIncidentRepository();
        SupportAvailabilityService supportAvailabilityService = new SupportAvailabilityServiceImpl(true);

        PassengerService passengerService = new PassengerServiceImpl(passengerRepository);
        IncidentService incidentService = new IncidentServiceImpl(
                incidentRepository,
                passengerRepository,
                supportAvailabilityService
        );

        new DemoDataInitializer(passengerRepository, incidentService).initialize();
        return new DashboardContext(passengerService, incidentService);
    }

    public record DashboardContext(PassengerService passengerService, IncidentService incidentService) {
    }
}
