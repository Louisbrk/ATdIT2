package org.example.spaceflight.ui.passenger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.alert.AlertIncident;
import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.alert.PsychologicalIncident;
import org.example.spaceflight.alert.PsychologicalSupportService;
import org.example.spaceflight.model.ShuttleState;
import org.example.spaceflight.model.Stewardess;
import org.example.spaceflight.ui.shared.RouteMapCanvas;

import java.lang.invoke.MethodHandles;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** Stewardess dashboard showing the incident inbox, flight status, and emergency notifications. */
public class StewardessInboxView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private final BorderPane root;
    private final Stewardess stewardess;
    private final RouteMapCanvas routeMap;
    private final AlertService alertService;
    private final PsychologicalSupportService psychService;

    // Flight info labels
    private final Label phaseLabel;
    private final Label elapsedLabel;
    private final Label timeLeftLabel;
    private final Label altitudeLabel;

    // Telemetry labels
    private final Label oxygenLabel;
    private final Label altTelLabel;
    private final Label velocityLabel;
    private final Label cabinTempLabel;

    // Sidebar time labels
    private final Label depTimeLabel;
    private final Label arrTimeLabel;

    // Incident cards area (main content)
    private final VBox incidentContainer;
    private final Map<String, VBox> incidentNodes = new HashMap<>();

    // Notifications (sidebar, general only)
    private final VBox notifMessages;

    public StewardessInboxView(Stewardess stewardess, LocalTime departure, LocalTime arrival,
                               AlertService alertService, PsychologicalSupportService psychService) {
        this.stewardess = stewardess;
        this.alertService = alertService;
        this.psychService = psychService;

        root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        // Title bar
        Label title = new Label("Stewardess Dashboard — " + stewardess.getName());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox titleBar = new HBox(title);
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.setStyle("-fx-background-color: #5C6BC0;");

        // === CENTER: route map + telemetry + incidents ===
        VBox centerContent = new VBox(12);
        centerContent.setPadding(new Insets(15));

        routeMap = new RouteMapCanvas(320, 180);

        phaseLabel = new Label("Flight Phase: --");
        phaseLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        elapsedLabel = new Label("Elapsed: --:--");
        elapsedLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        timeLeftLabel = new Label("Remaining: --:--");
        timeLeftLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        altitudeLabel = new Label("Altitude: --");
        altitudeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox flightInfoBox = new VBox(10, phaseLabel, elapsedLabel, timeLeftLabel, altitudeLabel);
        flightInfoBox.setPadding(new Insets(15));
        flightInfoBox.setAlignment(Pos.CENTER);
        flightInfoBox.setStyle("-fx-background-color: #C5CAE9; -fx-background-radius: 8;");

        HBox mapRow = new HBox(12, routeMap, flightInfoBox);
        HBox.setHgrow(flightInfoBox, Priority.ALWAYS);

        // Telemetry bar
        oxygenLabel = createTelemetryBox("--\nOxygen Level");
        altTelLabel = createTelemetryBox("--\nAltitude");
        velocityLabel = createTelemetryBox("--\nVelocity");
        cabinTempLabel = createTelemetryBox("--\nCabin Temp");

        HBox telemetryBar = new HBox(10, oxygenLabel, altTelLabel, velocityLabel, cabinTempLabel);
        telemetryBar.setPadding(new Insets(8));
        telemetryBar.setStyle("-fx-background-color: #B0BEC5; -fx-background-radius: 8;");

        // Incident cards
        Label incidentTitle = new Label("Active Incidents");
        incidentTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        incidentContainer = new VBox(10);
        incidentContainer.setPadding(new Insets(4));

        ScrollPane incidentScroll = new ScrollPane(incidentContainer);
        incidentScroll.setFitToWidth(true);
        incidentScroll.setPrefHeight(200);
        incidentScroll.setStyle("-fx-background: white; -fx-background-color: white;");
        VBox.setVgrow(incidentScroll, Priority.ALWAYS);

        VBox incidentArea = new VBox(6, incidentTitle, incidentScroll);
        incidentArea.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(incidentArea, Priority.ALWAYS);

        centerContent.getChildren().addAll(mapRow, telemetryBar, incidentArea);

        // === RIGHT: sidebar ===
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(15));
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #E8EAF6;");

        // Departure / arrival info
        Label depHeader = new Label("Departure");
        depHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label depLoc = new Label("Base Station");
        depLoc.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        Label depTime = new Label(departure.format(TIME_FMT));
        depTimeLabel = depTime;
        depTime.setStyle("-fx-font-size: 12px;");

        Label arrHeader = new Label("Arrival");
        arrHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label arrLoc = new Label("Base Station");
        arrLoc.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        Label arrTime = new Label(arrival.format(TIME_FMT));
        arrTimeLabel = arrTime;
        arrTime.setStyle("-fx-font-size: 12px;");

        VBox flightSchedule = new VBox(4, depHeader, depLoc, depTime, arrHeader, arrLoc, arrTime);
        flightSchedule.setPadding(new Insets(10));
        flightSchedule.setStyle("-fx-background-color: #ECEFF1; -fx-background-radius: 8;");

        // Alert button
        Button alertButton = new Button("Alert!");
        alertButton.setMaxWidth(Double.MAX_VALUE);
        alertButton.setStyle(
                "-fx-background-color: #F44336; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        alertButton.setOnAction(e -> {
            log.warning("Alert triggered by stewardess " + stewardess.getName());
            alertService.raiseAlert(stewardess, "Manual alert by stewardess");
            addNotification("> Alert triggered!", "#B71C1C");
        });

        // Notifications area (small, general)
        Label notifTitle = new Label("Notifications");
        notifTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        notifMessages = new VBox(6);
        notifMessages.setPadding(new Insets(4));

        ScrollPane notifScroll = new ScrollPane(notifMessages);
        notifScroll.setFitToWidth(true);
        notifScroll.setPrefHeight(160);
        notifScroll.setStyle("-fx-background: #E8EAF6; -fx-background-color: #E8EAF6;");
        VBox.setVgrow(notifScroll, Priority.ALWAYS);

        sidebar.getChildren().addAll(flightSchedule, alertButton, notifTitle, notifScroll);

        root.setTop(titleBar);
        root.setCenter(centerContent);
        root.setRight(sidebar);

        // Passenger alerts that are NOT sent via Send-to-Stewardess go to notifications only
        alertService.setOnAlertRaised(incident ->
                Platform.runLater(() -> pushAlertNotification(incident)));
        alertService.setOnAlertResolved(incident ->
                Platform.runLater(() -> removeIncidentCard(incident.getId())));
    }

    public Region getRoot() {
        return root;
    }

    public Stewardess getStewardess() {
        return stewardess;
    }

    public void updateTimes(LocalTime departure, LocalTime arrival) {
        depTimeLabel.setText(departure.format(TIME_FMT));
        arrTimeLabel.setText(arrival.format(TIME_FMT));
    }

    /**
     * Called by SpaceFlightApp when "Send to Stewardess" is pressed.
     * Creates a full incident card in the center area.
     */
    public void receiveIncident(AlertIncident incident, String crewMessage) {
        Platform.runLater(() -> {
            if (incidentNodes.containsKey(incident.getId())) return;

            StewardessIncidentCard card = new StewardessIncidentCard(
                    incident, crewMessage, "#E53935",
                    () -> {
                        alertService.resolveAlert(incident.getId());
                        removeIncidentCard(incident.getId());
                    });
            VBox cardNode = (VBox) card.getRoot();
            incidentNodes.put(incident.getId(), cardNode);
            incidentContainer.getChildren().add(cardNode);

            addNotification("[" + incident.getPassenger().getName() + "] Alert received", "#1565C0");
            log.info("Stewardess received alert card for " + incident.getPassenger().getName());
        });
    }

    public void receivePsychIncident(PsychologicalIncident incident, String crewMessage) {
        Platform.runLater(() -> {
            if (incidentNodes.containsKey(incident.getId())) return;

            StewardessIncidentCard card = new StewardessIncidentCard(
                    incident, crewMessage, "#7B1FA2",
                    () -> {
                        psychService.resolveRequest(incident.getId());
                        removeIncidentCard(incident.getId());
                    });
            VBox cardNode = (VBox) card.getRoot();
            incidentNodes.put(incident.getId(), cardNode);
            incidentContainer.getChildren().add(cardNode);

            addNotification("[" + incident.getPassenger().getName() + "] Psych support received", "#6A1B9A");
            log.info("Stewardess received psych incident card for " + incident.getPassenger().getName());
        });
    }

    /** Preferred entry point — accepts the serialisation-ready snapshot. */
    public void update(org.example.spaceflight.model.SimulationSnapshot snapshot) {
        update(snapshot.getShuttleState(), snapshot.getEmergencyProgress());
    }

    public void update(ShuttleState state, double emergencyProgress) {
        routeMap.update(state.getRouteProgress(), state.isEmergencyLanding(), emergencyProgress);

        phaseLabel.setText("Flight Phase: " + formatPhase(state.getFlightPhase().name()));
        elapsedLabel.setText("Elapsed: " + formatTime(state.getElapsedSeconds()));
        double remaining = Math.max(0, state.getTotalFlightSeconds() - state.getElapsedSeconds());
        timeLeftLabel.setText("Remaining: " + formatTime(remaining));
        altitudeLabel.setText(String.format("Altitude: %.0f km", state.getAltitudeKm()));

        oxygenLabel.setText(String.format("%.0f%%\nOxygen Level", state.getOxygenPercent()));
        altTelLabel.setText(String.format("%.0f km\nAltitude", state.getAltitudeKm()));
        velocityLabel.setText(String.format("%.0f km/h\nVelocity", state.getVelocityKmph()));
        cabinTempLabel.setText(String.format("%.0f°C\nCabin Temp", state.getCabinTemperature()));
    }

    private void pushAlertNotification(AlertIncident incident) {
        if (incident.getPassenger().equals(stewardess)) return;
        String msg = "[ALERT] " + incident.getPassenger().getName() + ": " + incident.getReason();
        addNotification(msg, "#B71C1C");
        log.info("Stewardess notified of alert from " + incident.getPassenger().getName());
    }

    private void removeIncidentCard(String incidentId) {
        VBox node = incidentNodes.remove(incidentId);
        if (node != null) {
            incidentContainer.getChildren().remove(node);
        }
    }

    /**
     * Called when emergency landing is initiated — shown as a prominent notification.
     */
    public void receiveEmergencyNotification(String message) {
        Platform.runLater(() -> addNotification(message, "#B71C1C"));
    }

    private void addNotification(String text, String color) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + color + "; " +
                "-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 5 7;"
        );
        label.setMaxWidth(Double.MAX_VALUE);
        notifMessages.getChildren().addFirst(label); // newest on top
    }

    private Label createTelemetryBox(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: #5C6BC0; -fx-background-radius: 6; " +
                "-fx-padding: 8 12; -fx-text-alignment: center;"
        );
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
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
