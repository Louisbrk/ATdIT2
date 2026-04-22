package org.example.spaceflight.ui.basestation;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.spaceflight.alert.PsychologicalIncident;
import org.example.spaceflight.alert.PsychologicalSupportService;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/** Displays active psychological support requests and lets the crew resolve or forward them. */
public class PsychologicalSupportView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final VBox root;
    private final VBox cardContainer;
    private final Map<String, VBox> cardNodes = new HashMap<>();
    // tracks active incidents for sorted insertion
    private final List<PsychologicalIncident> activeIncidents = new ArrayList<>();
    private BiConsumer<PsychologicalIncident, String> onSendToStewardess;

    public PsychologicalSupportView(PsychologicalSupportService psychService) {
        Label title = new Label("Support Requests");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        cardContainer = new VBox(12);
        cardContainer.setPadding(new Insets(4));

        ScrollPane scroll = new ScrollPane(cardContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");
        scroll.setFitToHeight(false);

        root = new VBox(15, title, scroll);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: white;");

        psychService.setOnRequestRaised(incident ->
                Platform.runLater(() -> addCard(incident, psychService)));
        psychService.setOnRequestResolved(incident ->
                Platform.runLater(() -> removeCard(incident.getId())));
    }

    public Region getRoot() {
        return root;
    }

    public void setOnSendToStewardess(BiConsumer<PsychologicalIncident, String> handler) {
        this.onSendToStewardess = handler;
    }

    private void addCard(PsychologicalIncident incident, PsychologicalSupportService psychService) {
        if (cardNodes.containsKey(incident.getId())) return;

        PsychIncidentCard card = new PsychIncidentCard(
                incident, psychService,
                (inc, note) -> {
                    inc.setStewardessNote(note);
                    if (onSendToStewardess != null) onSendToStewardess.accept(inc, note);
                },
                () -> removeCard(incident.getId()));

        VBox cardNode = (VBox) card.getRoot();
        cardNodes.put(incident.getId(), cardNode);

        // Insert sorted: HIGH first, then MEDIUM, then LOW
        activeIncidents.add(incident);
        activeIncidents.sort(Comparator.comparingInt(
                i -> -i.getSeverity().ordinal()));  // ordinal: LOW=0 MEDIUM=1 HIGH=2 → negate for descending
        int insertIndex = activeIncidents.indexOf(incident);
        cardContainer.getChildren().add(insertIndex, cardNode);

        log.info("Psych support card added for " + incident.getPassenger().getName()
                + " [" + incident.getSeverity() + "] at position " + insertIndex);
    }

    private void removeCard(String id) {
        VBox node = cardNodes.remove(id);
        if (node != null) {
            cardContainer.getChildren().remove(node);
        }
        activeIncidents.removeIf(i -> i.getId().equals(id));
    }
}
