package org.example.spaceflight.simulation;

import org.example.spaceflight.health.CsvTrainingDataLoader;
import org.example.spaceflight.health.KnnHealthEvaluationService;
import org.example.spaceflight.health.HealthEvaluationResult;
import org.example.spaceflight.health.HealthEvaluationService;
import org.example.spaceflight.health.VitalProfileTable;
import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.PassengerRegistry;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Runs a full flight simulation without JavaFX.
 * Wires up the same services as SpaceFlightApp but drives the tick loop
 * synchronously so the run completes instantly and produces a statistics report.
 *
 * Run via:
 *   mvn compile exec:java -Dexec.mainClass="org.example.spaceflight.simulation.HeadlessSimulationRunner"
 */
public class HeadlessSimulationRunner {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    // Tick interval used for the generator's internal scaling.
    // Keep at 500 ms to match production defaults.
    private static final int TICK_INTERVAL_MS = 500;

    // Total ticks driven by DefaultFlightSimulationService:
    // ASCENT(180) + ORBIT(840) + DESCENT(180) = 1200 ticks
    private static final int TOTAL_TICKS = 1200;

    // Number of passengers to mark as medical emergency (set to 1 to test RED detection).
    private static final int EMERGENCY_COUNT = 1;

    public static void main(String[] args) {
        new HeadlessSimulationRunner().run();
    }

    public void run() {
        log.info("HeadlessSimulationRunner starting");

        // -- Wire up services (mirrors SpaceFlightApp wiring) --
        PassengerRegistry registry = new PassengerRegistry();
        List<Passenger> allPersons = registry.getAllPersons();

        DefaultFlightSimulationService flightSim = new DefaultFlightSimulationService();

        DefaultVitalSignsGenerator generator = new DefaultVitalSignsGenerator();
        generator.configure(TICK_INTERVAL_MS);

        // Mark one passenger as emergency
        List<String> emergencyNames = markEmergencyPassengers(generator, allPersons, EMERGENCY_COUNT, TOTAL_TICKS);

        HealthEvaluationService evaluationService =
                new KnnHealthEvaluationService(new VitalProfileTable(), new CsvTrainingDataLoader());

        SimulationObserver observer = new SimulationObserver();
        observer.init(allPersons);

        log.info(String.format("Running %d ticks, %d emergency passenger(s): %s",
                TOTAL_TICKS, EMERGENCY_COUNT, emergencyNames));

        // -- Synchronous tick loop --
        for (long tick = 1; tick <= TOTAL_TICKS; tick++) {
            flightSim.update(tick);
            FlightPhase phase = flightSim.getCurrentState().getFlightPhase();

            for (Passenger p : allPersons) {
                p.setVitalSigns(generator.generateNext(p, phase));
            }

            // Evaluate health for each passenger (no manual override in headless mode)
            for (Passenger p : allPersons) {
                if (p.getVitalSigns() != null) {
                    HealthEvaluationResult result = evaluationService.evaluate(
                            p.getVitalSigns(), p, phase);
                    p.setHealthStatus(result.getOverallStatus());
                }
            }

            observer.record(allPersons, phase, tick);
        }

        log.info("Simulation run complete — printing report");
        observer.printReport(emergencyNames);
    }

    private List<String> markEmergencyPassengers(VitalSignsGenerator generator,
                                         List<Passenger> persons,
                                         int count,
                                         long totalTicks) {
        List<String> names = new ArrayList<>();
        if (count <= 0) return names;
        List<Passenger> shuffled = new ArrayList<>(persons);
        java.util.Collections.shuffle(shuffled);
        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            Passenger p = shuffled.get(i);
            generator.markAsEmergency(p.getName(), totalTicks);
            names.add(p.getName());
            log.info("Emergency scheduled for passenger: " + p.getName());
        }
        return names;
    }
}
