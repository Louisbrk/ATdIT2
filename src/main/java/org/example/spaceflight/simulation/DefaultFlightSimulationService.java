package org.example.spaceflight.simulation;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.ShuttleState;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/** Tracks shuttle telemetry (altitude, speed, fuel, flight phase) tick by tick through the flight profile. */
public class DefaultFlightSimulationService implements FlightSimulationService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    // 1200 ticks × 500ms = 600s = 10 min real time
    // Each tick = 3 seconds sim-time → 3600s = 60 min simulated
    private static final int ASCENT_TICKS = 180;      // 15% — ~90s real ascent
    private static final int ORBIT_TICKS = 840;        // 70% — ~420s real orbit
    private static final int DESCENT_TICKS = 180;      // 15% — ~90s real descent
    private static final int TOTAL_TICKS = ASCENT_TICKS + ORBIT_TICKS + DESCENT_TICKS;

    private static final double SIM_SECONDS_PER_TICK = 0.5;  // 1200 ticks × 0.5s = 600s = 10 min
    private static final double TOTAL_SIM_SECONDS = TOTAL_TICKS * SIM_SECONDS_PER_TICK;
    private static final double ORBIT_ALTITUDE_KM = 150.0;
    private static final int    EMERGENCY_MIN_TICKS = 40; // shortest possible emergency descent
    private static final int    EMERGENCY_TICKS_AT_ORBIT = 120; // 60s real time at full orbit altitude
    private static final double ORBIT_VELOCITY_KMPH = 28000.0; // low-Earth-orbit speed
    private static final double MAX_DISTANCE_KM = 800.0;       // total route distance

    private final ShuttleState state = new ShuttleState();

    private boolean emergencyActive = false;
    private double emergencyStartAltitude;
    private double emergencyStartProgress;
    private int emergencyTickCount;
    private int emergencyTotalTicks;
    private Runnable onEmergencyLanded;

    public DefaultFlightSimulationService() {
        state.setTotalFlightSeconds(TOTAL_SIM_SECONDS);
    }

    @Override
    public void update(long tickCount) {
        FlightPhase previousPhase = state.getFlightPhase();

        if (emergencyActive) {
            updateEmergencyDescent();
        } else if (tickCount <= ASCENT_TICKS) {
            updateAscent(tickCount);
        } else if (tickCount <= ASCENT_TICKS + ORBIT_TICKS) {
            updateOrbit(tickCount);
        } else if (tickCount <= TOTAL_TICKS) {
            updateDescent(tickCount);
        } else {
            updateLanded();
        }

        state.setElapsedSeconds(tickCount * SIM_SECONDS_PER_TICK);

        if (state.getFlightPhase() != previousPhase) {
            log.info("Flight phase changed to " + state.getFlightPhase());
        }
    }

    @Override
    public void emergencyLanding() {
        if (emergencyActive || state.getFlightPhase() == FlightPhase.LANDED) return;

        emergencyActive = true;
        emergencyStartAltitude = state.getAltitudeKm();
        emergencyStartProgress = state.getRouteProgress();
        emergencyTickCount = 0;
        // Emergency descent duration proportional to current altitude
        emergencyTotalTicks = Math.max(EMERGENCY_MIN_TICKS,
                (int) (emergencyStartAltitude / ORBIT_ALTITUDE_KM * EMERGENCY_TICKS_AT_ORBIT));

        state.setEmergencyLanding(true);
        log.warning("Emergency landing initiated at altitude " + String.format("%.0f", emergencyStartAltitude) + " km");
    }

    @Override
    public boolean isEmergencyLanding() {
        return emergencyActive;
    }

    @Override
    public ShuttleState getCurrentState() {
        return state;
    }

    private void updateAscent(long tick) {
        state.setFlightPhase(FlightPhase.ASCENT);
        double progress = (double) tick / ASCENT_TICKS;

        state.setAltitudeKm(progress * ORBIT_ALTITUDE_KM);
        state.setVelocityKmph(progress * ORBIT_VELOCITY_KMPH);
        state.setFuelPercent(100 - progress * 30);
        state.setDistanceKm(progress * 150);
        state.setOxygenPercent(100 - progress * 2);
        state.setCabinTemperature(21 + progress * 3);
        state.setRouteProgress(progress * 0.2);  // 0.0 - 0.2
    }

    private void updateOrbit(long tick) {
        state.setFlightPhase(FlightPhase.ORBIT);
        double progress = (double) (tick - ASCENT_TICKS) / ORBIT_TICKS;

        state.setAltitudeKm(ORBIT_ALTITUDE_KM + Math.sin(progress * Math.PI * 2) * 5);
        state.setVelocityKmph(ORBIT_VELOCITY_KMPH + Math.sin(progress * Math.PI * 4) * 200);
        state.setFuelPercent(70 - progress * 10);
        state.setDistanceKm(150 + progress * 500);
        state.setOxygenPercent(98 - progress * 1);
        state.setCabinTemperature(24 - progress * 2);
        state.setRouteProgress(0.2 + progress * 0.6);  // 0.2 - 0.8
    }

    private void updateDescent(long tick) {
        state.setFlightPhase(FlightPhase.DESCENT);
        double progress = (double) (tick - ASCENT_TICKS - ORBIT_TICKS) / DESCENT_TICKS;

        state.setAltitudeKm(ORBIT_ALTITUDE_KM * (1 - progress));
        state.setVelocityKmph(ORBIT_VELOCITY_KMPH * (1 - progress * 0.95));
        state.setFuelPercent(60 - progress * 20);
        state.setDistanceKm(650 + progress * 150);
        state.setOxygenPercent(97 + progress * 2);
        state.setCabinTemperature(22 + progress * 3);
        state.setRouteProgress(0.8 + progress * 0.2);  // 0.8 - 1.0
    }

    private void updateLanded() {
        state.setFlightPhase(FlightPhase.LANDED);
        state.setAltitudeKm(0);
        state.setVelocityKmph(0);
        state.setRouteProgress(1.0);
    }

    private void updateEmergencyDescent() {
        emergencyTickCount++;
        double progress = Math.min(1.0, (double) emergencyTickCount / emergencyTotalTicks);

        state.setFlightPhase(FlightPhase.DESCENT);
        state.setAltitudeKm(emergencyStartAltitude * (1 - progress));
        state.setVelocityKmph(ORBIT_VELOCITY_KMPH * 1.07 * (1 - progress)); // slightly faster during emergency re-entry
        state.setFuelPercent(Math.max(0, state.getFuelPercent() - 1.5));
        state.setOxygenPercent(Math.min(100, state.getOxygenPercent() + progress * 3));
        state.setCabinTemperature(24 + (1 - progress) * 5);

        // Route progress stays where it was (shuttle position tracked by emergency progress on map)
        state.setRouteProgress(emergencyStartProgress);

        if (progress >= 1.0) {
            state.setFlightPhase(FlightPhase.LANDED);
            state.setAltitudeKm(0);
            state.setVelocityKmph(0);
            state.setEmergencyLanding(false);
            emergencyActive = false;
            log.info("Emergency landing completed");
            if (onEmergencyLanded != null) {
                onEmergencyLanded.run();
            }
        }
    }

    /** Registers a callback that fires once the emergency descent reaches the ground. */
    public void setOnEmergencyLanded(Runnable callback) {
        this.onEmergencyLanded = callback;
    }

    /** Returns the emergency descent progress as a value between 0.0 and 1.0. */
    public double getEmergencyProgress() {
        if (emergencyTotalTicks == 0) return 0;
        return Math.min(1.0, (double) emergencyTickCount / emergencyTotalTicks);
    }
}
