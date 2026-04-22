package org.example.spaceflight.app;

import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.alert.PsychologicalSupportService;
import org.example.spaceflight.health.CsvTrainingDataLoader;
import org.example.spaceflight.health.HealthEvaluationOrchestrator;
import org.example.spaceflight.health.KnnHealthEvaluationService;
import org.example.spaceflight.health.VitalProfileTable;
import org.example.spaceflight.model.IPassengerRegistry;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.SimulationConfig;
import org.example.spaceflight.model.SimulationSnapshot;
import org.example.spaceflight.model.Stewardess;
import org.example.spaceflight.simulation.FlightSimulationService;
import org.example.spaceflight.simulation.SimulationService;
import org.example.spaceflight.simulation.VitalSignsGenerator;
import org.example.spaceflight.ui.aihealth.AiHealthDashboardView;
import org.example.spaceflight.ui.basestation.BaseStationView;
import org.example.spaceflight.ui.basestation.EmergencyAlertView;
import org.example.spaceflight.ui.basestation.PsychologicalSupportView;
import org.example.spaceflight.ui.passenger.PassengerDashboardView;
import org.example.spaceflight.ui.passenger.StewardessInboxView;
import org.example.spaceflight.ui.shared.MainWindow;
import org.example.spaceflight.ui.shared.NavigationBar;
import org.example.spaceflight.ui.simulation.SimulationConfigView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** JavaFX application entry point — wires up all services and opens the simulation config window. */
public class SpaceFlightApp extends Application {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    // All services live in AppContext — the only class that knows concrete implementations.
    private final AppContext ctx = new AppContext();

    private SimulationConfig activeConfig;
    private StewardessInboxView stewardessView;
    private final List<PassengerDashboardView> passengerDashboards = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        IPassengerRegistry registry = ctx.getPassengerRegistry();
        SimulationService  simSvc   = ctx.getSimulationService();

        SimulationConfigView configView = new SimulationConfigView(simSvc);
        configView.setOnSimulationStarted(config -> openDashboard(config, primaryStage));
        configView.setPassengers(registry.getAllPersons());
        configView.setOnOpenPassengerView(this::openPassengerDashboard);

        Scene scene = new Scene(configView.getRoot(), 500, 450);
        primaryStage.setTitle("SpaceFlight - Simulation Control");
        primaryStage.setScene(scene);
        primaryStage.show();

