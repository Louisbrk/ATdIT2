package org.example.spaceflight.health;

import org.example.spaceflight.model.HealthStatus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Immutable result of one health evaluation: overall status + per-vital statuses. */
public final class HealthEvaluationResult {

    private final HealthStatus overallStatus;
    private final Map<VitalType, HealthStatus> vitalStatuses;

    private HealthEvaluationResult(HealthStatus overall, Map<VitalType, HealthStatus> vitals) {
        this.overallStatus  = overall;
        this.vitalStatuses  = Collections.unmodifiableMap(vitals);
    }

    public static HealthEvaluationResult of(HealthStatus overall, Map<VitalType, HealthStatus> vitals) {
        return new HealthEvaluationResult(overall, new EnumMap<>(vitals));
    }

    /** Safe default before the first tick — everything green. */
    public static HealthEvaluationResult allGreen() {
        Map<VitalType, HealthStatus> all = new EnumMap<>(VitalType.class);
        for (VitalType t : VitalType.values()) {
            all.put(t, HealthStatus.GREEN);
        }
        return new HealthEvaluationResult(HealthStatus.GREEN, all);
    }

    public HealthStatus getOverallStatus() {
        return overallStatus;
    }

    /** Returns GREEN if the vital is not present (safe default). */
    public HealthStatus getVitalStatus(VitalType type) {
        return vitalStatuses.getOrDefault(type, HealthStatus.GREEN);
    }

    public Map<VitalType, HealthStatus> getVitalStatuses() {
        return vitalStatuses;
    }
}
