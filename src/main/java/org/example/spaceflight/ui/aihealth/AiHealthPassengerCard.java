package org.example.spaceflight.ui.aihealth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.spaceflight.health.HealthEvaluationResult;
import org.example.spaceflight.health.VitalType;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.VitalSigns;
import org.example.spaceflight.ui.shared.UIColors;

import java.lang.invoke.MethodHandles;
import java.util.Deque;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Compact card showing health classification, live vital values, and 4 mini trend charts
 * (BPM, SpO2, BP systolic, Respiratory Rate) stacked vertically.
 */
/** Card widget showing one passenger's vital signs, health classification, and manual override buttons. */
public class AiHealthPassengerCard {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private static final String COLOR_GREEN    = UIColors.HEALTH_GREEN;
    private static final String COLOR_YELLOW   = UIColors.HEALTH_YELLOW;
    private static final String COLOR_RED      = UIColors.HEALTH_RED;
    private static final String COLOR_CARD     = "#F5F7FA";

    // Chart line colors per vital sign
    private static final Color C_BPM  = Color.web("#E53935"); // red
    private static final Color C_SPO2 = Color.web("#0097A7"); // teal
    private static final Color C_BP   = Color.web("#F57C00"); // orange
    private static final Color C_RR   = Color.web("#388E3C"); // green

    private final VBox root;
    private final Passenger passenger;

    private final Label statusBadge;
    private final Label overrideBadge;
    private final Label modeLabel;

    // Value labels next to each chart
    private final Label bpmVal;
    private final Label spO2Val;
    private final Label bpVal;
    private final Label rrVal;

    // One chart per vital sign
    private final VitalSignsChartCanvas bpmChart;
    private final VitalSignsChartCanvas spO2Chart;
    private final VitalSignsChartCanvas bpChart;
    private final VitalSignsChartCanvas rrChart;

    /**
     * Callback: (passenger, requestedStatus) — wired in by the dashboard.
     */
    private BiConsumer<Passenger, HealthStatus> onOverrideRequested;

