package org.example.spaceflight.simulation;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;

/** Produces the next VitalSigns snapshot for a passenger based on flight phase and experience mode. */
public interface VitalSignsGenerator {

    VitalSigns generateNext(Passenger passenger, FlightPhase phase);

    void configure(int tickIntervalMs);

    void markAsEmergency(String passengerName, long totalFlightTicks);
}
