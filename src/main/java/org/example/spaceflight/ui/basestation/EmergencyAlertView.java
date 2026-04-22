package org.example.spaceflight.ui.basestation;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.alert.AlertIncident;
import org.example.spaceflight.alert.AlertService;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/** Displays active alert incidents and lets the crew resolve them or forward notes to the stewardess. */
public class EmergencyAlertView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;
    private final VBox cardContainer;
    private final AlertService alertService;
    private final Map<String, VBox> cardNodes = new HashMap<>();
    // (incident, message) → deliver message to that passenger's dashboard
    private BiConsumer<AlertIncident, String> onSendToStewardess;

    public EmergencyAlertView(AlertService alertService) {
        this.alertService = alertService;

        Label title = new Label("Alerts");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        cardContainer = new VBox(12);

        root = new VBox(15, title, cardContainer);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        alertService.setOnAlertRaised(incident -> Platform.runLater(() -> addCard(incident)));
        alertService.setOnAlertResolved(incident -> Platform.runLater(() -> removeCard(incident)));
    }

    public void setOnSendToStewardess(BiConsumer<AlertIncident, String> handler) {
        this.onSendToStewardess = handler;
    }

    public Region getRoot() {
        return root;
    }

    private void addCard(AlertIncident incident) {
        AlertIncidentCard card = new AlertIncidentCard(
                incident,
                alertService,
                (inc, msg) -> { if (onSendToStewardess != null) onSendToStewardess.accept(inc, msg); },
                () -> removeCard(incident));
        VBox cardRoot = (VBox) card.getRoot();
        cardNodes.put(incident.getId(), cardRoot);
        cardContainer.getChildren().add(cardRoot);
        log.info("Alert card added for " + incident.getPassenger().getName());
    }

    private void removeCard(AlertIncident incident) {
        VBox cardNode = cardNodes.remove(incident.getId());
        if (cardNode != null) {
            cardContainer.getChildren().remove(cardNode);
            log.info("Alert card removed for " + incident.getPassenger().getName());
        }
    }
}