    public AiHealthPassengerCard(Passenger passenger) {
        this.passenger = passenger;

        // --- Header: name + status badge ---
        Label nameLabel = new Label(passenger.getName());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #212121;");

        modeLabel = new Label(passenger.getExperienceMode().name());
        modeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #607D8B;");

        VBox nameBox = new VBox(1, nameLabel, modeLabel);

        statusBadge = new Label("GREEN");
        statusBadge.setStyle(badgeStyle(COLOR_GREEN));
        statusBadge.setMinWidth(52);
        statusBadge.setAlignment(Pos.CENTER);

        overrideBadge = new Label("⚑ Override");
        overrideBadge.setStyle("-fx-font-size: 9px; -fx-text-fill: #E64A19;");
        overrideBadge.setVisible(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(6, nameBox, spacer, statusBadge);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // --- Charts + value labels ---
        bpmChart  = new VitalSignsChartCanvas(400, 60, C_BPM);
        spO2Chart = new VitalSignsChartCanvas(400, 60, C_SPO2);
        bpChart   = new VitalSignsChartCanvas(400, 60, C_BP);
        rrChart   = new VitalSignsChartCanvas(400, 60, C_RR);

        bpmVal  = valueLabel("BPM",  "--");
        spO2Val = valueLabel("SpO2", "--");
        bpVal   = valueLabel("BP",   "--");
        rrVal   = valueLabel("RR",   "--");

        VBox chartsColumn = new VBox(3,
                chartRow(bpmVal,  bpmChart),
                chartRow(spO2Val, spO2Chart),
                chartRow(bpVal,   bpChart),
                chartRow(rrVal,   rrChart)
        );

        // --- Override buttons ---
        Button greenBtn  = overrideBtn("G", COLOR_GREEN,  HealthStatus.GREEN);
        Button yellowBtn = overrideBtn("Y", COLOR_YELLOW, HealthStatus.YELLOW);
        Button redBtn    = overrideBtn("R", COLOR_RED,    HealthStatus.RED);

        HBox buttonRow = new HBox(4, greenBtn, yellowBtn, redBtn, overrideBadge);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        // --- Assemble ---
        root = new VBox(6, headerRow, chartsColumn, buttonRow);
        root.setPadding(new Insets(10));
        root.setStyle(cardStyle("#BDBDBD", 1));
    }

    // --- Public API ---

    public Region getRoot() { return root; }

    public void setOnOverrideRequested(BiConsumer<Passenger, HealthStatus> handler) {
        this.onOverrideRequested = handler;
    }

    /**
     * Updates card visuals each simulation tick.
     *
     * @param vitals         current vitals (may be null before first tick)
     * @param result         full evaluation result with per-vital statuses
     * @param manualOverride whether status was set manually
     * @param bpmHistory     BPM history deque
     * @param spO2History    SpO2 history deque
     * @param bpHistory      systolic BP history deque
     * @param rrHistory      respiratory rate history deque
     */
    public void update(VitalSigns vitals, HealthEvaluationResult result, boolean manualOverride,
                       Deque<Double> bpmHistory, Deque<Double> spO2History,
                       Deque<Double> bpHistory,  Deque<Double> rrHistory) {
        modeLabel.setText(passenger.getExperienceMode().name());

        if (vitals != null) {
            bpmVal.setText("BPM\n"  + vitals.getBpm());
            spO2Val.setText("SpO2\n" + String.format("%.1f", vitals.getSpO2()));
            bpVal.setText("BP\n"   + vitals.getSystolicBp());
            rrVal.setText("RR\n"   + vitals.getRespiratoryRate());
        }

        applyStatus(result.getOverallStatus());
        applyVitalColors(result);

        bpmChart.update(bpmHistory,   result.getVitalStatus(VitalType.BPM));
        spO2Chart.update(spO2History, result.getVitalStatus(VitalType.SPO2));
        bpChart.update(bpHistory,     result.getVitalStatus(VitalType.SYSTOLIC_BP));
        rrChart.update(rrHistory,     result.getVitalStatus(VitalType.RESP_RATE));

        overrideBadge.setVisible(manualOverride);
    }

    // --- Private helpers ---

    private HBox chartRow(Label valueLabel, VitalSignsChartCanvas chart) {
        HBox row = new HBox(5, valueLabel, chart);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label valueLabel(String name, String val) {
        Label l = new Label(name + "\n" + val);
        l.setStyle("-fx-font-size: 9px; -fx-text-fill: #546E7A; -fx-alignment: center;");
        l.setMinWidth(32);
        l.setMaxWidth(32);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private void applyStatus(HealthStatus status) {
        String color = switch (status) {
            case GREEN  -> COLOR_GREEN;
            case YELLOW -> COLOR_YELLOW;
            case RED    -> COLOR_RED;
        };
        statusBadge.setText(status.name());
        statusBadge.setStyle(badgeStyle(color));
        root.setStyle(cardStyle(color, 2));
    }

    private void applyVitalColors(HealthEvaluationResult result) {
        applyVitalColor(bpmVal,  result.getVitalStatus(VitalType.BPM));
        applyVitalColor(spO2Val, result.getVitalStatus(VitalType.SPO2));
        applyVitalColor(bpVal,   result.getVitalStatus(VitalType.SYSTOLIC_BP));
        applyVitalColor(rrVal,   result.getVitalStatus(VitalType.RESP_RATE));
    }

    private void applyVitalColor(Label lbl, HealthStatus vitalStatus) {
        String color = switch (vitalStatus) {
            case RED    -> "#C62828";
            case YELLOW -> "#F57F17";
            case GREEN  -> "#546E7A";
        };
        lbl.setStyle("-fx-font-size: 9px; -fx-text-fill: " + color + "; -fx-alignment: center;");
    }

    private Button overrideBtn(String label, String color, HealthStatus target) {
        Button btn = new Button(label);
        btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-background-radius: 3; -fx-padding: 2 7;"
        );
        btn.setOnAction(e -> {
            log.info("Manual override: " + passenger.getName() + " -> " + target);
            if (onOverrideRequested != null) onOverrideRequested.accept(passenger, target);
        });
        return btn;
    }

    private String cardStyle(String borderColor, int borderWidth) {
        return "-fx-background-color: " + COLOR_CARD + ";" +
               "-fx-background-radius: 8;" +
               "-fx-border-color: " + borderColor + ";" +
               "-fx-border-radius: 8;" +
               "-fx-border-width: " + borderWidth + ";";
    }

    private String badgeStyle(String color) {
        return "-fx-background-color: " + color + "; " +
               "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
               "-fx-background-radius: 4; -fx-padding: 2 6;";
    }
}