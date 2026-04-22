package org.example.spaceflight.health;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;

import java.lang.invoke.MethodHandles;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Classifies passenger health using a weighted z-score algorithm over
 * demographic population baselines.
 *
 * Algorithm summary:
 *  1. Look up the VitalProfile for (age group, gender, experience mode).
 *  2. Per vital: z = |value - mean| / stdDev, capped at 4.0.
 *  3. Per-vital status: z < 1.0 → GREEN, 1.0–2.0 → YELLOW, ≥ 2.0 → RED.
 *  4. Composite score = Σ(z_i × weight_i), medically weighted.
 *  5. Overall status from composite score.
 *  6. Hard floor: worst per-vital status wins — one RED vital → overall RED.
 */
public class WeightedZScoreEvaluationService implements HealthEvaluationService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private static final double MAX_Z          = 4.0;
    private static final double YELLOW_Z_THRESHOLD = 1.0;
    private static final double RED_Z_THRESHOLD    = 2.0;

    private static final double YELLOW_COMPOSITE_THRESHOLD = 1.0;
    private static final double RED_COMPOSITE_THRESHOLD    = 2.0;

    private final IVitalProfileProvider profileTable;

    public WeightedZScoreEvaluationService(IVitalProfileProvider profileTable) {
        this.profileTable = profileTable;
    }

    @Override
    public HealthEvaluationResult evaluate(VitalSigns vitals, Passenger passenger, FlightPhase phase) {
        Map<VitalType, VitalProfile> profiles = profileTable.lookup(
                passenger.getAge(), passenger.getGender(), passenger.getExperienceMode());

        Map<VitalType, Double> rawValues = extractValues(vitals);
        Map<VitalType, HealthStatus> vitalStatuses = new EnumMap<>(VitalType.class);
        double compositeScore = 0.0;

        for (VitalType type : VitalType.values()) {
            VitalProfile profile = profiles.get(type);
            double value = rawValues.getOrDefault(type, profile.mean);

            double z = Math.min(Math.abs(value - profile.mean) / profile.stdDev, MAX_Z);
            HealthStatus vitalStatus = statusFromZ(z);
            vitalStatuses.put(type, vitalStatus);
            compositeScore += z * profile.weight;
        }

        HealthStatus overallFromScore = statusFromComposite(compositeScore);

        // Hard floor: a single critical vital pulls the overall to RED
        HealthStatus overall = applyHardFloor(overallFromScore, vitalStatuses);

        if (overall == HealthStatus.RED) {
            log.info("RED classification for " + passenger.getName()
                    + " (composite=" + String.format("%.2f", compositeScore)
                    + ", mode=" + passenger.getExperienceMode() + ")");
        }

        return HealthEvaluationResult.of(overall, vitalStatuses);
    }

    // -------------------------------------------------------------------------

    private Map<VitalType, Double> extractValues(VitalSigns v) {
        Map<VitalType, Double> m = new EnumMap<>(VitalType.class);
        m.put(VitalType.BPM,          (double) v.getBpm());
        m.put(VitalType.SPO2,         v.getSpO2());
        m.put(VitalType.SYSTOLIC_BP,  (double) v.getSystolicBp());
        m.put(VitalType.DIASTOLIC_BP, (double) v.getDiastolicBp());
        m.put(VitalType.RESP_RATE,    (double) v.getRespiratoryRate());
        return m;
    }

    private HealthStatus statusFromZ(double z) {
        if (z >= RED_Z_THRESHOLD)    return HealthStatus.RED;
        if (z >= YELLOW_Z_THRESHOLD) return HealthStatus.YELLOW;
        return HealthStatus.GREEN;
    }

    private HealthStatus statusFromComposite(double score) {
        if (score >= RED_COMPOSITE_THRESHOLD)    return HealthStatus.RED;
        if (score >= YELLOW_COMPOSITE_THRESHOLD) return HealthStatus.YELLOW;
        return HealthStatus.GREEN;
    }

    private HealthStatus applyHardFloor(HealthStatus base, Map<VitalType, HealthStatus> vitals) {
        HealthStatus worst = base;
        for (HealthStatus s : vitals.values()) {
            if (severity(s) > severity(worst)) worst = s;
        }
        return worst;
    }

    private int severity(HealthStatus s) {
        return switch (s) {
            case GREEN  -> 0;
            case YELLOW -> 1;
            case RED    -> 2;
        };
    }
}
