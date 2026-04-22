package org.example.spaceflight.ui.basestation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.model.ShuttleState;
import org.example.spaceflight.ui.shared.RouteMapCanvas;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.logging.Logger;

/** Sidebar panel showing live shuttle telemetry values and the emergency landing button. */
public class FlightInfoPanel {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;
    private final RouteMapCanvas routeMap;
    private final Label fuelLabel;
    private final Label distanceLabel;
    private final Label phaseLabel;
    private final Label altitudeLabel;
    private final Label velocityLabel;
    private final Label elapsedLabel;
    private final Label remainingLabel;
    private final Label plannedLabel;
    private Runnable onEmergencyLanding;
    private double emergencyProgress;

    public FlightInfoPanel() {
        Label title = new Label("Flight Information");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        routeMap = new RouteMapCanvas(280, 160);

        plannedLabel = createValueLabel("Planned: --:--");
        elapsedLabel = createValueLabel("Elapsed: --:--");
        remainingLabel = createValueLabel("Remaining: --:--");
        fuelLabel = createValueLabel("Fuel: --");
        distanceLabel = createValueLabel("Distance: --");
        phaseLabel = createValueLabel("Flight Phase: --");
        altitudeLabel = createValueLabel("Altitude: --");
        velocityLabel = createValueLabel("Velocity: --");

        Button emergencyButton = new Button("Emergency Landing");
        emergencyButton.setStyle(
                "-fx-background-color: #F44336; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        emergencyButton.setOnAction(e -> onEmergencyLandingClicked());

        root = new VBox(8, title, routeMap,
                plannedLabel, elapsedLabel, remainingLabel,
                fuelLabel, distanceLabel, phaseLabel, altitudeLabel, velocityLabel,
                emergencyButton);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 8;");
    }

    public Region getRoot() {
        return root;
    }

    public void setOnEmergencyLanding(Runnable handler) {
        this.onEmergencyLanding = handler;
    }

    public void setEmergencyProgress(double progress) {
        this.emergencyProgress = progress;
    }

    public void update(ShuttleState state) {
        fuelLabel.setText(String.format("Fuel: %.0f%%", state.getFuelPercent()));
        distanceLabel.setText(String.format("Distance: %.0f km", state.getDistanceKm()));
        phaseLabel.setText("Flight Phase: " + formatPhase(state.getFlightPhase().name()));
        altitudeLabel.setText(String.format("Altitude: %.0f km", state.getAltitudeKm()));
        velocityLabel.setText(String.format("Velocity: %.0f km/h", state.getVelocityKmph()));

        plannedLabel.setText("Planned: " + formatTime(state.getTotalFlightSeconds()));
        elapsedLabel.setText("Elapsed: " + formatTime(state.getElapsedSeconds()));
        double remaining = Math.max(0, state.getTotalFlightSeconds() - state.getElapsedSeconds());
        remainingLabel.setText("Remaining: " + formatTime(remaining));

        routeMap.update(state.getRouteProgress(), state.isEmergencyLanding(), emergencyProgress);
    }

    private void onEmergencyLandingClicked() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Emergency Landing");
        alert.setHeaderText("Confirm Emergency Landing");
        alert.setContentText("Are you sure you want to initiate an emergency landing?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            log.warning("Emergency landing confirmed by operator");
            if (onEmergencyLanding != null) {
                onEmergencyLanding.run();
            }
        }
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        return label;
    }

    private String formatPhase(String phase) {
        String lower = phase.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String formatTime(double seconds) {
        int totalSec = (int) seconds;
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }
}
