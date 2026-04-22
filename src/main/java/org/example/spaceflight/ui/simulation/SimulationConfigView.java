package org.example.spaceflight.ui.simulation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.Duration;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.SimulationConfig;
import org.example.spaceflight.simulation.SimulationService;

import java.lang.invoke.MethodHandles;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Start screen where the operator configures simulation parameters before launch. */
public class SimulationConfigView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;
    private final SimulationService simulationService;
    private Consumer<SimulationConfig> onSimulationStarted;
    private Consumer<Passenger> onOpenPassengerView;

    private final Spinner<Integer> emergencyCountSpinner;
    private final Label clockLabel;
    private final Button startButton;
    private final Button pauseButton;
    private final Button resumeButton;
    private final Button stopButton;
    private final Label statusLabel;
    private final Button[] speedButtons;

    public SimulationConfigView(SimulationService simulationService) {
        this.simulationService = simulationService;

        // Config fields
        emergencyCountSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 6, 0));
        emergencyCountSpinner.setPrefWidth(80);

        DateTimeFormatter clockFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        clockLabel = new Label(LocalTime.now().format(clockFormat));
        clockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Timeline clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> clockLabel.setText(LocalTime.now().format(clockFormat))));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();

        // Config grid
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);
        configGrid.add(new Label("Emergency Passengers:"), 0, 0);
        configGrid.add(emergencyCountSpinner, 1, 0);
        configGrid.add(new Label("Current Time:"), 0, 1);
        configGrid.add(clockLabel, 1, 1);

        // Control buttons
        startButton = new Button("Start");
        pauseButton = new Button("Pause");
        resumeButton = new Button("Resume");
        stopButton = new Button("Stop");

        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        stopButton.setDisable(true);

        startButton.setOnAction(e -> onStart());
        pauseButton.setOnAction(e -> onPause());
        resumeButton.setOnAction(e -> onResume());
        stopButton.setOnAction(e -> onStop());

        HBox buttonBar = new HBox(10, startButton, pauseButton, resumeButton, stopButton);
        buttonBar.setAlignment(Pos.CENTER);

        // Speed buttons
        double[] speeds = {1, 2, 3, 5};
        String[] labels = {"1×", "2×", "3×", "5×"};
        speedButtons = new Button[speeds.length];
        HBox speedBar = new HBox(8);
        speedBar.setAlignment(Pos.CENTER);
        speedBar.getChildren().add(new Label("Speed:"));
        for (int i = 0; i < speeds.length; i++) {
            double speed = speeds[i];
            Button btn = new Button(labels[i]);
            btn.setDisable(true);
            btn.setOnAction(e -> {
                simulationService.setSpeed(speed);
                highlightSpeedButton(btn);
            });
            speedButtons[i] = btn;
            speedBar.getChildren().add(btn);
        }

        // Status
        statusLabel = new Label("Status: Idle");

        // Layout
        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Simulation Configuration");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        root.getChildren().addAll(title, configGrid, new Separator(), buttonBar, speedBar, statusLabel);
    }

    public void setPassengers(List<Passenger> passengers) {
        ComboBox<Passenger> passengerCombo = new ComboBox<>();
        passengerCombo.getItems().addAll(passengers);
        if (!passengers.isEmpty()) {
            passengerCombo.setValue(passengers.getFirst());
        }
        passengerCombo.setPrefWidth(200);

        Button openPassengerViewButton = new Button("Open Passenger View");
        openPassengerViewButton.setOnAction(e -> {
            Passenger selected = passengerCombo.getValue();
            if (selected != null && onOpenPassengerView != null) {
                onOpenPassengerView.accept(selected);
            }
        });

        HBox passengerRow = new HBox(10, passengerCombo, openPassengerViewButton);
        passengerRow.setAlignment(Pos.CENTER);

        root.getChildren().addAll(new Separator(), new Label("Passenger View"), passengerRow);
    }

    public Region getRoot() {
        return root;
    }

    public void setOnSimulationStarted(Consumer<SimulationConfig> handler) {
        this.onSimulationStarted = handler;
    }

    public void setOnOpenPassengerView(Consumer<Passenger> handler) {
        this.onOpenPassengerView = handler;
    }

    private SimulationConfig buildConfig() {
        SimulationConfig config = new SimulationConfig();
        config.setEmergencyPassengerCount(emergencyCountSpinner.getValue());

        LocalTime departure = LocalTime.now();
        config.setDepartureTime(departure);
        config.setArrivalTime(departure.plusMinutes(10));

        return config;
    }

    private void onStart() {
        SimulationConfig config = buildConfig();
        simulationService.start(config);
        updateButtonState(true, false);
        statusLabel.setText("Status: Running");
        log.info("User started simulation with " + config.getEmergencyPassengerCount() + " emergency passengers");
        if (onSimulationStarted != null) {
            onSimulationStarted.accept(config);
        }
    }

    private void onPause() {
        simulationService.pause();
        updateButtonState(true, true);
        statusLabel.setText("Status: Paused");
    }

    private void onResume() {
        simulationService.resume();
        updateButtonState(true, false);
        statusLabel.setText("Status: Running");
    }

    private void onStop() {
        simulationService.stop();
        updateButtonState(false, false);
        statusLabel.setText("Status: Idle");
    }

    private void updateButtonState(boolean running, boolean paused) {
        startButton.setDisable(running);
        pauseButton.setDisable(!running || paused);
        resumeButton.setDisable(!running || !paused);
        stopButton.setDisable(!running);
        emergencyCountSpinner.setDisable(running);
        for (Button btn : speedButtons) {
            btn.setDisable(!running);
        }
        if (running) highlightSpeedButton(speedButtons[0]); // default 1×
    }

    private void highlightSpeedButton(Button active) {
        for (Button btn : speedButtons) {
            btn.setStyle("");
        }
        active.setStyle("-fx-font-weight: bold; -fx-background-color: #5C6BC0; -fx-text-fill: white;");
    }
}
