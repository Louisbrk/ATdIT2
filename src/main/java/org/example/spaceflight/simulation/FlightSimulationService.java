package org.example.spaceflight.simulation;

import org.example.spaceflight.model.ShuttleState;

/** Tracks shuttle flight state (altitude, phase, fuel, etc.) and handles emergency landing. */
public interface FlightSimulationService {

    void update(long tickCount);

    void emergencyLanding();

    boolean isEmergencyLanding();

    ShuttleState getCurrentState();

    void setOnEmergencyLanded(Runnable callback);

    double getEmergencyProgress();
}
