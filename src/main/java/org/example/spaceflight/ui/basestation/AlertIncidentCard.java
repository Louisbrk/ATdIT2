package org.example.spaceflight.ui.basestation;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.alert.AlertIncident;
import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.ui.shared.UIColors;

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/** Row card for a single AlertIncident shown inside the Emergency Alert dashboard. */
public class AlertIncidentCard {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;

    public AlertIncidentCard(AlertIncident incident, AlertService alertService,
                             BiConsumer<AlertIncident, String> onSendToStewardess,
                             Runnable onResolved) {
        String passengerName = incident.getPassenger().getName();
        String severity = "Critical";

        // Header: passenger name left, severity right
        Label nameLabel = new Label(passengerName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label severityLabel = new Label(severity);
        severityLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(nameLabel, spacer, severityLabel);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Reason text
        Label reasonLabel = new Label(incident.getReason());
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        reasonLabel.setWrapText(true);

        // Notes textarea
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Write here...");
        notesArea.setPrefHeight(70);
        notesArea.setStyle("-fx-background-color: white; -fx-background-radius: 4;");

        // Buttons
        Button sendButton = new Button("Send to Stewardess");
        sendButton.setStyle(
                "-fx-background-color: #BDBDBD; -fx-text-fill: black; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 16;"
        );
        sendButton.setOnAction(e -> {
            String note = notesArea.getText().trim();
            if (!note.isEmpty()) {
                log.info("Stewardess notified for " + passengerName + ": " + note);
                onSendToStewardess.accept(incident, note);
                notesArea.clear();
            }
        });

        Button solvedButton = new Button("Solved");
        solvedButton.setStyle(
                "-fx-background-color: " + UIColors.ACTION_GREEN + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 24;"
        );
        solvedButton.setOnAction(e -> {
            alertService.resolveAlert(incident.getId());
            onResolved.run();
        });

        HBox.setHgrow(sendButton, Priority.ALWAYS);
        HBox.setHgrow(solvedButton, Priority.ALWAYS);
        sendButton.setMaxWidth(Double.MAX_VALUE);
        solvedButton.setMaxWidth(Double.MAX_VALUE);

        HBox buttonRow = new HBox(10, sendButton, solvedButton);

        root = new VBox(8, header, reasonLabel, notesArea, buttonRow);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: " + UIColors.ALERT_RED + "; -fx-background-radius: 10;");
        root.setMaxWidth(380);
    }

    public Region getRoot() {
        return root;
    }
}
