package org.example.spaceflight.simulation;

import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Records per-passenger health status on each tick and prints a statistical
 * evaluation report at the end of the simulation run.
 * Purely observational — no side effects on simulation state.
 */
public class SimulationObserver {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    /** Lightweight snapshot of one tick's classification result. */
    record StatusSnapshot(long tick, FlightPhase phase, HealthStatus status) {}

    private final Map<Passenger, List<StatusSnapshot>> history = new LinkedHashMap<>();

    /** Register all passengers before the simulation starts. */
    public void init(List<Passenger> passengers) {
        for (Passenger p : passengers) {
            history.put(p, new ArrayList<>());
        }
    }

    /** Record the current health status for all passengers on a given tick. */
    public void record(List<Passenger> passengers, FlightPhase phase, long tick) {
        for (Passenger p : passengers) {
            List<StatusSnapshot> snaps = history.get(p);
            if (snaps != null) {
                snaps.add(new StatusSnapshot(tick, phase, p.getHealthStatus()));
            }
        }
    }

    /** Compute and print the full evaluation report to stdout and the logger. */
    public void printReport() {
        printReport(List.of());
    }

    /** Compute and print the full evaluation report, marking emergency passengers by name. */
    public void printReport(List<String> emergencyPassengerNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("=".repeat(65)).append("\n");
        sb.append("  SIMULATION HEALTH CLASSIFICATION REPORT\n");
        sb.append("=".repeat(65)).append("\n\n");

        int nonEmergencyGreenSum = 0;
        int nonEmergencyTotalSum = 0;

        for (Map.Entry<Passenger, List<StatusSnapshot>> entry : history.entrySet()) {
            Passenger p = entry.getKey();
            List<StatusSnapshot> snaps = entry.getValue();

            if (snaps.isEmpty()) continue;

            int total = snaps.size();
            int green = 0, yellow = 0, red = 0;
            int transitionCount = 0;
            int gy = 0, yr = 0, rg = 0, ry = 0, yg = 0, gr = 0;

            HealthStatus prev = null;
            for (StatusSnapshot s : snaps) {
                switch (s.status()) {
                    case GREEN  -> green++;
                    case YELLOW -> yellow++;
                    case RED    -> red++;
                }
                if (prev != null && prev != s.status()) {
                    transitionCount++;
                    String t = prev.name() + "->" + s.status().name();
                    switch (t) {
                        case "GREEN->YELLOW"  -> gy++;
                        case "YELLOW->RED"    -> yr++;
                        case "RED->GREEN"     -> rg++;
                        case "RED->YELLOW"    -> ry++;
                        case "YELLOW->GREEN"  -> yg++;
                        case "GREEN->RED"     -> gr++;
                        default -> { /* ignore other combos */ }
                    }
                }
                prev = s.status();
            }

            // Per-phase breakdown
            Map<FlightPhase, int[]> phaseStats = new EnumMap<>(FlightPhase.class);
            for (FlightPhase phase : FlightPhase.values()) {
                phaseStats.put(phase, new int[3]); // [green, yellow, red]
            }
            for (StatusSnapshot s : snaps) {
                int[] counts = phaseStats.get(s.phase());
                if (counts != null) {
                    switch (s.status()) {
                        case GREEN  -> counts[0]++;
                        case YELLOW -> counts[1]++;
                        case RED    -> counts[2]++;
                    }
                }
            }

            sb.append(String.format("Passenger: %s (%d, %s, %s)%s\n",
                    p.getName(), p.getAge(), p.getGender(), p.getExperienceMode(),
                    emergencyPassengerNames.contains(p.getName()) ? "  *** EMERGENCY ***" : ""));
            sb.append(String.format("  Total ticks : %d\n", total));
            sb.append(String.format("  GREEN  : %4d ticks (%5.1f%%)\n", green,  pct(green,  total)));
            sb.append(String.format("  YELLOW : %4d ticks (%5.1f%%)\n", yellow, pct(yellow, total)));
            sb.append(String.format("  RED    : %4d ticks (%5.1f%%)\n", red,    pct(red,    total)));
            sb.append(String.format("  Transitions: %d  [G→Y:%d  Y→R:%d  R→Y:%d  Y→G:%d  R→G:%d  G→R:%d]\n",
                    transitionCount, gy, yr, ry, yg, rg, gr));

            sb.append("  Phase breakdown:\n");
            for (FlightPhase phase : FlightPhase.values()) {
                int[] c = phaseStats.get(phase);
                int phaseTotal = c[0] + c[1] + c[2];
                if (phaseTotal == 0) continue;
                sb.append(String.format("    %-12s G:%5.1f%%  Y:%5.1f%%  R:%5.1f%%  (%d ticks)\n",
                        phase.name() + ":",
                        pct(c[0], phaseTotal), pct(c[1], phaseTotal), pct(c[2], phaseTotal),
                        phaseTotal));
            }
            sb.append("\n");

            // Accumulate for non-emergency summary (never true here unless manually set,
            // so we rely on the runner to flag emergency passengers)
            nonEmergencyGreenSum += green;
            nonEmergencyTotalSum += total;
        }

        // Overall summary
        sb.append("=".repeat(65)).append("\n");
        sb.append("  OVERALL ASSESSMENT\n");
        sb.append("=".repeat(65)).append("\n");

        int allGreen = 0, allYellow = 0, allRed = 0, allTotal = 0;
        int allTransitions = 0;
        for (List<StatusSnapshot> snaps : history.values()) {
            for (StatusSnapshot s : snaps) {
                allTotal++;
                switch (s.status()) {
                    case GREEN  -> allGreen++;
                    case YELLOW -> allYellow++;
                    case RED    -> allRed++;
                }
            }
        }
        // Count transitions across all passengers combined
        for (List<StatusSnapshot> snaps : history.values()) {
            HealthStatus prev2 = null;
            for (StatusSnapshot s : snaps) {
                if (prev2 != null && prev2 != s.status()) allTransitions++;
                prev2 = s.status();
            }
        }

        sb.append(String.format("  All passengers combined: G=%.1f%%  Y=%.1f%%  R=%.1f%%\n",
                pct(allGreen, allTotal), pct(allYellow, allTotal), pct(allRed, allTotal)));
        sb.append(String.format("  Total status transitions (all passengers): %d\n", allTransitions));
        sb.append(String.format("  Total ticks recorded: %d\n", allTotal));
        sb.append("\n");

        // Tuning evaluation
        sb.append("  TUNING EVALUATION:\n");
        double greenPct = pct(allGreen, allTotal);
        double yellowPct = pct(allYellow, allTotal);
        double redPct = pct(allRed, allTotal);
        double avgTransitionsPerPassenger = (double) allTransitions / Math.max(1, history.size());

        if (greenPct > 95) {
            sb.append("  [WARN] >95% GREEN overall — simulation may be too relaxed, thresholds too wide\n");
        } else if (greenPct < 50) {
            sb.append("  [WARN] <50% GREEN overall — thresholds may be too tight\n");
        } else {
            sb.append("  [OK]   GREEN ratio looks balanced\n");
        }

        if (yellowPct < 3) {
            sb.append("  [WARN] Very little YELLOW (<3%) — stress phases not producing enough warnings\n");
        } else {
            sb.append("  [OK]   YELLOW coverage adequate\n");
        }

        if (redPct == 0) {
            sb.append("  [WARN] No RED detected at all — emergency detection may be broken\n");
        } else {
            sb.append("  [OK]   RED detected\n");
        }

        if (avgTransitionsPerPassenger > 10) {
            sb.append(String.format("  [WARN] High oscillation: avg %.1f transitions/passenger — hysteresis may be too weak\n",
                    avgTransitionsPerPassenger));
        } else {
            sb.append(String.format("  [OK]   Avg transitions/passenger: %.1f\n", avgTransitionsPerPassenger));
        }

        sb.append("=".repeat(65)).append("\n");

        log.info(sb.toString());
    }

    private static double pct(int part, int total) {
        return total == 0 ? 0.0 : (100.0 * part / total);
    }
}
