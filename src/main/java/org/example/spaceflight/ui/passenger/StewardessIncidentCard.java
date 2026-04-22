package org.example.spaceflight.ui.passenger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.alert.Incident;
import org.example.spaceflight.model.VitalSigns;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/** Compact card showing one incident forwarded to the stewardess, with resolve action. */
public class StewardessIncidentCard {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;

    public StewardessIncidentCard(Incident incident, String crewMessage,
                                  String cardColor, Runnable onResolve) {
        String passengerName = incident.getPassenger().getName();

        // Header: name + time
        Label nameLabel = new Label(passengerName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label timeLabel = new Label(incident.getFormattedTime());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFE0B2;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(nameLabel, spacer, timeLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // Reason
        Label reasonLabel = new Label(incident.getReason());
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        reasonLabel.setWrapText(true);

        // Crew message from Base Station
        VBox messageBox = buildInfoRow("Message from Base Station:", crewMessage, "#FFF9C4", "#F57F17");

        // Vital signs at time of incident
        VitalSigns vitals = incident.getPassenger().getVitalSigns();
        VBox vitalsBox = buildVitalsBox(vitals);

        // Solved button
        Button solvedButton = new Button("Solved");
        solvedButton.setMaxWidth(Double.MAX_VALUE);
        solvedButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 24;"
        );
        solvedButton.setOnAction(e -> {
            log.info("Stewardess resolved incident for " + passengerName);
            onResolve.run();
        });

        root = new VBox(8, header, reasonLabel, messageBox, vitalsBox, solvedButton);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 10;");
        root.setMaxWidth(Double.MAX_VALUE);
    }

    public Region getRoot() {
        return root;
    }

    private VBox buildInfoRow(String labelText, String value, String bgColor, String textColor) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #FFCCBC; -fx-font-weight: bold;");

        Label val = new Label(value.isBlank() ? "—" : value);
        val.setWrapText(true);
        val.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: " + textColor + "; " +
                "-fx-background-color: " + bgColor + "; -fx-background-radius: 4; -fx-padding: 4 6;"
        );
        val.setMaxWidth(Double.MAX_VALUE);

        return new VBox(2, lbl, val);
    }

    private VBox buildVitalsBox(VitalSigns vitals) {
        Label header = new Label("Vital Signs");
        header.setStyle("-fx-font-size: 11px; -fx-text-fill: #FFCCBC; -fx-font-weight: bold;");

        if (vitals == null) {
            Label noData = new Label("No vitals available");
            noData.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
            return new VBox(2, header, noData);
        }

        Label bpm = new Label(String.format("BPM: %d", vitals.getBpm()));
        Label spo2 = new Label(String.format("SpO₂: %.1f%%", vitals.getSpO2()));
        Label bp = new Label(String.format("BP: %d/%d mmHg", vitals.getSystolicBp(), vitals.getDiastolicBp()));
        Label resp = new Label(String.format("Resp: %d /min", vitals.getRespiratoryRate()));

        for (Label l : new Label[]{bpm, spo2, bp, resp}) {
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        }

        HBox row = new HBox(12, bpm, spo2, bp, resp);
        row.setPadding(new Insets(4, 6, 4, 6));
        row.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 4;");

        return new VBox(2, header, row);
    }
}
