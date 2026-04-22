package org.example.spaceflight.health;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * k-Nearest Neighbours health classifier.
 *
 * Algorithm:
 *  1. Receive labelled training cases from an ITrainingDataLoader at construction.
 *  2. Normalise each of the 5 vital features to [0,1] using per-feature min/max
 *     derived from the training set.
 *  3. To classify a new observation, compute the weighted Euclidean distance to
 *     every training case. Demographic features (age group, gender, mode) add a
 *     small context bonus that reduces the distance for matching segments.
 *  4. Take the k=5 nearest neighbours and apply majority vote → overall status.
 *  5. Per-vital status is derived independently via the VitalProfileTable z-scores
 *     (same as WeightedZScoreEvaluationService) so individual vitals are still
 *     highlighted correctly.
 *
 * Feature weights (reflect medical importance, same rationale as z-score service):
 *   SpO2 0.30 · Systolic 0.25 · BPM 0.20 · Diastolic 0.15 · RespRate 0.10
 */
public class KnnHealthEvaluationService implements HealthEvaluationService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private static final int K = 5;
    private static final int HYSTERESIS_TICKS = 7; // ticks a new status must hold before committing

    // Clinically dangerous absolute thresholds — override everything including hysteresis
    private static final double CRITICAL_SPO2_FLOOR = 91.5;
    private static final int    CRITICAL_BPM_MAX    = 155;
    private static final int    CRITICAL_BPM_MIN    = 38;

    // Feature importance weights – must sum to 1.0
    private static final double W_BPM  = 0.20;
    private static final double W_SPO2 = 0.30;
    private static final double W_SYS  = 0.25;
    private static final double W_DIAS = 0.15;
    private static final double W_RR   = 0.10;

    // Bonus subtracted from distance when demographic segment matches
    private static final double DEMO_MATCH_BONUS = 0.08;

    private final List<TrainingCase> trainingSet;
    private final IVitalProfileProvider profileTable;

    // Per-passenger hysteresis: status must persist for HYSTERESIS_TICKS before it is committed
    private final Map<String, StatusBuffer> statusBuffers = new HashMap<>();

    // Per-feature normalisation ranges (derived from training data)
    private double bpmMin, bpmMax;
    private double spo2Min, spo2Max;
    private double sysMin, sysMax;
    private double diasMin, diasMax;
    private double rrMin, rrMax;

    public KnnHealthEvaluationService(IVitalProfileProvider profileTable, ITrainingDataLoader loader) {
        this.profileTable = profileTable;
        this.trainingSet  = loader.load();
        if (!trainingSet.isEmpty()) {
            computeNormalisationRanges();
        }
        log.info("kNN classifier ready – " + trainingSet.size() + " training cases loaded (k=" + K + ")");
    }

    @Override
    public HealthEvaluationResult evaluate(VitalSigns vitals, Passenger passenger, FlightPhase phase) {
        if (vitals == null || trainingSet.isEmpty()) return HealthEvaluationResult.allGreen();

        double[] query = normalise(vitals);
        AgeGroup ageGroup = AgeGroup.fromAge(passenger.getAge());

        // Compute distances to all training cases and pick k nearest
        List<Neighbour> neighbours = new ArrayList<>(trainingSet.size());
        for (TrainingCase tc : trainingSet) {
            double dist = weightedDistance(query, tc.features);
            // Reduce distance for demographically similar cases
            if (tc.ageGroup == ageGroup)                      dist -= DEMO_MATCH_BONUS;
            if (tc.gender   == passenger.getGender())         dist -= DEMO_MATCH_BONUS;
            if (tc.mode     == passenger.getExperienceMode()) dist -= DEMO_MATCH_BONUS;
            neighbours.add(new Neighbour(Math.max(0, dist), tc.label));
        }
        neighbours.sort(Comparator.comparingDouble(n -> n.distance));
        List<Neighbour> kNearest = neighbours.subList(0, Math.min(K, neighbours.size()));

        HealthStatus overall = majorityVote(kNearest);

        // Per-vital status via z-score, with phase-aware tolerance
        Map<VitalType, HealthStatus> vitalStatuses = computePerVitalStatuses(vitals, passenger, phase);

        // Escalation rules — overall = max(kNN, z-score escalation), never downgraded
        int redCount = 0, yellowCount = 0;
        for (HealthStatus s : vitalStatuses.values()) {
            if (s == HealthStatus.RED)    redCount++;
            if (s == HealthStatus.YELLOW) yellowCount++;
        }

        if (redCount >= 2) {
            overall = HealthStatus.RED;
        } else if (redCount == 1 || yellowCount >= 1) {
            if (overall == HealthStatus.GREEN) overall = HealthStatus.YELLOW;
        }

        if (overall == HealthStatus.RED) {
            log.info("kNN RED classification for " + passenger.getName()
                    + " (nearest: " + kNearest.get(0).label + ", "
                    + kNearest.get(1).label + ", " + kNearest.get(2).label + ")");
        }

        // Absolute safety floors — override everything including hysteresis.
        // These values are clinically dangerous regardless of demographics or mode.
        if (vitals.getSpO2() < CRITICAL_SPO2_FLOOR) {
            log.warning("Safety floor triggered for " + passenger.getName()
                    + ": SpO2=" + vitals.getSpO2() + " < " + CRITICAL_SPO2_FLOOR + "% → RED");
            return HealthEvaluationResult.of(HealthStatus.RED, vitalStatuses);
        }
        if (vitals.getBpm() > CRITICAL_BPM_MAX || vitals.getBpm() < CRITICAL_BPM_MIN) {
            log.warning("Safety floor triggered for " + passenger.getName()
                    + ": BPM=" + vitals.getBpm() + " → RED");
            return HealthEvaluationResult.of(HealthStatus.RED, vitalStatuses);
        }

        // Hysteresis: only commit a status change after it has held for HYSTERESIS_TICKS
        StatusBuffer buf = statusBuffers.computeIfAbsent(passenger.getName(), k -> new StatusBuffer());
        overall = buf.commit(overall);

        return HealthEvaluationResult.of(overall, vitalStatuses);
    }

    // -------------------------------------------------------------------------
    // Per-vital status (reuses z-score logic from profile table)
    // -------------------------------------------------------------------------

    private Map<VitalType, HealthStatus> computePerVitalStatuses(VitalSigns v, Passenger p, FlightPhase phase) {
        Map<VitalType, VitalProfile> profiles = profileTable.lookup(
                p.getAge(), p.getGender(), p.getExperienceMode());
        Map<VitalType, HealthStatus> result = new EnumMap<>(VitalType.class);

        // During high-stress phases (ascent, descent, pre-flight) the body is naturally
        // pushed harder — raise z-score thresholds so physiologically expected elevations
        // are not flagged as warnings.
        double yellowThreshold = switch (phase) {
            case ASCENT, DESCENT -> 1.6;  // tolerate more deviation during stress phases
            case PRE_FLIGHT      -> 1.3;  // mild tolerance for pre-launch nerves
            case ORBIT, LANDED   -> 1.3;  // slightly relaxed: microgravity causes mild physiological shifts
        };
        double redThreshold = switch (phase) {
            case ASCENT, DESCENT -> 2.8;
            case PRE_FLIGHT      -> 2.3;
            case ORBIT, LANDED   -> 2.2;  // slightly raised: orbit/landed are calmer but not zero-stress
        };

        result.put(VitalType.BPM,          zStatus(v.getBpm(),             profiles.get(VitalType.BPM),          yellowThreshold, redThreshold));
        result.put(VitalType.SPO2,         zStatus(v.getSpO2(),            profiles.get(VitalType.SPO2),         yellowThreshold, redThreshold));
        result.put(VitalType.SYSTOLIC_BP,  zStatus(v.getSystolicBp(),      profiles.get(VitalType.SYSTOLIC_BP),  yellowThreshold, redThreshold));
        result.put(VitalType.DIASTOLIC_BP, zStatus(v.getDiastolicBp(),     profiles.get(VitalType.DIASTOLIC_BP), yellowThreshold, redThreshold));
        result.put(VitalType.RESP_RATE,    zStatus(v.getRespiratoryRate(), profiles.get(VitalType.RESP_RATE),    yellowThreshold, redThreshold));
        return result;
    }

    private HealthStatus zStatus(double value, VitalProfile profile, double yellowThreshold, double redThreshold) {
        if (profile == null) return HealthStatus.GREEN;
        double z = Math.min(Math.abs(value - profile.mean) / profile.stdDev, 4.0);
        if (z >= redThreshold)    return HealthStatus.RED;
        if (z >= yellowThreshold) return HealthStatus.YELLOW;
        return HealthStatus.GREEN;
    }

    // -------------------------------------------------------------------------
    // Distance + voting
    // -------------------------------------------------------------------------

    private double weightedDistance(double[] query, double[] training) {
        double d = 0;
        d += W_BPM  * sq(query[0] - training[0]);
        d += W_SPO2 * sq(query[1] - training[1]);
        d += W_SYS  * sq(query[2] - training[2]);
        d += W_DIAS * sq(query[3] - training[3]);
        d += W_RR   * sq(query[4] - training[4]);
        return Math.sqrt(d);
    }

    private HealthStatus majorityVote(List<Neighbour> kNearest) {
        int green = 0, yellow = 0, red = 0;
        for (Neighbour n : kNearest) {
            switch (n.label) {
                case GREEN  -> green++;
                case YELLOW -> yellow++;
                case RED    -> red++;
            }
        }
        if (red    >= green && red    >= yellow) return HealthStatus.RED;
        if (yellow >= green)                     return HealthStatus.YELLOW;
        return HealthStatus.GREEN;
    }

    // -------------------------------------------------------------------------
    // Normalisation
    // -------------------------------------------------------------------------

    private double[] normalise(VitalSigns v) {
        return new double[]{
            norm(v.getBpm(),             bpmMin,  bpmMax),
            norm(v.getSpO2(),            spo2Min, spo2Max),
            norm(v.getSystolicBp(),      sysMin,  sysMax),
            norm(v.getDiastolicBp(),     diasMin, diasMax),
            norm(v.getRespiratoryRate(), rrMin,   rrMax)
        };
    }

    private double norm(double value, double min, double max) {
        if (max == min) return 0.5;
        return Math.max(0, Math.min(1, (value - min) / (max - min)));
    }

    private void computeNormalisationRanges() {
        bpmMin  = bpmMax  = trainingSet.getFirst().rawBpm;
        spo2Min = spo2Max = trainingSet.getFirst().rawSpo2;
        sysMin  = sysMax  = trainingSet.getFirst().rawSys;
        diasMin = diasMax = trainingSet.getFirst().rawDias;
        rrMin   = rrMax   = trainingSet.getFirst().rawRr;

        for (TrainingCase tc : trainingSet) {
            bpmMin  = Math.min(bpmMin,  tc.rawBpm);  bpmMax  = Math.max(bpmMax,  tc.rawBpm);
            spo2Min = Math.min(spo2Min, tc.rawSpo2); spo2Max = Math.max(spo2Max, tc.rawSpo2);
            sysMin  = Math.min(sysMin,  tc.rawSys);  sysMax  = Math.max(sysMax,  tc.rawSys);
            diasMin = Math.min(diasMin, tc.rawDias);  diasMax = Math.max(diasMax, tc.rawDias);
            rrMin   = Math.min(rrMin,   tc.rawRr);   rrMax   = Math.max(rrMax,   tc.rawRr);
        }

        // Pre-normalise training features in place
        for (TrainingCase tc : trainingSet) {
            tc.features[0] = norm(tc.rawBpm,  bpmMin,  bpmMax);
            tc.features[1] = norm(tc.rawSpo2, spo2Min, spo2Max);
            tc.features[2] = norm(tc.rawSys,  sysMin,  sysMax);
            tc.features[3] = norm(tc.rawDias, diasMin, diasMax);
            tc.features[4] = norm(tc.rawRr,   rrMin,   rrMax);
        }
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    private static double sq(double x) { return x * x; }

    private static class Neighbour {
        final double distance;
        final HealthStatus label;
        Neighbour(double distance, HealthStatus label) {
            this.distance = distance;
            this.label    = label;
        }
    }

    /**
     * Sliding-window hysteresis buffer.
     * A new status is only committed (returned to the caller) once it has appeared
     * in HYSTERESIS_TICKS consecutive evaluations. Until then, the last committed
     * status is returned, preventing rapid GREEN↔YELLOW flickering.
     */
    private static class StatusBuffer {
        private HealthStatus committed = HealthStatus.GREEN;
        private HealthStatus candidate = HealthStatus.GREEN;
        private int streak = 0;

        HealthStatus commit(HealthStatus raw) {
            if (raw == candidate) {
                streak++;
            } else {
                candidate = raw;
                streak = 1;
            }
            // Always allow immediate escalation to RED (safety-critical)
            if (raw == HealthStatus.RED || streak >= HYSTERESIS_TICKS) {
                committed = candidate;
            }
            return committed;
        }
    }
}
