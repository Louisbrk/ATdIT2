package org.example.spaceflight.health;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.Passenger;

import java.util.List;

public interface IHealthEvaluationOrchestrator {

    /** Evaluate all passengers for the given phase, update their health status, and cache results. */
    void evaluate(List<Passenger> passengers, FlightPhase phase);

    /** Returns the most recent evaluation result for a passenger, or allGreen if not yet evaluated. */
    HealthEvaluationResult getLatestResult(Passenger passenger);
}
