package org.example.spaceflight.ui.aihealth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.example.spaceflight.health.HealthEvaluationResult;
import org.example.spaceflight.health.IHealthEvaluationOrchestrator;
import org.example.spaceflight.model.FlightPhase;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.ui.shared.UIColors;

import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AI Health dashboard — three columns (CRITICAL / WARNING / STABLE).
 * Each card shows four stacked mini charts (BPM, SpO2, BP, RR) with live history.
 */
/** AI Health dashboard — classifies all passengers per tick and shows them in three health-status columns. */
public class AiHealthDashboardView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private static final int HISTORY_MAX = 60;

    private final ScrollPane root;

    private final Map<Passenger, AiHealthPassengerCard> cardMap = new LinkedHashMap<>();

    // One deque per vital sign per passenger
    private final Map<Passenger, Deque<Double>> bpmHistory  = new LinkedHashMap<>();
    private final Map<Passenger, Deque<Double>> spO2History = new LinkedHashMap<>();
    private final Map<Passenger, Deque<Double>> bpHistory   = new LinkedHashMap<>();
    private final Map<Passenger, Deque<Double>> rrHistory   = new LinkedHashMap<>();

    private final VBox redColumn;
    private final VBox yellowColumn;
    private final VBox greenColumn;

    private IHealthEvaluationOrchestrator orchestrator;

    public AiHealthDashboardView(List<Passenger> passengers) {
        for (Passenger p : passengers) {
            AiHealthPassengerCard card = new AiHealthPassengerCard(p);
            card.setOnOverrideRequested((passenger, requestedStatus) -> {
                passenger.setHealthStatus(requestedStatus);
                passenger.setManualOverride(true);
                log.info("Override applied: " + passenger.getName() + " -> " + requestedStatus);
                refreshCards(passengers);
            });
            cardMap.put(p, card);
            bpmHistory.put(p,  new ArrayDeque<>(HISTORY_MAX));
            spO2History.put(p, new ArrayDeque<>(HISTORY_MAX));
            bpHistory.put(p,   new ArrayDeque<>(HISTORY_MAX));
            rrHistory.put(p,   new ArrayDeque<>(HISTORY_MAX));
        }

        // --- Column layout ---
        redColumn    = column();
        yellowColumn = column();
        greenColumn  = column();

        VBox redSection    = new VBox(0, columnHeader("CRITICAL", UIColors.HEALTH_RED), redColumn);
        VBox yellowSection = new VBox(0, columnHeader("WARNING",  UIColors.HEALTH_YELLOW), yellowColumn);
        VBox greenSection  = new VBox(0, columnHeader("STABLE",   UIColors.HEALTH_GREEN), greenColumn);

        HBox columnRow = new HBox(0, redSection, divider(), yellowSection, divider(), greenSection);
        HBox.setHgrow(redSection,    Priority.ALWAYS);
        HBox.setHgrow(yellowSection, Priority.ALWAYS);
        HBox.setHgrow(greenSection,  Priority.ALWAYS);

        Label header = new Label("AI Health Monitor");
        header.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold; " +
                "-fx-text-fill: #212121; -fx-padding: 12 16 2 16;"
        );
        Label subHeader = new Label("Live classification · 4 vital trends · Manual override");
        subHeader.setStyle("-fx-font-size: 11px; -fx-text-fill: #607D8B; -fx-padding: 0 16 10 16;");

        VBox content = new VBox(0, header, subHeader, columnRow);
        content.setStyle("-fx-background-color: white;");
        VBox.setVgrow(columnRow, Priority.ALWAYS);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background: white; -fx-background-color: white;");

        renderColumns(passengers);
    }

    // --- Public API ---

    public Region getRoot() { return root; }

    public void setOrchestrator(IHealthEvaluationOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /** Called each simulation tick via Platform.runLater. */
    public void update(List<Passenger> passengers, FlightPhase phase) {
        // Record history first so charts always show the newest point
        for (Passenger p : passengers) {
            if (p.getVitalSigns() == null) continue;
            appendCapped(bpmHistory.get(p),  p.getVitalSigns().getBpm());
            appendCapped(spO2History.get(p), p.getVitalSigns().getSpO2());
            appendCapped(bpHistory.get(p),   p.getVitalSigns().getSystolicBp());
            appendCapped(rrHistory.get(p),   p.getVitalSigns().getRespiratoryRate());
        }

        if (orchestrator != null) {
            orchestrator.evaluate(passengers, phase);
        }

        refreshCards(passengers);
    }

    // --- Private helpers ---

    private void refreshCards(List<Passenger> passengers) {
        List<Passenger> sorted = new ArrayList<>(passengers);
        sorted.sort(Comparator.comparingInt(p -> statusOrder(p.getHealthStatus())));

        for (Passenger p : sorted) {
            AiHealthPassengerCard card = cardMap.get(p);
            if (card == null) continue;
            card.update(
                    p.getVitalSigns(),
                    orchestrator != null ? orchestrator.getLatestResult(p) : HealthEvaluationResult.allGreen(),
                    p.isManualOverride(),
                    dequeOf(bpmHistory, p),
                    dequeOf(spO2History, p),
                    dequeOf(bpHistory, p),
                    dequeOf(rrHistory, p)
            );
        }

        renderColumns(sorted);
    }

    private void renderColumns(List<Passenger> ordered) {
        redColumn.getChildren().clear();
        yellowColumn.getChildren().clear();
        greenColumn.getChildren().clear();

        for (Passenger p : ordered) {
            AiHealthPassengerCard card = cardMap.get(p);
            if (card == null) continue;
            switch (p.getHealthStatus()) {
                case RED    -> redColumn.getChildren().add(card.getRoot());
                case YELLOW -> yellowColumn.getChildren().add(card.getRoot());
                case GREEN  -> greenColumn.getChildren().add(card.getRoot());
            }
        }
    }

    private void appendCapped(Deque<Double> deque, double value) {
        if (deque == null) return;
        if (deque.size() >= HISTORY_MAX) deque.pollFirst();
        deque.addLast(value);
    }

    private Deque<Double> dequeOf(Map<Passenger, Deque<Double>> map, Passenger p) {
        return map.getOrDefault(p, new ArrayDeque<>());
    }

    private VBox column() {
        VBox col = new VBox(8);
        col.setPadding(new Insets(10, 10, 10, 10));
        return col;
    }

    private Label columnHeader(String title, String color) {
        Label lbl = new Label(title);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER_LEFT);
        lbl.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + "; " +
                "-fx-padding: 8 16 8 16; " +
                "-fx-border-color: transparent transparent " + color + " transparent; " +
                "-fx-border-width: 0 0 2 0; " +
                "-fx-background-color: #F5F5F5;"
        );
        return lbl;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefWidth(1);
        r.setStyle("-fx-background-color: #E0E0E0;");
        return r;
    }

    private int statusOrder(HealthStatus status) {
        return switch (status) {
            case RED    -> 0;
            case YELLOW -> 1;
            case GREEN  -> 2;
        };
    }
}