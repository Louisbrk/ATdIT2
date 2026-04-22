package org.example.spaceflight.health;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.Passenger;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Runs health evaluation for all passengers each tick.
 * Owns the evaluation service and caches the latest results.
 * Passengers with a manual override are skipped.
 */
public class HealthEvaluationOrchestrator implements IHealthEvaluationOrchestrator {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final HealthEvaluationService evaluationService;
    private final Map<Passenger, HealthEvaluationResult> latestResults = new LinkedHashMap<>();

    public HealthEvaluationOrchestrator(HealthEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Override
    public void evaluate(List<Passenger> passengers, FlightPhase phase) {
        for (Passenger p : passengers) {
            if (p.isManualOverride() || p.getVitalSigns() == null) continue;
            HealthEvaluationResult result = evaluationService.evaluate(p.getVitalSigns(), p, phase);
            p.setHealthStatus(result.getOverallStatus());
            latestResults.put(p, result);
            log.fine("Evaluated " + p.getName() + " -> " + result.getOverallStatus());
        }
    }

    @Override
    public HealthEvaluationResult getLatestResult(Passenger passenger) {
        return latestResults.getOrDefault(passenger, HealthEvaluationResult.allGreen());
    }
}
