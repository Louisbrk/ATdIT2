package com.spaceflight.support.ui;

import com.spaceflight.support.app.DemoActors;
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
import java.util.Optional;
import java.util.UUID;

public class MainDashboardView {

    private static final DateTimeFormatter HISTORY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PassengerService passengerService;
    private final IncidentService incidentService;

    private final ListView<Passenger> passengerListView = new ListView<>();
    private final ListView<String> historyListView = new ListView<>();
    private final Map<IncidentStatus, VBox> statusColumns = new EnumMap<>(IncidentStatus.class);
    private final Map<IncidentStatus, Label> statusCounters = new EnumMap<>(IncidentStatus.class);

    private final CheckBox supportAvailableCheckBox = new CheckBox("Onboard support available");
    private final TextField titleField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final ComboBox<IncidentSeverity> severityComboBox = new ComboBox<>();
    private final ComboBox<SupportActionType> supportActionComboBox = new ComboBox<>();

    private final Label passengerSummaryLabel = new Label("Select a passenger to inspect the profile and create an incident.");
    private final Label incidentTitleLabel = new Label("No incident selected");
    private final Label incidentPassengerLabel = new Label("-");
    private final Label incidentStatusLabel = new Label("-");
    private final Label incidentSeverityLabel = new Label("-");
    private final Label incidentResponsibleLabel = new Label("-");
    private final Label incidentSupportActionLabel = new Label("-");
    private final TextArea incidentDescriptionArea = new TextArea();

    private final Button applySupportActionButton = new Button("Apply Support Action");
    private final Button escalateButton = new Button("Escalate");
    private final Button takeOverButton = new Button("Base Station Take Over");
    private final Button resolveButton = new Button("Resolve");
    private final Button refreshButton = new Button("Refresh");

    private UUID selectedIncidentId;

    public MainDashboardView(PassengerService passengerService, IncidentService incidentService) {
        this.passengerService = passengerService;
        this.incidentService = incidentService;
    }

    public Scene createScene() {
        configureControls();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f7fbfc, #eaf2f4);");
        root.setTop(buildTopBar());
        root.setLeft(buildPassengerPanel());
        root.setCenter(buildBoardSection());
        root.setRight(buildDetailPanel());

        refreshView();
        return new Scene(root, 1600, 960);
    }

