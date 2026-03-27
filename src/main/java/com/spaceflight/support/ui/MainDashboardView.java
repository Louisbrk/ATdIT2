package com.spaceflight.support.ui;

import com.spaceflight.support.app.DemoActors;
import com.spaceflight.support.domain.enums.FlightPhase;
import com.spaceflight.support.domain.enums.IncidentSeverity;
import com.spaceflight.support.domain.enums.IncidentStatus;
import com.spaceflight.support.domain.enums.ResponsibleRole;
import com.spaceflight.support.domain.enums.SupportActionType;
import com.spaceflight.support.domain.model.Incident;
import com.spaceflight.support.domain.model.IncidentActionLog;
import com.spaceflight.support.domain.model.Passenger;
import com.spaceflight.support.service.IncidentService;
import com.spaceflight.support.service.PassengerService;
import com.spaceflight.support.service.dto.CreateIncidentCommand;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MainDashboardView {

    private static final DateTimeFormatter HISTORY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PassengerService passengerService;
    private final IncidentService incidentService;

    private final ListView<Passenger> passengerListView = new ListView<>();
    private final ComboBox<Passenger> passengerConsoleComboBox = new ComboBox<>();
    private final ListView<String> historyListView = new ListView<>();
    private final Map<IncidentStatus, VBox> statusColumns = new EnumMap<>(IncidentStatus.class);
    private final Map<IncidentStatus, Label> statusCounters = new EnumMap<>(IncidentStatus.class);

    private final CheckBox supportReadyCheckBox = new CheckBox("Onboard support team ready");
    private final ComboBox<FlightPhase> flightPhaseComboBox = new ComboBox<>();
    private final TextField titleField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final ComboBox<IncidentSeverity> severityComboBox = new ComboBox<>();
    private final ComboBox<SupportActionType> supportActionComboBox = new ComboBox<>();

    private final Label supportStatusValueLabel = new Label("-");
    private final Label passengerCountValueLabel = new Label("-");
    private final Label activeIncidentCountValueLabel = new Label("-");
    private final Label escalatedIncidentCountValueLabel = new Label("-");
    private final Label missionStatusValueLabel = new Label("-");
    private final Label phaseConstraintLabel = new Label("-");

    private final Label passengerSummaryLabel = new Label("Select a passenger to inspect the profile and create an incident.");
    private final Label incidentTitleLabel = new Label("No incident selected");
    private final Label incidentPassengerLabel = new Label("-");
    private final Label incidentStatusLabel = new Label("-");
    private final Label incidentSeverityLabel = new Label("-");
    private final Label incidentResponsibleLabel = new Label("-");
    private final Label incidentSupportActionLabel = new Label("-");
    private final TextArea incidentDescriptionArea = new TextArea();

    private final Label passengerConsoleSelectedPassengerLabel = new Label("-");
    private final Label passengerConsolePhaseLabel = new Label("-");
    private final Label passengerConsoleFallbackLabel = new Label("-");
    private final Label passengerConsoleStatusLabel = new Label("Select a passenger and use the in-seat display to trigger help.");
    private final Label passengerConsoleHelpResultLabel = new Label("No passenger interaction yet.");

    private final Button applySupportActionButton = new Button("Apply Support Action");
    private final Button escalateButton = new Button("Escalate");
    private final Button takeOverButton = new Button("Base Station Take Over");
    private final Button resolveButton = new Button("Resolve");
    private final Button refreshButton = new Button("Refresh");

    private UUID selectedIncidentId;
    private boolean syncingPassengerSelection;

    public MainDashboardView(PassengerService passengerService, IncidentService incidentService) {
        this.passengerService = passengerService;
        this.incidentService = incidentService;
    }

    public Scene createScene() {
        configureControls();

        VBox root = new VBox(18, buildApplicationHeader(), buildTabPane());
        root.setPadding(new Insets(18));
        root.getStyleClass().add("app-root");
        VBox.setVgrow(root.getChildren().get(1), Priority.ALWAYS);

        Scene scene = new Scene(root, 1700, 1000);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("dashboard.css"), "dashboard.css is missing")
                        .toExternalForm()
        );

        refreshView();
        return scene;
    }

    private void configureControls() {
        flightPhaseComboBox.getItems().setAll(FlightPhase.values());
        flightPhaseComboBox.setValue(FlightPhase.ORBIT_SERVICE);
        supportReadyCheckBox.setSelected(true);

        severityComboBox.getItems().setAll(IncidentSeverity.values());
        severityComboBox.setValue(IncidentSeverity.LOW);

        supportActionComboBox.getItems().setAll(SupportActionType.values());
        supportActionComboBox.setValue(SupportActionType.REASSURANCE_PROTOCOL);

        titleField.setPromptText("Incident title");
        descriptionArea.setPromptText("Describe the passenger issue.");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        incidentDescriptionArea.setEditable(false);
        incidentDescriptionArea.setWrapText(true);
        incidentDescriptionArea.setPrefRowCount(4);

        passengerSummaryLabel.setWrapText(true);
        incidentTitleLabel.setWrapText(true);
        phaseConstraintLabel.setWrapText(true);
        missionStatusValueLabel.setWrapText(true);
        passengerConsoleStatusLabel.setWrapText(true);
        passengerConsoleHelpResultLabel.setWrapText(true);

        historyListView.setPlaceholder(createMutedLabel("No action history yet."));
        passengerListView.setPlaceholder(createMutedLabel("No passengers loaded."));

        passengerListView.setCellFactory(list -> createPassengerCell());
        passengerConsoleComboBox.setCellFactory(list -> createPassengerCell());
        passengerConsoleComboBox.setButtonCell(createPassengerCell());

        passengerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                syncConsolePassengerSelection(newValue);
            }
            updatePassengerSummary();
            updatePassengerConsoleStatus();
        });

        passengerConsoleComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                syncListPassengerSelection(newValue);
            }
            updatePassengerConsoleStatus();
        });

        flightPhaseComboBox.setOnAction(event -> updateSupportAvailabilityFromContext());
        supportReadyCheckBox.setOnAction(event -> updateSupportAvailabilityFromContext());

        applySupportActionButton.setOnAction(event -> runSafely(this::applySupportAction));
        escalateButton.setOnAction(event -> runSafely(this::escalateSelectedIncident));
        takeOverButton.setOnAction(event -> runSafely(this::takeOverSelectedIncident));
        resolveButton.setOnAction(event -> runSafely(this::resolveSelectedIncident));
        refreshButton.setOnAction(event -> refreshView());
    }

    private HBox buildApplicationHeader() {
        Label eyebrow = new Label("Space Flight Phase | JavaFX Proof of Concept");
        eyebrow.getStyleClass().add("hero-eyebrow");

        Label titleLabel = new Label("In-Flight Passenger Support & Emergency Escalation");
        titleLabel.getStyleClass().add("app-title");

        Label subtitleLabel = new Label(
                "Passenger seat display + mission-control workflow for normal support cases, fallback escalation, and traceable action logging."
        );
        subtitleLabel.getStyleClass().add("hero-subtitle");
        subtitleLabel.setWrapText(true);

        VBox titleBox = new VBox(4, eyebrow, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label scopeChip = new Label("Implemented: interaction, drag-and-drop workflow, fallback handling");
        scopeChip.getStyleClass().add("scope-chip");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(18, titleBox, spacer, scopeChip);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private TabPane buildTabPane() {
        Tab missionControlTab = new Tab("Mission Control");
        missionControlTab.setClosable(false);
        missionControlTab.setContent(buildMissionControlTab());

        Tab passengerConsoleTab = new Tab("Passenger Console");
        passengerConsoleTab.setClosable(false);
        passengerConsoleTab.setContent(buildPassengerConsoleTab());

        TabPane tabPane = new TabPane(missionControlTab, passengerConsoleTab);
        tabPane.getStyleClass().add("mission-tabs");
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        return tabPane;
    }

    private VBox buildMissionControlTab() {
        VBox content = new VBox(18, buildMissionControlHero(), buildMissionControlWorkspace());
        VBox.setVgrow(content.getChildren().get(1), Priority.ALWAYS);
        return content;
    }

    private HBox buildMissionControlHero() {
        VBox commandPanel = new VBox(
                18,
                buildMissionControlIntro(),
                buildAvailabilityControls(),
                buildHeroMetricStrip(),
                buildScenarioButtons(),
                createInfoCard("Why this matters", phaseConstraintLabel)
        );
        commandPanel.setPadding(new Insets(22));
        commandPanel.getStyleClass().addAll("panel", "hero-panel");
        HBox.setHgrow(commandPanel, Priority.ALWAYS);

        VBox flightInfoPanel = buildFlightInformationPanel();
        flightInfoPanel.setPrefWidth(360);

        HBox hero = new HBox(18, commandPanel, flightInfoPanel);
        hero.setAlignment(Pos.TOP_LEFT);
        return hero;
    }

    private VBox buildMissionControlIntro() {
        Label eyebrow = new Label("Mission Control - Support Interface");
        eyebrow.getStyleClass().add("hero-eyebrow");

        Label titleLabel = new Label("Structured support handling during flight");
        titleLabel.getStyleClass().add("hero-title");

        Label subtitleLabel = new Label(
                "Use the board to triage incidents, model launch/landing constraints, and hand over critical or fallback cases to base station."
        );
        subtitleLabel.getStyleClass().add("hero-subtitle");
        subtitleLabel.setWrapText(true);

        Label missionStatusTitle = new Label("Current mission status");
        missionStatusTitle.getStyleClass().add("micro-title");
        missionStatusValueLabel.getStyleClass().add("mission-status");

        return new VBox(8, eyebrow, titleLabel, subtitleLabel, missionStatusTitle, missionStatusValueLabel);
    }

    private HBox buildAvailabilityControls() {
        Label phaseLabel = new Label("Flight phase");
        phaseLabel.getStyleClass().add("micro-title");
        VBox phaseBox = new VBox(6, phaseLabel, flightPhaseComboBox);

        Label crewLabel = new Label("Crew readiness");
        crewLabel.getStyleClass().add("micro-title");
        VBox crewBox = new VBox(6, crewLabel, supportReadyCheckBox);

        refreshButton.getStyleClass().add("secondary-button");

        HBox controls = new HBox(18, phaseBox, crewBox, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);
        return controls;
    }

    private HBox buildHeroMetricStrip() {
        HBox strip = new HBox(
                12,
                createMetricCard("Support mode", supportStatusValueLabel),
                createMetricCard("Passengers onboard", passengerCountValueLabel),
                createMetricCard("Active cases", activeIncidentCountValueLabel),
                createMetricCard("Escalated", escalatedIncidentCountValueLabel)
        );
        strip.setAlignment(Pos.CENTER_LEFT);
        return strip;
    }

    private VBox createMetricCard(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("metric-label");
        valueLabel.getStyleClass().add("metric-value");

        VBox card = new VBox(8, label, valueLabel);
        card.getStyleClass().add("metric-card");
        card.setPrefWidth(190);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private HBox buildScenarioButtons() {
        Button normalDemoButton = new Button("Create Normal Demo Case");
        normalDemoButton.getStyleClass().add("secondary-button");
        normalDemoButton.setOnAction(event -> runSafely(this::createNormalDemoIncident));

        Button emergencyDemoButton = new Button("Create Emergency Demo Case");
        emergencyDemoButton.getStyleClass().add("secondary-button");
        emergencyDemoButton.setOnAction(event -> runSafely(this::createEmergencyDemoIncident));

        HBox buttons = new HBox(12, normalDemoButton, emergencyDemoButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        return buttons;
    }

    private VBox buildFlightInformationPanel() {
        Label panelTitle = new Label("Flight Information");
        panelTitle.getStyleClass().add("panel-title");

        Label panelSubtitle = new Label("Figma-inspired mission overview translated into JavaFX for the runnable prototype.");
        panelSubtitle.getStyleClass().add("panel-subtitle");
        panelSubtitle.setWrapText(true);

        VBox timeCard = new VBox(6, createMutedLabel("Estimated Time Remaining"), createHighlightedValueLabel("04:21:19"));
        timeCard.getStyleClass().add("flight-highlight");

        HBox systemMetrics = new HBox(
                10,
                createSystemMetricCard("Altitude", "266 km"),
                createSystemMetricCard("Velocity", "28728 km/h"),
                createSystemMetricCard("Oxygen", "98 %")
        );

        VBox panel = new VBox(
                16,
                panelTitle,
                panelSubtitle,
                createFlightFact("Departure", "Earth Station Alpha", "Mar 27, 2026 - 09:30 UTC"),
                createFlightFact("Arrival", "Mars Colony Omega", "Mar 27, 2026 - 14:15 UTC"),
                timeCard,
                systemMetrics
        );
        panel.setPadding(new Insets(22));
        panel.getStyleClass().addAll("panel", "flight-panel");
        return panel;
    }

    private VBox createFlightFact(String labelText, String valueText, String captionText) {
        Label label = createMutedLabel(labelText);
        Label value = new Label(valueText);
        value.getStyleClass().add("flight-fact-value");
        Label caption = createMutedLabel(captionText);
        return new VBox(4, label, value, caption);
    }

    private VBox createSystemMetricCard(String labelText, String valueText) {
        Label valueLabel = new Label(valueText);
        valueLabel.getStyleClass().add("system-metric-value");

        Label label = createMutedLabel(labelText);
        label.getStyleClass().add("system-metric-label");

        VBox card = new VBox(6, valueLabel, label);
        card.getStyleClass().add("system-metric-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private HBox buildMissionControlWorkspace() {
        VBox boardPanel = buildBoardPanel();
        HBox.setHgrow(boardPanel, Priority.ALWAYS);

        HBox workspace = new HBox(18, buildPassengerPanel(), boardPanel, buildDetailPanel());
        HBox.setHgrow(boardPanel, Priority.ALWAYS);
        VBox.setVgrow(workspace, Priority.ALWAYS);
        return workspace;
    }

    private VBox buildPassengerPanel() {
        Label panelTitle = new Label("Passenger Intake");
        panelTitle.getStyleClass().add("panel-title");

        Label panelSubtitle = new Label(
                "Create incidents manually from onboard observations or select a passenger whose seat display triggered a help request."
        );
        panelSubtitle.getStyleClass().add("panel-subtitle");
        panelSubtitle.setWrapText(true);

        passengerListView.setPrefWidth(340);
        passengerListView.setPrefHeight(260);

        Label formTitle = new Label("Manual Incident Creation");
        formTitle.getStyleClass().add("section-title");

        Button createIncidentButton = new Button("Report Incident");
        createIncidentButton.setMaxWidth(Double.MAX_VALUE);
        createIncidentButton.getStyleClass().add("secondary-button");
        createIncidentButton.setOnAction(event -> runSafely(this::createIncidentFromForm));

        VBox panel = new VBox(
                12,
                panelTitle,
                panelSubtitle,
                passengerListView,
                createInfoCard("Selected Passenger", passengerSummaryLabel),
                new Separator(),
                formTitle,
                titleField,
                severityComboBox,
                descriptionArea,
                createIncidentButton
        );
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(340);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private VBox buildBoardPanel() {
        Label titleLabel = new Label("Incident Workflow Board");
        titleLabel.getStyleClass().add("panel-title");

        Label subtitleLabel = new Label(
                "Drag cards between valid states only. Invalid transitions are blocked by the service layer, not by the UI."
        );
        subtitleLabel.getStyleClass().add("panel-subtitle");
        subtitleLabel.setWrapText(true);

        ScrollPane boardScrollPane = buildBoardSection();
        VBox.setVgrow(boardScrollPane, Priority.ALWAYS);

        VBox panel = new VBox(12, titleLabel, subtitleLabel, boardScrollPane);
        panel.setPadding(new Insets(18));
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private ScrollPane buildBoardSection() {
        HBox board = new HBox(14);
        board.getStyleClass().add("board-container");

        for (IncidentStatus status : IncidentStatus.values()) {
            board.getChildren().add(createBoardColumn(status));
        }

        ScrollPane scrollPane = new ScrollPane(board);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("board-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    private VBox createBoardColumn(IncidentStatus status) {
        Label titleLabel = new Label(status.getDisplayName());
        titleLabel.getStyleClass().add("column-title");

        Label counterLabel = new Label("0 incidents");
        counterLabel.getStyleClass().add("column-counter");

        VBox cardContainer = new VBox(10);
        cardContainer.setFillWidth(true);

        VBox column = new VBox(10, titleLabel, counterLabel, cardContainer);
        column.setPadding(new Insets(14));
        column.setPrefWidth(270);
        column.setMinHeight(760);
        column.getStyleClass().addAll("board-column", columnStyleClass(status));

        configureDropTarget(column, status);
        configureDropTarget(cardContainer, status);

        statusColumns.put(status, cardContainer);
        statusCounters.put(status, counterLabel);
        return column;
    }

    private VBox buildDetailPanel() {
        Label panelTitle = new Label("Incident Detail");
        panelTitle.getStyleClass().add("panel-title");

        FlowPane actionButtons = new FlowPane(8, 8, applySupportActionButton, escalateButton, takeOverButton, resolveButton);
        actionButtons.setPrefWrapLength(300);

        Button[] primaryButtons = {applySupportActionButton, escalateButton, takeOverButton, resolveButton};
        for (Button button : primaryButtons) {
            button.getStyleClass().add("secondary-button");
        }

        VBox panel = new VBox(
                12,
                panelTitle,
                createInfoCard("Title", incidentTitleLabel),
                createInfoCard("Passenger", incidentPassengerLabel),
                createInfoCard("Status", incidentStatusLabel),
                createInfoCard("Severity", incidentSeverityLabel),
                createInfoCard("Responsible Role", incidentResponsibleLabel),
                createInfoCard("Support Action", incidentSupportActionLabel),
                createInfoCard("Description", incidentDescriptionArea),
                new Separator(),
                new Label("Support Workflow"),
                supportActionComboBox,
                actionButtons,
                new Separator(),
                new Label("Fallback & Specialists"),
                createInfoCard("Medical Backup (mocked)", createWrappedLabel(
                        "Remote doctor at base station supports escalated medical incidents in the next iteration."
                )),
                createInfoCard("Psychology / Chatbot (mocked)", createWrappedLabel(
                        "Seat-display calming guidance can start immediately; psychologist follow-up is a realistic extension."
                )),
                new Separator(),
                new Label("Action History"),
                historyListView
        );
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(360);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(historyListView, Priority.ALWAYS);
        return panel;
    }

    private BorderPane buildPassengerConsoleTab() {
        VBox console = new VBox(
                18,
                buildPassengerConsoleHero(),
                buildPassengerConsoleMetrics(),
                buildPassengerConsoleActions(),
                buildPassengerConsoleFeedback()
        );
        console.setPadding(new Insets(4, 0, 0, 0));
        return new BorderPane(console);
    }

    private VBox buildPassengerConsoleHero() {
        Label eyebrow = new Label("Mission Control - Passenger Interface");
        eyebrow.getStyleClass().add("hero-eyebrow");

        Label titleLabel = new Label("Space Flight Dashboard");
        titleLabel.getStyleClass().add("hero-title");

        Label subtitleLabel = new Label(
                "This seat display is intentionally interactive: passengers can request help, trigger escalation, and stay informed during launch, orbit, and landing."
        );
        subtitleLabel.getStyleClass().add("hero-subtitle");
        subtitleLabel.setWrapText(true);

        Label selectorLabel = new Label("Passenger");
        selectorLabel.getStyleClass().add("micro-title");
        passengerConsoleComboBox.setMaxWidth(280);

        HBox headerRow = new HBox(18, new VBox(8, eyebrow, titleLabel, subtitleLabel), new VBox(6, selectorLabel, passengerConsoleComboBox));
        HBox.setHgrow(headerRow.getChildren().get(0), Priority.ALWAYS);
        headerRow.setAlignment(Pos.TOP_LEFT);

        HBox summaryRow = new HBox(
                12,
                createConsoleSummaryCard("Passenger", passengerConsoleSelectedPassengerLabel),
                createConsoleSummaryCard("Flight Phase", passengerConsolePhaseLabel),
                createConsoleSummaryCard("Current Support Mode", passengerConsoleFallbackLabel)
        );

        HBox flightInfoRow = new HBox(
                12,
                createConsoleRouteCard("Departure", "Earth Station Alpha", "Mar 27, 2026 - 09:30 UTC"),
                createConsoleRouteCard("Arrival", "Mars Colony Omega", "Mar 27, 2026 - 14:15 UTC"),
                createConsoleTimeCard()
        );

        VBox hero = new VBox(18, headerRow, summaryRow, flightInfoRow);
        hero.setPadding(new Insets(22));
        hero.getStyleClass().addAll("panel", "passenger-console-panel");
        return hero;
    }

    private HBox buildPassengerConsoleMetrics() {
        return new HBox(
                12,
                createSystemMetricCard("Altitude", "266 km"),
                createSystemMetricCard("Velocity", "28728 km/h"),
                createSystemMetricCard("Cabin Temp", "21 °C"),
                createSystemMetricCard("Oxygen Level", "98 %")
        );
    }

    private VBox buildPassengerConsoleActions() {
        Label titleLabel = new Label("Interactive Support Actions");
        titleLabel.getStyleClass().add("panel-title");

        Label subtitleLabel = new Label(
                "These actions create real incidents in the workflow board. When crew movement is blocked, the system falls back to remote guidance and escalation."
        );
        subtitleLabel.getStyleClass().add("panel-subtitle");
        subtitleLabel.setWrapText(true);

        Button medicalHelpButton = createActionButton("Medical Help", "action-medical", this::createPassengerMedicalIncident);
        Button psychologicalHelpButton = createActionButton("Psychological Help", "action-psychological", this::createPassengerPsychologicalIncident);
        Button requestOnboardSupportButton = createActionButton("Request Onboard Support", "action-onboard", this::createPassengerComfortIncident);
        Button emergencyButton = createActionButton("EMERGENCY", "action-emergency", this::createPassengerEmergencyIncident);

        FlowPane buttons = new FlowPane(12, 12, medicalHelpButton, psychologicalHelpButton, requestOnboardSupportButton, emergencyButton);
        buttons.setPrefWrapLength(1400);

        VBox panel = new VBox(12, titleLabel, subtitleLabel, buttons);
        panel.setPadding(new Insets(18));
        panel.getStyleClass().add("panel");
        return panel;
    }

    private VBox buildPassengerConsoleFeedback() {
        Label titleLabel = new Label("System Response");
        titleLabel.getStyleClass().add("panel-title");

        VBox panel = new VBox(
                12,
                titleLabel,
                createInfoCard("Current Guidance", passengerConsoleStatusLabel),
                createInfoCard("Last Interaction Result", passengerConsoleHelpResultLabel)
        );
        panel.setPadding(new Insets(18));
        panel.getStyleClass().add("panel");
        return panel;
    }

    private VBox createConsoleSummaryCard(String title, Label valueLabel) {
        Label titleLabel = createMutedLabel(title);
        valueLabel.getStyleClass().add("console-summary-value");
        valueLabel.setWrapText(true);

        VBox card = new VBox(6, titleLabel, valueLabel);
        card.getStyleClass().add("metric-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox createConsoleRouteCard(String title, String destination, String timestamp) {
        Label titleLabel = createMutedLabel(title);
        Label destinationLabel = new Label(destination);
        destinationLabel.getStyleClass().add("flight-fact-value");
        Label timestampLabel = createMutedLabel(timestamp);

        VBox card = new VBox(6, titleLabel, destinationLabel, timestampLabel);
        card.getStyleClass().add("metric-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox createConsoleTimeCard() {
        Label titleLabel = createMutedLabel("Estimated Time Remaining");
        Label valueLabel = createHighlightedValueLabel("04:21:19");
        VBox card = new VBox(6, titleLabel, valueLabel);
        card.getStyleClass().add("flight-highlight");
        card.setPrefWidth(250);
        return card;
    }

    private Button createActionButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("quick-action-button", styleClass);
        button.setPrefSize(260, 92);
        button.setOnAction(event -> runSafely(action));
        return button;
    }

    private VBox createInfoCard(String title, javafx.scene.Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("info-card-title");

        if (content instanceof Label label) {
            label.getStyleClass().add("info-card-value");
            label.setWrapText(true);
        }
        if (content instanceof TextArea area) {
            area.getStyleClass().add("readonly-text-area");
        }

        VBox wrapper = new VBox(6, titleLabel, content);
        wrapper.setPadding(new Insets(10));
        wrapper.getStyleClass().add("info-card");
        return wrapper;
    }

    private Label createWrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        return label;
    }

    private Label createMutedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        label.setWrapText(true);
        return label;
    }

    private Label createHighlightedValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("flight-highlight-value");
        return label;
    }

    private ListCell<Passenger> createPassengerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Passenger passenger, boolean empty) {
                super.updateItem(passenger, empty);
                if (empty || passenger == null) {
                    setText(null);
                } else {
                    setText(passenger.getFullName() + " | " + passenger.getTier().getDisplayName()
                            + " | Seat " + passenger.getSeatLabel());
                }
            }
        };
    }

    private void configureDropTarget(Region region, IncidentStatus targetStatus) {
        region.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            UUID incidentId = dragboard.hasString() ? tryParseUuid(dragboard.getString()) : null;
            if (incidentId != null && incidentService.canMoveIncident(incidentId, targetStatus)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        region.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard dragboard = event.getDragboard();
            UUID incidentId = dragboard.hasString() ? tryParseUuid(dragboard.getString()) : null;
            if (incidentId != null) {
                Incident incident = incidentService.getIncidentById(incidentId);
                ResponsibleRole actorRole = incident.getResponsibleRole() == ResponsibleRole.BASE_STATION
                        ? ResponsibleRole.BASE_STATION
                        : ResponsibleRole.ONBOARD_SUPPORT;
                String actorName = actorRole == ResponsibleRole.BASE_STATION
                        ? DemoActors.BASE_STATION_OPERATOR
                        : DemoActors.ONBOARD_SUPPORT_OPERATOR;
                try {
                    incidentService.moveIncident(incidentId, targetStatus, actorName, actorRole);
                    selectedIncidentId = incidentId;
                    success = true;
                    refreshView();
                } catch (RuntimeException exception) {
                    showError(exception.getMessage());
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void createIncidentFromForm() {
        Passenger selectedPassenger = passengerListView.getSelectionModel().getSelectedItem();
        if (selectedPassenger == null) {
            throw new IllegalStateException("Select a passenger before creating an incident.");
        }

        Incident incident = incidentService.createIncident(new CreateIncidentCommand(
                selectedPassenger.getId(),
                titleField.getText(),
                descriptionArea.getText(),
                severityComboBox.getValue(),
                selectedPassenger.getFullName(),
                ResponsibleRole.PASSENGER
        ));

        selectedIncidentId = incident.getId();
        titleField.clear();
        descriptionArea.clear();
        severityComboBox.setValue(IncidentSeverity.LOW);
        refreshView();
    }

    private void createNormalDemoIncident() {
        Passenger ben = findPassengerByName("Ben Cooper").orElseGet(this::firstPassenger);
        Incident incident = incidentService.createIncident(new CreateIncidentCommand(
                ben.getId(),
                "Passenger anxiety during weightlessness",
                "Passenger reports stress and requests calm step-by-step guidance.",
                IncidentSeverity.MEDIUM,
                ben.getFullName(),
                ResponsibleRole.PASSENGER
        ));
        selectedIncidentId = incident.getId();
        supportActionComboBox.setValue(SupportActionType.REASSURANCE_PROTOCOL);
        passengerConsoleHelpResultLabel.setText("Psychological help request sent from the seat display and queued for support review.");
        refreshView();
    }

    private void createEmergencyDemoIncident() {
        Passenger jennifer = findPassengerByName("Jennifer Monroe").orElseGet(this::firstPassenger);
        Incident incident = incidentService.createIncident(new CreateIncidentCommand(
                jennifer.getId(),
                "Severe panic and breathing difficulty",
                "Passenger becomes visibly distressed and reports breathing difficulty during the flight.",
                IncidentSeverity.CRITICAL,
                jennifer.getFullName(),
                ResponsibleRole.PASSENGER
        ));
        selectedIncidentId = incident.getId();
        supportActionComboBox.setValue(SupportActionType.MEDICAL_CHECKLIST);
        passengerConsoleHelpResultLabel.setText("Critical case created. The incident is escalated automatically for base-station support.");
        refreshView();
    }

    private void updateSupportAvailabilityFromContext() {
        incidentService.updateOnboardSupportAvailability(
                supportReadyCheckBox.isSelected() && isCrewMovementAllowed(),
                DemoActors.BASE_STATION_OPERATOR,
                ResponsibleRole.BASE_STATION
        );
        refreshView();
    }

    private void applySupportAction() {
        Incident incident = requireSelectedIncident();
        incidentService.applySupportAction(incident.getId(), supportActionComboBox.getValue(), DemoActors.ONBOARD_SUPPORT_OPERATOR);
        refreshView();
    }

    private void escalateSelectedIncident() {
        Incident incident = requireSelectedIncident();
        ResponsibleRole actorRole = incident.getResponsibleRole() == ResponsibleRole.BASE_STATION
                ? ResponsibleRole.BASE_STATION
                : ResponsibleRole.ONBOARD_SUPPORT;
        String actorName = actorRole == ResponsibleRole.BASE_STATION
                ? DemoActors.BASE_STATION_OPERATOR
                : DemoActors.ONBOARD_SUPPORT_OPERATOR;

        incidentService.escalateIncident(
                incident.getId(),
                "Escalated from the dashboard for immediate remote support.",
                actorName,
                actorRole
        );
        refreshView();
    }

    private void takeOverSelectedIncident() {
        Incident incident = requireSelectedIncident();
        incidentService.takeOverEscalatedIncident(incident.getId(), DemoActors.BASE_STATION_OPERATOR);
        refreshView();
    }

    private void resolveSelectedIncident() {
        Incident incident = requireSelectedIncident();
        ResponsibleRole actorRole = incident.getResponsibleRole() == ResponsibleRole.BASE_STATION
                ? ResponsibleRole.BASE_STATION
                : ResponsibleRole.ONBOARD_SUPPORT;
        String actorName = actorRole == ResponsibleRole.BASE_STATION
                ? DemoActors.BASE_STATION_OPERATOR
                : DemoActors.ONBOARD_SUPPORT_OPERATOR;

        incidentService.resolveIncident(
                incident.getId(),
                "Passenger stabilized and case closed for the demo.",
                actorName,
                actorRole
        );
        refreshView();
    }

    private void createPassengerMedicalIncident() {
        createPassengerConsoleIncident(
                "Medical help requested from seat display",
                "Passenger uses the in-seat display to request medical support.",
                IncidentSeverity.HIGH,
                SupportActionType.MEDICAL_CHECKLIST,
                "Medical support request sent to onboard support. Continue calming guidance on the seat display.",
                "Crew cannot respond physically right now. The request is traceable and has been routed to fallback support."
        );
    }

    private void createPassengerPsychologicalIncident() {
        createPassengerConsoleIncident(
                "Psychological support requested",
                "Passenger reports anxiety and requests reassurance or guided breathing support.",
                IncidentSeverity.MEDIUM,
                SupportActionType.GUIDED_BREATHING,
                "Guided reassurance requested. Onboard support can review the incident and continue with calming assistance.",
                "Self-guided calming instructions are shown first while the system routes the case to remote fallback support."
        );
    }

    private void createPassengerComfortIncident() {
        createPassengerConsoleIncident(
                "Passenger requests onboard assistance",
                "Passenger requests direct crew support via the seat display.",
                IncidentSeverity.LOW,
                SupportActionType.REASSURANCE_PROTOCOL,
                "Request sent. The passenger will stay visible in the mission-control board until support closes the case.",
                "Direct crew movement is currently blocked. The request remains logged and visible for later follow-up."
        );
    }

    private void createPassengerEmergencyIncident() {
        createPassengerConsoleIncident(
                "Emergency triggered from seat display",
                "Passenger pressed the emergency button on the in-seat display.",
                IncidentSeverity.CRITICAL,
                SupportActionType.MEDICAL_CHECKLIST,
                "Emergency request sent. Base station escalation is activated automatically.",
                "Emergency request sent. Base station escalation is activated automatically."
        );
    }

    private void createPassengerConsoleIncident(
            String title,
            String description,
            IncidentSeverity severity,
            SupportActionType suggestedAction,
            String availableMessage,
            String fallbackMessage
    ) {
        Passenger selectedPassenger = requireConsolePassenger();
        Incident incident = incidentService.createIncident(new CreateIncidentCommand(
                selectedPassenger.getId(),
                title,
                description,
                severity,
                selectedPassenger.getFullName(),
                ResponsibleRole.PASSENGER
        ));
        selectedIncidentId = incident.getId();
        supportActionComboBox.setValue(suggestedAction);
        passengerConsoleHelpResultLabel.setText(
                incidentService.isOnboardSupportAvailable() && severity != IncidentSeverity.CRITICAL
                        ? availableMessage
                        : fallbackMessage
        );
        refreshView();
    }

    private Passenger requireConsolePassenger() {
        Passenger passenger = passengerConsoleComboBox.getSelectionModel().getSelectedItem();
        if (passenger == null) {
            throw new IllegalStateException("Select a passenger in the passenger console first.");
        }
        return passenger;
    }

    private void refreshView() {
        refreshPassengerSelectors();
        refreshDashboardMetrics();
        refreshBoard();
        refreshIncidentDetail();
        updatePassengerSummary();
        updatePassengerConsoleStatus();
    }

    private void refreshPassengerSelectors() {
        UUID selectedPassengerId = Optional.ofNullable(passengerConsoleComboBox.getSelectionModel().getSelectedItem())
                .map(Passenger::getId)
                .orElseGet(() -> Optional.ofNullable(passengerListView.getSelectionModel().getSelectedItem())
                        .map(Passenger::getId)
                        .orElse(null));

        List<Passenger> passengers = passengerService.getAllPassengers();
        passengerListView.getItems().setAll(passengers);
        passengerConsoleComboBox.getItems().setAll(passengers);

        Passenger passengerToSelect = passengers.stream()
                .filter(passenger -> passenger.getId().equals(selectedPassengerId))
                .findFirst()
                .orElseGet(() -> passengers.isEmpty() ? null : passengers.get(0));

        if (passengerToSelect != null) {
            syncingPassengerSelection = true;
            passengerListView.getSelectionModel().select(passengerToSelect);
            passengerConsoleComboBox.getSelectionModel().select(passengerToSelect);
            syncingPassengerSelection = false;
        }
    }

    private void refreshDashboardMetrics() {
        List<Incident> incidents = incidentService.getAllIncidents();
        long activeCases = incidents.stream().filter(incident -> !incident.isResolved()).count();
        long escalatedCases = incidents.stream()
                .filter(incident -> !incident.isResolved())
                .filter(incident -> incident.getStatus() == IncidentStatus.ESCALATED
                        || incident.getResponsibleRole() == ResponsibleRole.BASE_STATION)
                .count();
        long criticalCases = incidents.stream()
                .filter(incident -> !incident.isResolved())
                .filter(incident -> incident.getSeverity() == IncidentSeverity.CRITICAL)
                .count();

        supportStatusValueLabel.setText(determineSupportModeText());
        passengerCountValueLabel.setText(String.valueOf(passengerService.getAllPassengers().size()));
        activeIncidentCountValueLabel.setText(String.valueOf(activeCases));
        escalatedIncidentCountValueLabel.setText(String.valueOf(escalatedCases));

        if (!isCrewMovementAllowed()) {
            missionStatusValueLabel.setText("Seatbelt phase active. Physical onboard assistance is paused.");
        } else if (!supportReadyCheckBox.isSelected()) {
            missionStatusValueLabel.setText("Crew fallback mode active. Base station supervises all unresolved cases.");
        } else if (criticalCases > 0) {
            missionStatusValueLabel.setText("Critical support activity detected. Monitor escalated incidents closely.");
        } else {
            missionStatusValueLabel.setText("All systems nominal. Normal onboard handling remains available.");
        }

        phaseConstraintLabel.setText(flightPhaseComboBox.getValue().getOperationalNote());
    }

    private void refreshBoard() {
        for (VBox column : statusColumns.values()) {
            column.getChildren().clear();
        }

        Map<IncidentStatus, Long> counters = new EnumMap<>(IncidentStatus.class);
        for (IncidentStatus status : IncidentStatus.values()) {
            counters.put(status, 0L);
        }

        for (Incident incident : incidentService.getAllIncidents()) {
            VBox column = statusColumns.get(incident.getStatus());
            if (column != null) {
                column.getChildren().add(createIncidentCard(incident));
                counters.put(incident.getStatus(), counters.get(incident.getStatus()) + 1);
            }
        }

        for (IncidentStatus status : IncidentStatus.values()) {
            Label counter = statusCounters.get(status);
            if (counter != null) {
                long count = counters.get(status);
                counter.setText(count + (count == 1 ? " incident" : " incidents"));
            }
        }
    }

    private VBox createIncidentCard(Incident incident) {
        Passenger passenger = passengerService.getPassengerById(incident.getPassengerId());

        Label passengerLabel = new Label(passenger.getFullName());
        passengerLabel.getStyleClass().add("incident-passenger");

        Label titleLabel = new Label(incident.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("incident-title");

        Label statusLabel = new Label(incident.getStatus().getDisplayName());
        statusLabel.getStyleClass().add("incident-meta");

        HBox chips = new HBox(
                8,
                createChip(incident.getSeverity().getDisplayName(), severityChipClass(incident.getSeverity())),
                createChip(incident.getResponsibleRole().getDisplayName(), roleChipClass(incident.getResponsibleRole()))
        );

        VBox card = new VBox(8, passengerLabel, titleLabel, statusLabel, chips);
        card.setPadding(new Insets(12));
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().addAll("incident-card", severityCardClass(incident.getSeverity()));
        if (incident.getId().equals(selectedIncidentId)) {
            card.getStyleClass().add("incident-card-selected");
        }

        card.setOnMouseClicked(event -> {
            selectedIncidentId = incident.getId();
            refreshIncidentDetail();
            refreshBoard();
        });

        card.setOnDragDetected(event -> {
            Dragboard dragboard = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(incident.getId().toString());
            dragboard.setContent(clipboardContent);
            event.consume();
        });

        return card;
    }

    private Label createChip(String text, String styleClass) {
        Label chip = new Label(text);
        chip.getStyleClass().addAll("chip", styleClass);
        chip.setPadding(new Insets(4, 8, 4, 8));
        return chip;
    }

    private void refreshIncidentDetail() {
        List<Incident> incidents = incidentService.getAllIncidents();
        if (selectedIncidentId == null && !incidents.isEmpty()) {
            selectedIncidentId = incidents.get(0).getId();
        }

        Incident incident = incidents.stream()
                .filter(current -> current.getId().equals(selectedIncidentId))
                .findFirst()
                .orElse(null);

        if (incident == null) {
            incidentTitleLabel.setText("No incident selected");
            incidentPassengerLabel.setText("-");
            incidentStatusLabel.setText("-");
            incidentSeverityLabel.setText("-");
            incidentResponsibleLabel.setText("-");
            incidentSupportActionLabel.setText("-");
            incidentDescriptionArea.clear();
            historyListView.getItems().clear();
            updateActionButtons(null);
            return;
        }

        Passenger passenger = passengerService.getPassengerById(incident.getPassengerId());
        incidentTitleLabel.setText(incident.getTitle());
        incidentPassengerLabel.setText(passenger.getFullName());
        incidentStatusLabel.setText(incident.getStatus().getDisplayName());
        incidentSeverityLabel.setText(incident.getSeverity().getDisplayName());
        incidentResponsibleLabel.setText(incident.getResponsibleRole().getDisplayName());
        incidentSupportActionLabel.setText(incident.getSelectedSupportAction() == null
                ? "No action selected"
                : incident.getSelectedSupportAction().getDisplayName());
        incidentDescriptionArea.setText(incident.getDescription());

        historyListView.getItems().setAll(
                incident.getActionLog().stream()
                        .map(this::formatActionLog)
                        .toList()
        );

        updateActionButtons(incident);
    }

    private void updatePassengerSummary() {
        Passenger passenger = passengerListView.getSelectionModel().getSelectedItem();
        if (passenger == null) {
            passengerSummaryLabel.setText("No passenger selected.");
            return;
        }

        long activeIncidents = incidentService.getAllIncidents().stream()
                .filter(incident -> incident.getPassengerId().equals(passenger.getId()))
                .filter(incident -> !incident.isResolved())
                .count();

        passengerSummaryLabel.setText(
                passenger.getTier().getDisplayName() + " passenger in seat " + passenger.getSeatLabel()
                        + ". Active incidents: " + activeIncidents
                        + ". Notes: " + passenger.getProfileNotes()
        );
    }

    private void updatePassengerConsoleStatus() {
        Passenger passenger = passengerConsoleComboBox.getSelectionModel().getSelectedItem();
        if (passenger == null) {
            passengerConsoleSelectedPassengerLabel.setText("-");
            passengerConsolePhaseLabel.setText("-");
            passengerConsoleFallbackLabel.setText("-");
            passengerConsoleStatusLabel.setText("Select a passenger to activate the in-seat support interface.");
            return;
        }

        passengerConsoleSelectedPassengerLabel.setText(
                passenger.getFullName() + " | " + passenger.getTier().getDisplayName() + " | Seat " + passenger.getSeatLabel()
        );
        passengerConsolePhaseLabel.setText(flightPhaseComboBox.getValue().getDisplayName());
        passengerConsoleFallbackLabel.setText(determineSupportModeText());

        if (!isCrewMovementAllowed()) {
            passengerConsoleStatusLabel.setText(
                    "Launch/landing seatbelt phase: crew cannot move through the cabin. The seat display, calming guidance, and base-station fallback remain available."
            );
        } else if (!supportReadyCheckBox.isSelected()) {
            passengerConsoleStatusLabel.setText(
                    "Crew fallback mode: requests are still logged, visible, and escalated to base station when needed."
            );
        } else {
            passengerConsoleStatusLabel.setText(
                    "Normal support mode: passenger requests reach onboard support first and can still be escalated when the case becomes severe."
            );
        }
    }

    private void updateActionButtons(Incident incident) {
        boolean noIncidentSelected = incident == null;
        applySupportActionButton.setDisable(noIncidentSelected
                || incident.getResponsibleRole() != ResponsibleRole.ONBOARD_SUPPORT
                || !incidentService.isOnboardSupportAvailable()
                || !(incident.getStatus() == IncidentStatus.ASSESSING || incident.getStatus() == IncidentStatus.MONITORING));
        escalateButton.setDisable(noIncidentSelected || incident.isResolved());
        takeOverButton.setDisable(noIncidentSelected || incident.getStatus() != IncidentStatus.ESCALATED);
        resolveButton.setDisable(noIncidentSelected || !incidentService.canMoveIncident(incident.getId(), IncidentStatus.RESOLVED));
    }

    private Passenger firstPassenger() {
        return passengerService.getAllPassengers().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No passengers available."));
    }

    private Optional<Passenger> findPassengerByName(String fullName) {
        return passengerService.getAllPassengers().stream()
                .filter(passenger -> passenger.getFullName().equalsIgnoreCase(fullName))
                .findFirst();
    }

    private Incident requireSelectedIncident() {
        if (selectedIncidentId == null) {
            throw new IllegalStateException("Select an incident first.");
        }
        return incidentService.getIncidentById(selectedIncidentId);
    }

    private void syncConsolePassengerSelection(Passenger passenger) {
        if (syncingPassengerSelection || passenger == null) {
            return;
        }
        syncingPassengerSelection = true;
        passengerConsoleComboBox.getSelectionModel().select(passenger);
        syncingPassengerSelection = false;
    }

    private void syncListPassengerSelection(Passenger passenger) {
        if (syncingPassengerSelection || passenger == null) {
            return;
        }
        syncingPassengerSelection = true;
        passengerListView.getSelectionModel().select(passenger);
        syncingPassengerSelection = false;
    }

    private boolean isCrewMovementAllowed() {
        FlightPhase phase = flightPhaseComboBox.getValue();
        return phase != null && phase.isOnboardMovementAllowed();
    }

    private String determineSupportModeText() {
        if (!supportReadyCheckBox.isSelected()) {
            return "Crew unavailable";
        }
        if (!isCrewMovementAllowed()) {
            return "Remote-only phase";
        }
        return "Onboard active";
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Action Failed");
        alert.setHeaderText("The requested action could not be completed.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatActionLog(IncidentActionLog log) {
        return HISTORY_TIME_FORMAT.format(log.getTimestamp())
                + " | " + log.getActorRole().getDisplayName()
                + " | " + log.getActorName()
                + " | " + log.getDescription();
    }

    private UUID tryParseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String columnStyleClass(IncidentStatus status) {
        return switch (status) {
            case NEW -> "column-new";
            case ASSESSING -> "column-assessing";
            case MONITORING -> "column-monitoring";
            case ESCALATED -> "column-escalated";
            case RESOLVED -> "column-resolved";
        };
    }

    private String severityCardClass(IncidentSeverity severity) {
        return switch (severity) {
            case LOW -> "severity-low-card";
            case MEDIUM -> "severity-medium-card";
            case HIGH -> "severity-high-card";
            case CRITICAL -> "severity-critical-card";
        };
    }

    private String severityChipClass(IncidentSeverity severity) {
        return switch (severity) {
            case LOW -> "chip-low";
            case MEDIUM -> "chip-medium";
            case HIGH -> "chip-high";
            case CRITICAL -> "chip-critical";
        };
    }

    private String roleChipClass(ResponsibleRole responsibleRole) {
        return switch (responsibleRole) {
            case PASSENGER -> "chip-passenger";
            case ONBOARD_SUPPORT -> "chip-onboard";
            case BASE_STATION -> "chip-base";
            case SYSTEM -> "chip-system";
        };
    }
}
