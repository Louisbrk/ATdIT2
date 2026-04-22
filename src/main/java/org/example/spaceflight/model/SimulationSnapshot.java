package org.example.spaceflight.model;

import java.util.List;

/**
 * Immutable snapshot of the complete simulation state at one tick.
 *
 * This is the single data boundary between the simulation engine and the UI
 * (and in the future between server and clients). Every view receives one of
 * these per tick instead of holding live references to Passenger objects or
 * ShuttleState. The class is designed so that it can be serialised to JSON
 * without any further changes when moving to a client-server architecture.
 */
public class SimulationSnapshot {

    private final ShuttleState shuttleState;
    private final List<PassengerSnapshot> passengers;
    private final double emergencyProgress;
    private final long tickCount;

    public SimulationSnapshot(ShuttleState shuttleState,
                              List<Passenger> passengers,
                              double emergencyProgress,
                              long tickCount) {
        // Defensive copy of ShuttleState values via a new object
        this.shuttleState      = copyShuttleState(shuttleState);
        this.passengers        = passengers.stream()
                                           .map(PassengerSnapshot::new)
                                           .toList();
        this.emergencyProgress = emergencyProgress;
        this.tickCount         = tickCount;
    }

    public ShuttleState getShuttleState()            { return shuttleState; }
    public List<PassengerSnapshot> getPassengers()   { return passengers; }
    public double getEmergencyProgress()             { return emergencyProgress; }
    public long getTickCount()                       { return tickCount; }

    private static ShuttleState copyShuttleState(ShuttleState src) {
        ShuttleState copy = new ShuttleState();
        copy.setFuelPercent(src.getFuelPercent());
        copy.setDistanceKm(src.getDistanceKm());
        copy.setAltitudeKm(src.getAltitudeKm());
        copy.setVelocityKmph(src.getVelocityKmph());
        copy.setOxygenPercent(src.getOxygenPercent());
        copy.setCabinTemperature(src.getCabinTemperature());
        copy.setRouteProgress(src.getRouteProgress());
        copy.setFlightPhase(src.getFlightPhase());
        copy.setEmergencyLanding(src.isEmergencyLanding());
        copy.setElapsedSeconds(src.getElapsedSeconds());
        copy.setTotalFlightSeconds(src.getTotalFlightSeconds());
        return copy;
    }
}
