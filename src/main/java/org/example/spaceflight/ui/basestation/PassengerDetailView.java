package org.example.spaceflight.ui.basestation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.alert.AlertIncident;
import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;

/** Slide-out detail panel showing full passenger info, health status, and active alarms. */
public class PassengerDetailView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;
    private final Label healthLabel;
    private final Label bpmLabel;
    private final Label spo2Label;
    private final Label bpLabel;
    private final Label respLabel;
    private final Label modeLabel;
    private final VBox alarmsList;
    private final Passenger passenger;
    private final AlertService alertService;

    public PassengerDetailView(Passenger passenger, AlertService alertService, Runnable onBack) {
        this.passenger = passenger;
        this.alertService = alertService;

        // Back button + title
        Button backButton = new Button("←");
        backButton.setStyle("-fx-font-size: 18px; -fx-cursor: hand; -fx-background-color: transparent;");
        backButton.setOnAction(e -> onBack.run());

        Label title = new Label("Passenger Information");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox header = new HBox(10, backButton, title);
        header.setAlignment(Pos.CENTER_LEFT);

        // Info grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);
        infoGrid.add(boldLabel("Name:"), 0, 0);
        infoGrid.add(new Label(passenger.getName()), 1, 0);
        infoGrid.add(boldLabel("Age:"), 0, 1);
        infoGrid.add(new Label(String.valueOf(passenger.getAge())), 1, 1);
        infoGrid.add(boldLabel("Title/Role:"), 0, 2);
        infoGrid.add(new Label(passenger.getRole()), 1, 2);

        // Health status
        Label healthTitle = boldLabel("Health Status:");
        healthLabel = new Label("Good");
        healthLabel.setStyle("-fx-font-size: 13px;");

        // Vitals
        bpmLabel = createValueLabel("BPM: --");
        spo2Label = createValueLabel("SpO2: --");
        bpLabel = createValueLabel("Blood Pressure: --");
        respLabel = createValueLabel("Respiratory Rate: --");

        // Experience mode
        modeLabel = createValueLabel("Experience Mode: " + passenger.getExperienceMode().name());

        // Alarms section
        Label alarmsTitle = boldLabel("Alarms:");
        alarmsList = new VBox(6);

        root = new VBox(12, header, infoGrid,
                healthTitle, healthLabel,
                bpmLabel, spo2Label, bpLabel, respLabel,
                modeLabel, alarmsTitle, alarmsList);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
    }

    public Region getRoot() {
        return root;
    }

    public void update() {
        VitalSigns v = passenger.getVitalSigns();
        if (v != null) {
            bpmLabel.setText("BPM: " + v.getBpm() + " bpm");
            spo2Label.setText(String.format("SpO2: %.0f%%", v.getSpO2()));
            bpLabel.setText("Blood Pressure: " + v.getSystolicBp() + "/" + v.getDiastolicBp());
            respLabel.setText("Respiratory Rate: " + v.getRespiratoryRate() + " bpm");
        }

        modeLabel.setText("Experience Mode: " + passenger.getExperienceMode().name());

        HealthStatus status = passenger.getHealthStatus();
        healthLabel.setText(switch (status) {
            case GREEN -> "Good";
            case YELLOW -> "Warning";
            case RED -> "Critical";
        });

        refreshAlarms();
    }

    private void refreshAlarms() {
        alarmsList.getChildren().clear();

        List<AlertIncident> incidents = alertService.getAllAlertsForPassenger(passenger);
        if (incidents.isEmpty()) {
            Label none = new Label("None");
            none.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
            alarmsList.getChildren().add(none);
            return;
        }

        for (AlertIncident incident : incidents) {
            boolean resolved = incident.isResolved();
            VBox card = new VBox(3);
            card.setPadding(new Insets(8));
            card.setStyle(resolved
                    ? "-fx-background-color: #E8F5E9; -fx-background-radius: 6;"
                    : "-fx-background-color: #FFEBEE; -fx-background-radius: 6;");

            String statusSuffix = resolved ? "  ✓ Solved" : "";
            String headerText = incident.getFormattedTime() + "  —  " + incident.getReason() + statusSuffix;
            Label timeLabel = new Label(headerText);
            timeLabel.setStyle(resolved
                    ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;"
                    : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #C62828;");
            timeLabel.setWrapText(true);
            card.getChildren().add(timeLabel);

            String note = incident.getStewardessNote();
            if (!note.isBlank()) {
                Label noteLabel = new Label("Note: " + note);
                noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
                noteLabel.setWrapText(true);
                card.getChildren().add(noteLabel);
            }

            alarmsList.getChildren().add(card);
        }
    }

    private Label boldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px;");
        return label;
    }
}
