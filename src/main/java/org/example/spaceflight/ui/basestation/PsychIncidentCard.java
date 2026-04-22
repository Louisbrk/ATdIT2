package org.example.spaceflight.ui.basestation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.alert.PsychologicalIncident;
import org.example.spaceflight.alert.PsychologicalSupportService;
import org.example.spaceflight.ui.shared.UIColors;

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/** Row card for a single PsychologicalIncident shown inside the Psychological Support dashboard. */
public class PsychIncidentCard {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;

    public PsychIncidentCard(PsychologicalIncident incident, PsychologicalSupportService psychService,
                             BiConsumer<PsychologicalIncident, String> onSendToStewardess,
                             Runnable onResolved) {
        String passengerName = incident.getPassenger().getName();
        String severityLabel = incident.getSeverity().name().charAt(0)
                + incident.getSeverity().name().substring(1).toLowerCase();

        // Header: name left, severity right
        Label nameLabel = new Label(passengerName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label severity = new Label(severityLabel);
        severity.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(nameLabel, spacer, severity);
        header.setAlignment(Pos.CENTER_LEFT);

        // Reason / message
        Label reasonLabel = new Label(incident.getReason());
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #EDE7F6;");
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
                log.info("Stewardess notified for psych request of " + passengerName + ": " + note);
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
            psychService.resolveRequest(incident.getId());
            onResolved.run();
        });

        HBox.setHgrow(sendButton, Priority.ALWAYS);
        HBox.setHgrow(solvedButton, Priority.ALWAYS);
        sendButton.setMaxWidth(Double.MAX_VALUE);
        solvedButton.setMaxWidth(Double.MAX_VALUE);

        HBox buttonRow = new HBox(10, sendButton, solvedButton);

        root = new VBox(8, header, reasonLabel, notesArea, buttonRow);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: " + UIColors.PURPLE + "; -fx-background-radius: 10;");
        root.setMaxWidth(380);
    }

    public Region getRoot() {
        return root;
    }
}