        log.info("Application started");
    }

    private void openDashboard(SimulationConfig config, @SuppressWarnings("unused") Stage ownerStage) {
        this.activeConfig = config;

        SimulationService       simSvc   = ctx.getSimulationService();
        FlightSimulationService flightSvc = ctx.getFlightSimulationService();
        VitalSignsGenerator     vitalGen  = ctx.getVitalSignsGenerator();
        AlertService            alertSvc  = ctx.getAlertService();
        PsychologicalSupportService psychSvc = ctx.getPsychService();
        IPassengerRegistry      registry  = ctx.getPassengerRegistry();

        vitalGen.configure(config.getTickIntervalMs());
        long flightDurationMs = java.time.Duration.between(
                config.getDepartureTime(), config.getArrivalTime()).toMillis();
        long totalTicks = flightDurationMs / config.getTickIntervalMs();
        markEmergencyPassengers(vitalGen, config.getEmergencyPassengerCount(), totalTicks, registry);

        for (PassengerDashboardView pdv : passengerDashboards) {
            pdv.updateTimes(config.getDepartureTime(), config.getArrivalTime());
        }
        if (stewardessView != null) {
            stewardessView.updateTimes(config.getDepartureTime(), config.getArrivalTime());
        }

        List<Passenger> allPersons = registry.getAllPersons();
        MainWindow mainWindow = new MainWindow();

        BaseStationView baseStationView = new BaseStationView(allPersons, mainWindow, alertSvc);
        baseStationView.subscribePsychService(psychSvc);
        baseStationView.setOnEmergencyLanding(() -> {
            flightSvc.emergencyLanding();
            log.warning("Emergency landing initiated - shuttle returning to Earth");
            String msg = "EMERGENCY LANDING initiated!";
            Platform.runLater(() -> {
                for (PassengerDashboardView pdv : passengerDashboards) {
                    pdv.addStatusMessage(msg);
                }
                if (stewardessView != null) {
                    stewardessView.receiveEmergencyNotification(msg);
                }
            });
        });

        flightSvc.setOnEmergencyLanded(() -> Platform.runLater(simSvc::stop));

        mainWindow.registerView(NavigationBar.Tab.BASE_STATION, baseStationView.getRoot());
        mainWindow.showView(NavigationBar.Tab.BASE_STATION);

        AiHealthDashboardView aiHealthView = new AiHealthDashboardView(allPersons);
        aiHealthView.setOrchestrator(new HealthEvaluationOrchestrator(
                new KnnHealthEvaluationService(new VitalProfileTable(), new CsvTrainingDataLoader())));
        mainWindow.registerView(NavigationBar.Tab.AI_HEALTH, aiHealthView.getRoot());

        EmergencyAlertView alertView = new EmergencyAlertView(alertSvc);
        alertView.setOnSendToStewardess((incident, message) -> {
            incident.setStewardessNote(message);
            if (stewardessView != null) {
                stewardessView.receiveIncident(incident, message);
            }
        });
        mainWindow.registerView(NavigationBar.Tab.EMERGENCY_ALERT, alertView.getRoot());

        PsychologicalSupportView psychView = new PsychologicalSupportView(psychSvc);
        psychView.setOnSendToStewardess((incident, message) -> {
            incident.setStewardessNote(message);
            if (stewardessView != null) {
                stewardessView.receivePsychIncident(incident, message);
            }
        });
        mainWindow.registerView(NavigationBar.Tab.PSYCHOLOGICAL_SUPPORT, psychView.getRoot());

        simSvc.addTickListener(tickCount -> {
            flightSvc.update(tickCount);

            for (Passenger p : allPersons) {
                p.setVitalSigns(vitalGen.generateNext(
                        p, flightSvc.getCurrentState().getFlightPhase()));
            }

            // Build a serialisable snapshot — the single data boundary for client-facing views.
            SimulationSnapshot snapshot = new SimulationSnapshot(
                    flightSvc.getCurrentState(), allPersons,
                    flightSvc.getEmergencyProgress(), tickCount);

            Platform.runLater(() -> {
                // Base-station views run server-side and use the live objects directly.
                baseStationView.getFlightInfoPanel().setEmergencyProgress(snapshot.getEmergencyProgress());
                baseStationView.updateFlightInfo(flightSvc.getCurrentState());
                baseStationView.updatePassengerCards(allPersons);
                baseStationView.updateDetailView();
                aiHealthView.update(allPersons, flightSvc.getCurrentState().getFlightPhase());

                // Client-facing views receive only the snapshot — no direct object references.
                if (stewardessView != null) stewardessView.update(snapshot);
                passengerDashboards.forEach(pdv -> pdv.update(snapshot));
            });
        });

        Stage dashboardStage = new Stage();
        Scene dashboardScene = new Scene(mainWindow.getRoot(), 1200, 700);
        dashboardStage.setTitle("SpaceFlight - Base Station");
        dashboardStage.setScene(dashboardScene);
        dashboardStage.show();

        log.info("Dashboard opened");
    }

    private void openPassengerDashboard(Passenger passenger) {
        SimulationConfig config   = activeConfig != null ? activeConfig : new SimulationConfig();
        AlertService     alertSvc = ctx.getAlertService();
        PsychologicalSupportService psychSvc = ctx.getPsychService();

        if (passenger.isCrewMember()) {
            StewardessInboxView view = new StewardessInboxView(
                    (Stewardess) passenger, config.getDepartureTime(), config.getArrivalTime(),
                    alertSvc, psychSvc);
            stewardessView = view;

            Stage stage = new Stage();
            stage.setScene(new Scene(view.getRoot(), 750, 550));
            stage.setTitle("SpaceFlight - Stewardess: " + passenger.getName());
            stage.setOnHidden(e -> stewardessView = null);
            stage.show();
            log.info("Stewardess dashboard opened for " + passenger.getName());
        } else {
            PassengerDashboardView pdv = new PassengerDashboardView(
                    passenger, config.getDepartureTime(), config.getArrivalTime(),
                    alertSvc, psychSvc, ctx.getExperienceModeService());
            passengerDashboards.add(pdv);

            Stage stage = new Stage();
            stage.setScene(new Scene(pdv.getRoot(), 750, 550));
            stage.setTitle("SpaceFlight - Passenger: " + passenger.getName());
            stage.setOnHidden(e -> passengerDashboards.remove(pdv));
            stage.show();
            log.info("Passenger dashboard opened for " + passenger.getName());
        }
    }

    private void markEmergencyPassengers(VitalSignsGenerator generator, int count,
                                         long totalFlightTicks, IPassengerRegistry registry) {
        if (count <= 0) return;
        List<Passenger> shuffled = new ArrayList<>(registry.getAllPersons());
        java.util.Collections.shuffle(shuffled);
        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            generator.markAsEmergency(shuffled.get(i).getName(), totalFlightTicks);
        }
    }
}
