package org.example.spaceflight.app;

import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.alert.DefaultAlertService;
import org.example.spaceflight.alert.DefaultPsychologicalSupportService;
import org.example.spaceflight.alert.PsychologicalSupportService;
import org.example.spaceflight.model.IPassengerRegistry;
import org.example.spaceflight.model.PassengerRegistry;
import org.example.spaceflight.simulation.DefaultExperienceModeService;
import org.example.spaceflight.simulation.DefaultFlightSimulationService;
import org.example.spaceflight.simulation.DefaultSimulationService;
import org.example.spaceflight.simulation.DefaultVitalSignsGenerator;
import org.example.spaceflight.simulation.ExperienceModeService;
import org.example.spaceflight.simulation.FlightSimulationService;
import org.example.spaceflight.simulation.SimulationService;
import org.example.spaceflight.simulation.VitalSignsGenerator;

/**
 * Central service registry for the running application.
 *
 * This is the only class that knows which concrete implementation backs each
 * service interface. All other classes depend only on the interfaces.
 *
 * Migration note: to move to a client-server setup, introduce a
 * ClientAppContext that provides HTTP-backed implementations of the same
 * interfaces. No view or controller code needs to change.
 */
public class AppContext {

    private final SimulationService simulationService;
    private final FlightSimulationService flightSimulationService;
    private final VitalSignsGenerator vitalSignsGenerator;
    private final IPassengerRegistry passengerRegistry;
    private final AlertService alertService;
    private final PsychologicalSupportService psychService;
    private final ExperienceModeService experienceModeService;

    /** Creates the default local (single-process) context. */
    public AppContext() {
        simulationService       = new DefaultSimulationService();
        flightSimulationService = new DefaultFlightSimulationService();
        vitalSignsGenerator     = new DefaultVitalSignsGenerator();
        passengerRegistry       = new PassengerRegistry();
        alertService            = new DefaultAlertService();
        psychService            = new DefaultPsychologicalSupportService();
        experienceModeService   = new DefaultExperienceModeService();
    }

    public SimulationService getSimulationService()              { return simulationService; }
    public FlightSimulationService getFlightSimulationService()  { return flightSimulationService; }
    public VitalSignsGenerator getVitalSignsGenerator()          { return vitalSignsGenerator; }
    public IPassengerRegistry getPassengerRegistry()             { return passengerRegistry; }
    public AlertService getAlertService()                        { return alertService; }
    public PsychologicalSupportService getPsychService()         { return psychService; }
    public ExperienceModeService getExperienceModeService()      { return experienceModeService; }
}