    private void configureControls() {
        severityComboBox.getItems().setAll(IncidentSeverity.values());
        severityComboBox.setValue(IncidentSeverity.LOW);

        supportActionComboBox.getItems().setAll(SupportActionType.values());
        supportActionComboBox.setValue(SupportActionType.REASSURANCE_PROTOCOL);

        descriptionArea.setPromptText("Describe the passenger issue.");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        titleField.setPromptText("Incident title");

        incidentDescriptionArea.setEditable(false);
        incidentDescriptionArea.setWrapText(true);
        incidentDescriptionArea.setPrefRowCount(4);

        passengerSummaryLabel.setWrapText(true);
        incidentTitleLabel.setWrapText(true);

        passengerListView.setCellFactory(list -> new ListCell<>() {
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
        });
        passengerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updatePassengerSummary();
        });

        supportAvailableCheckBox.setSelected(incidentService.isOnboardSupportAvailable());
        supportAvailableCheckBox.setOnAction(event -> updateSupportAvailability());

        applySupportActionButton.setOnAction(event -> runSafely(this::applySupportAction));
        escalateButton.setOnAction(event -> runSafely(this::escalateSelectedIncident));
        takeOverButton.setOnAction(event -> runSafely(this::takeOverSelectedIncident));
        resolveButton.setOnAction(event -> runSafely(this::resolveSelectedIncident));
        refreshButton.setOnAction(event -> refreshView());
    }

    private HBox buildTopBar() {
        Label titleLabel = new Label("Space Flight Support Board");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        Label subtitleLabel = new Label("Manage onboard incidents, fallback escalation, and action history during flight.");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4f6475;");

        VBox titleBox = new VBox(4, titleLabel, subtitleLabel);

        Button normalDemoButton = new Button("Create Normal Demo Case");
        normalDemoButton.setOnAction(event -> runSafely(this::createNormalDemoIncident));

        Button emergencyDemoButton = new Button("Create Emergency Demo Case");
        emergencyDemoButton.setOnAction(event -> runSafely(this::createEmergencyDemoIncident));

        HBox controls = new HBox(12, supportAvailableCheckBox, normalDemoButton, emergencyDemoButton);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(18, titleBox, spacer, controls);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 18, 0));
        return topBar;
    }

    private VBox buildPassengerPanel() {
        Label panelTitle = new Label("Passenger Overview");
        panelTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        passengerListView.setPrefWidth(300);
        passengerListView.setPrefHeight(280);

        Label formTitle = new Label("Create Incident");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button createIncidentButton = new Button("Report Incident");
        createIncidentButton.setMaxWidth(Double.MAX_VALUE);
        createIncidentButton.setOnAction(event -> runSafely(this::createIncidentFromForm));

        VBox panel = new VBox(
                12,
                panelTitle,
                passengerListView,
                createInfoCard("Selected Passenger", passengerSummaryLabel),
                new Separator(),
                formTitle,
                titleField,
                severityComboBox,
                descriptionArea,
                createIncidentButton
        );
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(330);
        panel.setStyle(panelStyle());
        return panel;
    }

    private ScrollPane buildBoardSection() {
        HBox board = new HBox(14);
        board.setPadding(new Insets(0, 12, 0, 12));

        for (IncidentStatus status : IncidentStatus.values()) {
            board.getChildren().add(createBoardColumn(status));
        }

        ScrollPane scrollPane = new ScrollPane(board);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0, 16, 0, 16));
        return scrollPane;
    }

    private VBox createBoardColumn(IncidentStatus status) {
        Label titleLabel = new Label(status.getDisplayName());
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        Label counterLabel = new Label("0 incidents");
        counterLabel.setStyle("-fx-text-fill: #557082;");

        VBox cardContainer = new VBox(10);
        cardContainer.setFillWidth(true);

        VBox column = new VBox(10, titleLabel, counterLabel, cardContainer);
        column.setPadding(new Insets(14));
        column.setPrefWidth(260);
        column.setMinHeight(720);
        column.setStyle(columnStyle(status));

        configureDropTarget(column, status);
        configureDropTarget(cardContainer, status);

        statusColumns.put(status, cardContainer);
        statusCounters.put(status, counterLabel);
        return column;
    }

    private VBox buildDetailPanel() {
        Label panelTitle = new Label("Incident Detail");
        panelTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        FlowPane actionButtons = new FlowPane(8, 8, applySupportActionButton, escalateButton, takeOverButton, resolveButton, refreshButton);
        actionButtons.setPrefWrapLength(280);

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
                new Label("Action History"),
                historyListView
        );
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(350);
        panel.setStyle(panelStyle());
        VBox.setVgrow(historyListView, Priority.ALWAYS);
        return panel;
    }

    private VBox createInfoCard(String title, javafx.scene.Node content) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #557082;");

        VBox wrapper = new VBox(6, titleLabel, content);
        wrapper.setPadding(new Insets(10));
        wrapper.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #d5e3e8; -fx-border-radius: 12;");
        return wrapper;
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
        refreshView();
    }

    private void updateSupportAvailability() {
        incidentService.updateOnboardSupportAvailability(
                supportAvailableCheckBox.isSelected(),
                DemoActors.BASE_STATION_OPERATOR,
                ResponsibleRole.BASE_STATION
        );
        refreshView();
    }

    private void applySupportAction() {
        Incident incident = requireSelectedIncident();
        incidentService.applySupportAction(
                incident.getId(),
                supportActionComboBox.getValue(),
                DemoActors.ONBOARD_SUPPORT_OPERATOR
        );
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

    private void refreshView() {
        supportAvailableCheckBox.setSelected(incidentService.isOnboardSupportAvailable());
        refreshPassengerList();
        refreshBoard();
        refreshIncidentDetail();
        updatePassengerSummary();
    }

    private void refreshPassengerList() {
        UUID selectedPassengerId = Optional.ofNullable(passengerListView.getSelectionModel().getSelectedItem())
                .map(Passenger::getId)
                .orElse(null);

        passengerListView.getItems().setAll(passengerService.getAllPassengers());

        if (selectedPassengerId != null) {
            passengerListView.getItems().stream()
                    .filter(passenger -> passenger.getId().equals(selectedPassengerId))
                    .findFirst()
                    .ifPresent(passenger -> passengerListView.getSelectionModel().select(passenger));
        } else if (!passengerListView.getItems().isEmpty()) {
            passengerListView.getSelectionModel().selectFirst();
        }
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
        passengerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        Label titleLabel = new Label(incident.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 13px;");

        HBox chips = new HBox(
                8,
                createChip(incident.getSeverity().getDisplayName(), severityChipStyle(incident.getSeverity())),
                createChip(incident.getResponsibleRole().getDisplayName(), roleChipStyle(incident.getResponsibleRole()))
        );

        VBox card = new VBox(8, passengerLabel, titleLabel, chips);
        card.setPadding(new Insets(12));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(cardStyle(incident.getId().equals(selectedIncidentId), incident.getSeverity()));

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

    private Label createChip(String text, String style) {
        Label chip = new Label(text);
        chip.setPadding(new Insets(4, 8, 4, 8));
        chip.setStyle(style);
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

    private String panelStyle() {
        return "-fx-background-color: rgba(255,255,255,0.92);"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: #d5e3e8;"
                + "-fx-border-radius: 16;";
    }

    private String columnStyle(IncidentStatus status) {
        return "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: #d0dde2;"
                + "-fx-background-color: " + switch (status) {
            case NEW -> "#fff7d8";
            case ASSESSING -> "#dff1fb";
            case MONITORING -> "#e3f6e7";
            case ESCALATED -> "#fde3e0";
            case RESOLVED -> "#ebf0f2";
        } + ";";
    }

    private String cardStyle(boolean selected, IncidentSeverity severity) {
        String accentColor = switch (severity) {
            case LOW -> "#7ba05b";
            case MEDIUM -> "#e0a421";
            case HIGH -> "#d97706";
            case CRITICAL -> "#c24130";
        };
        String borderWidth = selected ? "2.5" : "1.0";
        return "-fx-background-color: white;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-width: " + borderWidth + ";"
                + "-fx-border-color: " + accentColor + ";";
    }

    private String severityChipStyle(IncidentSeverity severity) {
        return "-fx-background-radius: 999;"
                + "-fx-text-fill: #17324d;"
                + "-fx-background-color: " + switch (severity) {
            case LOW -> "#ddeccd";
            case MEDIUM -> "#fce6a6";
            case HIGH -> "#ffd5a3";
            case CRITICAL -> "#f8c0bb";
        } + ";";
    }

    private String roleChipStyle(ResponsibleRole responsibleRole) {
        return "-fx-background-radius: 999;"
                + "-fx-text-fill: #17324d;"
                + "-fx-background-color: " + switch (responsibleRole) {
            case PASSENGER -> "#e5f2f8";
            case ONBOARD_SUPPORT -> "#d7e7fb";
            case BASE_STATION -> "#f7d8d4";
            case SYSTEM -> "#e6e6e6";
        } + ";";
    }
}
