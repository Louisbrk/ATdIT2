package org.example.spaceflight.simulation;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Generates realistic-looking vital signs per tick.
 *
 * Design principles:
 * - Each passenger gets a personal baseline derived once at startup.
 * - Each vital carries a slow-moving trend that shifts the target over time,
 *   so curves look like real physiology rather than random noise.
 * - Per-tick noise is very small; bigger changes come from gradual trend shifts.
 * - Flight phase and experience mode influence the target range via IVitalTargetProvider.
 * - Tick interval is taken into account so the behaviour is the same regardless
 *   of how fast the simulation runs.
 */
public class DefaultVitalSignsGenerator implements VitalSignsGenerator {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    // Reference interval the tuning values were designed for (ms).
    private static final double REFERENCE_TICK_MS = 1000.0;

    private final Random random = new Random();
    private final IVitalTargetProvider targetProvider;

    // Per-passenger state
    private final Map<String, VitalSigns>      previous = new HashMap<>();
    private final Map<String, PersonalProfile> profiles = new HashMap<>();
    private final Map<String, TrendState>      trends   = new HashMap<>();

    // Tick interval in ms – set once via configure(); defaults to 1000 ms.
    private double tickIntervalMs = REFERENCE_TICK_MS;

    // Maps passenger name → tick number at which the emergency activates.
    private final Map<String, Long> emergencyActivationTick = new HashMap<>();
    // Counts how many ticks each passenger has been generated.
    private final Map<String, Long> tickCounter = new HashMap<>();

    public DefaultVitalSignsGenerator() {
        this(new DefaultVitalTargetProvider());
    }

    public DefaultVitalSignsGenerator(IVitalTargetProvider targetProvider) {
        this.targetProvider = targetProvider;
    }

    /** Must be called before the first tick so step sizes scale correctly. */
    @Override
    public void configure(int tickIntervalMs) {
        this.tickIntervalMs = tickIntervalMs;
    }

    /**
     * Schedule a passenger's vitals to drift critical after a random delay.
     * The emergency kicks in somewhere between 20 % and 70 % of total flight ticks.
     */
    @Override
    public void markAsEmergency(String passengerName, long totalFlightTicks) {
        long minTick = (long) (totalFlightTicks * 0.20);
        long maxTick = (long) (totalFlightTicks * 0.70);
        long activationTick = minTick + (long) (random.nextDouble() * (maxTick - minTick));
        emergencyActivationTick.put(passengerName, activationTick);
        log.info("Emergency scheduled for " + passengerName + " at tick " + activationTick
                + " (flight has " + totalFlightTicks + " ticks)");
    }

    @Override
    public VitalSigns generateNext(Passenger passenger, FlightPhase phase) {
        String key = passenger.getName();

        PersonalProfile profile = profiles.computeIfAbsent(key, k -> new PersonalProfile(passenger));
        TrendState trend        = trends.computeIfAbsent(key, k -> new TrendState());
        VitalSigns prev         = previous.computeIfAbsent(key, k -> createBaseline(profile));

        long tick = tickCounter.merge(key, 1L, Long::sum);
        boolean emergency = isEmergencyActive(key, tick);

        // How far this tick is relative to the reference interval.
        // A 500 ms tick is 0.5 x, a 2000 ms tick is 2 x.
        double tickScale = tickIntervalMs / REFERENCE_TICK_MS;

        // Target ranges depend on phase and mode.
        PhaseTarget target = targetProvider.buildTarget(profile, phase, passenger.getExperienceMode(), emergency);

        // Advance slow trends (target offset that drifts within ±half-range).
        // Trend offsets are suppressed during emergencies so critical targets are reached quickly.
        if (!emergency) trend.advance(random, tickScale);

        // During emergencies use a much stronger pull so the vitals reach critical levels
        // within ~10 ticks instead of drifting slowly over hundreds of ticks.
        double pullFactor = emergency ? 0.40 : 0.08;

        int bpm       = nextInt(prev.getBpm(),             target.bpmCenter  + (emergency ? 0 : trend.bpmOffset),
                                target.bpmRange,  1.2, tickScale, pullFactor);
        double spO2   = nextDouble(prev.getSpO2(),         target.spo2Center + (emergency ? 0 : trend.spo2Offset),
                                target.spo2Range, 0.10, tickScale, pullFactor);
        int systolic  = nextInt(prev.getSystolicBp(),      target.sysCenter  + (emergency ? 0 : trend.sysOffset),
                                target.sysRange,  1.5, tickScale, pullFactor);
        int diastolic = nextInt(prev.getDiastolicBp(),     target.diasCenter + (emergency ? 0 : trend.diasOffset),
                                target.diasRange, 1.2, tickScale, pullFactor);
        int respRate  = nextInt(prev.getRespiratoryRate(), target.respCenter + (emergency ? 0 : trend.respOffset),
                                target.respRange, 0.8, tickScale, pullFactor);

        // Hard physiological limits
        bpm       = clamp(bpm,       40,  200);
        spO2      = clamp(spO2,      80.0, 100.0);
        systolic  = clamp(systolic,  80,  200);
        diastolic = clamp(diastolic, 50,  120);
        respRate  = clamp(respRate,   8,   40);

        // Diastolic must stay below systolic
        if (diastolic >= systolic) diastolic = systolic - 5;

        VitalSigns next = new VitalSigns(bpm, spO2, systolic, diastolic, respRate);
        previous.put(key, next);
        return next;
    }

