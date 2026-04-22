package org.example.spaceflight.health;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;

import java.lang.invoke.MethodHandles;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/** Legacy threshold-based classifier. Kept for comparison; use KnnHealthEvaluationService instead. */
@Deprecated
public class DefaultHealthEvaluationService implements HealthEvaluationService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    @Override
    public HealthEvaluationResult evaluate(VitalSigns v, Passenger passenger, FlightPhase phase) {
        if (v == null) return HealthEvaluationResult.allGreen();

        ExperienceMode mode = passenger.getExperienceMode();
        HealthStatus overall;
        if (isCritical(v, mode)) {
            log.fine("Health evaluated RED: " + v);
            overall = HealthStatus.RED;
        } else if (isWarning(v, mode)) {
            log.fine("Health evaluated YELLOW: " + v);
            overall = HealthStatus.YELLOW;
        } else {
            overall = HealthStatus.GREEN;
        }

        Map<VitalType, HealthStatus> vitals = new EnumMap<>(VitalType.class);
        for (VitalType t : VitalType.values()) vitals.put(t, overall);
        return HealthEvaluationResult.of(overall, vitals);
    }

    private boolean isCritical(VitalSigns v, ExperienceMode mode) {
        if (v.getSpO2() < 90.0) return true;
        if (v.getSystolicBp() > 185 || v.getDiastolicBp() > 115) return true;
        if (v.getSystolicBp() < 75) return true;
        if (v.getRespiratoryRate() > 30 || v.getRespiratoryRate() < 6) return true;
        int bpmRedHigh = (mode == ExperienceMode.ACTION) ? 160 : 140;
        return v.getBpm() > bpmRedHigh || v.getBpm() < 38;
    }

    private boolean isWarning(VitalSigns v, ExperienceMode mode) {
        if (v.getSpO2() < 94.0) return true;
        if (v.getSystolicBp() > 160 || v.getDiastolicBp() > 100) return true;
        if (v.getSystolicBp() < 85) return true;
        if (v.getRespiratoryRate() > 24 || v.getRespiratoryRate() < 10) return true;
        int bpmWarnHigh = switch (mode) {
            case RELAXED -> 95;
            case NORMAL  -> 110;
            case ACTION  -> 140;
        };
        return v.getBpm() > bpmWarnHigh || v.getBpm() < 48;
    }
}
