package org.example.spaceflight.health;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;

/** Classifies a passenger's health status from their current vital signs and flight context. */
public interface HealthEvaluationService {

    HealthEvaluationResult evaluate(VitalSigns vitalSigns, Passenger passenger, FlightPhase phase);
}