    // -------------------------------------------------------------------------
    // Step calculation
    // -------------------------------------------------------------------------

    /**
     * Moves an int value one step toward its target.
     * maxNoisePerSecond controls the random jitter per second of real time.
     * The mean-reversion pull ensures the value gravitates back to target.
     */
    private int nextInt(int current, double target, double halfRange,
                        double maxNoisePerSecond, double tickScale, double pullFactor) {
        double pull      = (target - current) * pullFactor * tickScale;
        double noiseStep = maxNoisePerSecond * tickScale;
        double noise     = random.nextGaussian() * noiseStep;
        double next      = current + pull + noise;
        // Soft clamping to target ± halfRange
        double lo = target - halfRange;
        double hi = target + halfRange;
        if (next < lo) next = lo + random.nextDouble() * 0.5;
        if (next > hi) next = hi - random.nextDouble() * 0.5;
        return (int) Math.round(next);
    }

    private double nextDouble(double current, double target, double halfRange,
                              double maxNoisePerSecond, double tickScale, double pullFactor) {
        double pull      = (target - current) * pullFactor * tickScale;
        double noiseStep = maxNoisePerSecond * tickScale;
        double noise     = random.nextGaussian() * noiseStep;
        double next      = current + pull + noise;
        double lo = target - halfRange;
        double hi = target + halfRange;
        if (next < lo) next = lo + random.nextDouble() * 0.05;
        if (next > hi) next = hi - random.nextDouble() * 0.05;
        return Math.round(next * 10.0) / 10.0;
    }

    // -------------------------------------------------------------------------
    // Inner helpers
    // -------------------------------------------------------------------------

    private static VitalSigns createBaseline(PersonalProfile p) {
        return new VitalSigns(p.baseBpm, p.baseSpO2, p.baseSystolic, p.baseDiastolic, p.baseResp);
    }

    private static int    clamp(int    v, int    lo, int    hi) { return Math.max(lo, Math.min(hi, v)); }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    private boolean isEmergencyActive(String name, long currentTick) {
        Long activation = emergencyActivationTick.get(name);
        return activation != null && currentTick >= activation;
    }

    // -------------------------------------------------------------------------
    // Slow trend state – offsets that drift independently for each vital
    // -------------------------------------------------------------------------

    /**
     * Each vital has a slow trend offset that wanders within a bounded range.
     * This makes the curves look like real telemetry rather than pure noise.
     */
    private static class TrendState {
        double bpmOffset;
        double spo2Offset;
        double sysOffset;
        double diasOffset;
        double respOffset;

        // Internal velocities (units per second)
        private double bpmV, spo2V, sysV, diasV, respV;

        // How quickly velocity itself changes – controls smoothness
        private static final double ACCELERATION = 0.04;
        // Max drift from target centre allowed by the trend
        private static final double BPM_DRIFT  = 5;
        private static final double SPO2_DRIFT = 0.4;
        private static final double SYS_DRIFT  = 7;
        private static final double DIAS_DRIFT = 5;
        private static final double RESP_DRIFT = 1.5;

        void advance(Random rng, double tickScale) {
            bpmV  = advanceV(bpmV,  rng, tickScale);
            spo2V = advanceV(spo2V, rng, tickScale * 0.3);
            sysV  = advanceV(sysV,  rng, tickScale);
            diasV = advanceV(diasV, rng, tickScale);
            respV = advanceV(respV, rng, tickScale * 0.5);

            bpmOffset  = advanceChannel(bpmOffset,  bpmV,  BPM_DRIFT,  tickScale);
            spo2Offset = advanceChannel(spo2Offset, spo2V, SPO2_DRIFT, tickScale);
            sysOffset  = advanceChannel(sysOffset,  sysV,  SYS_DRIFT,  tickScale);
            diasOffset = advanceChannel(diasOffset, diasV, DIAS_DRIFT, tickScale);
            respOffset = advanceChannel(respOffset, respV, RESP_DRIFT, tickScale);
        }

        private double advanceV(double v, Random rng, double tickScale) {
            // Small random kick to velocity, dampened toward zero
            double kick = rng.nextGaussian() * ACCELERATION * tickScale;
            return (v + kick) * 0.97; // damping keeps things stable
        }

        private double advanceChannel(double offset, double velocity, double maxDrift, double tickScale) {
            double next = offset + velocity * tickScale;
            // Reflect at boundaries so the trend stays bounded
            if (next >  maxDrift) { next =  maxDrift; }
            if (next < -maxDrift) { next = -maxDrift; }
            return next;
        }
    }
}
