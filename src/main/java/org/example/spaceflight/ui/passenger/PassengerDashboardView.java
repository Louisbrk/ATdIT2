package org.example.spaceflight.ui.passenger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.alert.PsychSeverity;
import org.example.spaceflight.alert.PsychologicalSupportService;
import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.SimulationSnapshot;
import org.example.spaceflight.simulation.ExperienceModeService;
import org.example.spaceflight.ui.passenger.theme.DashboardSkin;
import org.example.spaceflight.ui.shared.RouteMapCanvas;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Passenger-facing window — layout and event wiring only; all logic lives in the presenter. */
public class PassengerDashboardView {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private final BorderPane root;
    private final Passenger passenger;
    private final RouteMapCanvas routeMap;
    private final PassengerSettingsDialog settings;
    private final PassengerDashboardPresenter presenter;

    private final VBox statusMessages;
    private final Button psychButton;
    private final Button alertButton;

    public PassengerDashboardView(Passenger passenger, LocalTime departure, LocalTime arrival,
                                  AlertService alertService, PsychologicalSupportService psychService,
                                  ExperienceModeService modeService) {
        this.passenger = passenger;

        root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        settings  = new PassengerSettingsDialog(root, this::onLanguageChanged);
        presenter = new PassengerDashboardPresenter(
                passenger, alertService, psychService, modeService, settings);

        // === Title bar ===
        Label titleLabel = new Label("Flight Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        Button gearButton = new Button("⚙");
        gearButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-cursor: hand; -fx-border-color: transparent;"
        );
        gearButton.setOnAction(e -> settings.show());
        HBox titleBar = new HBox(titleLabel, titleSpacer, gearButton);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 15, 10, 15));

        // === Route map + flight info ===
        routeMap = new RouteMapCanvas(320, 180);

        Label phaseLabel    = new Label("Flight Phase: --");
        Label elapsedLabel  = new Label("Elapsed: --:--");
        Label timeLeftLabel = new Label("Remaining: --:--");
        Label kmLeftLabel   = new Label("Altitude: --");

        VBox flightInfoBox = new VBox(10, phaseLabel, elapsedLabel, timeLeftLabel, kmLeftLabel);
        flightInfoBox.setPadding(new Insets(15));
        flightInfoBox.setAlignment(Pos.CENTER);

        HBox mapRow = new HBox(12, routeMap, flightInfoBox);
        HBox.setHgrow(flightInfoBox, Priority.ALWAYS);

        // === Telemetry bar ===
        Label oxygenLabel    = telemetryBox("--\nOxygen Level");
        Label altitudeLabel  = telemetryBox("--\nAltitude");
        Label velocityLabel  = telemetryBox("--\nVelocity");
        Label cabinTempLabel = telemetryBox("--\nCabin Temp");

        HBox telemetryBar = new HBox(10, oxygenLabel, altitudeLabel, velocityLabel, cabinTempLabel);
        telemetryBar.setPadding(new Insets(8));

        // === Status messages ===
        Label statusTitle = new Label("Status");
        statusTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        statusMessages = new VBox(4);
        statusMessages.setPadding(new Insets(8));
        ScrollPane statusScroll = new ScrollPane(statusMessages);
        statusScroll.setFitToWidth(true);
        statusScroll.setPrefHeight(80);
        statusScroll.setStyle("-fx-background: white;");
        VBox statusArea = new VBox(6, statusTitle, statusScroll);
        statusArea.setPadding(new Insets(10));
        VBox.setVgrow(statusArea, Priority.ALWAYS);

        VBox leftContent = new VBox(12, mapRow, telemetryBar, statusArea);
        leftContent.setPadding(new Insets(15));

        // === Sidebar ===
        Label passengerTitle    = new Label("Passenger");
        passengerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label departureHeader   = new Label("Departure");
        departureHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label departureLocation = new Label("Base Station");
        departureLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        Label departureLabel    = new Label(departure.format(TIME_FMT));
        departureLabel.setStyle("-fx-font-size: 12px;");
        Label arrivalHeader     = new Label("Arrival");
        arrivalHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label arrivalLocation   = new Label("Base Station");
        arrivalLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        Label arrivalLabel      = new Label(arrival.format(TIME_FMT));
        arrivalLabel.setStyle("-fx-font-size: 12px;");

        VBox infoBox = new VBox(4, passengerTitle,
                departureHeader, departureLocation, departureLabel,
                arrivalHeader, arrivalLocation, arrivalLabel);
        infoBox.setPadding(new Insets(10));
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #ECEFF1; -fx-background-radius: 8;");

        alertButton = new Button("Alert!");
        alertButton.setMaxWidth(Double.MAX_VALUE);
        alertButton.setOnAction(e -> presenter.onAlertClicked());

        psychButton = new Button("Psychological\nHelp");
        psychButton.setMaxWidth(Double.MAX_VALUE);
        psychButton.setOnAction(e -> openPsychDialog());
        psychButton.setVisible(passenger.getExperienceMode() == ExperienceMode.RELAXED);
        psychButton.setManaged(passenger.getExperienceMode() == ExperienceMode.RELAXED);

        Label modeTitle = new Label("Experience Modes");
        modeTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton relaxRadio  = radio("Relax",  ExperienceMode.RELAXED,  modeGroup);
        RadioButton normalRadio = radio("Normal", ExperienceMode.NORMAL,   modeGroup);
        RadioButton actionRadio = radio("Action", ExperienceMode.ACTION,   modeGroup);

        switch (passenger.getExperienceMode()) {
            case RELAXED -> relaxRadio.setSelected(true);
            case NORMAL  -> normalRadio.setSelected(true);
            case ACTION  -> actionRadio.setSelected(true);
        }

        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                presenter.onModeSelected((ExperienceMode) newVal.getUserData());
            }
        });

        VBox modeBox = new VBox(6, modeTitle, relaxRadio, normalRadio, actionRadio);
        modeBox.setPadding(new Insets(8));

        VBox sidebar = new VBox(15, infoBox, alertButton, psychButton, modeBox);
        sidebar.setPadding(new Insets(15));
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPrefWidth(200);

        root.setTop(titleBar);
        root.setCenter(leftContent);
        root.setRight(sidebar);

        // Wire presenter callbacks
        presenter.setSkin(DashboardSkin.builder()
                .titleBar(titleBar)
                .leftContent(leftContent)
                .sidebar(sidebar)
                .flightInfoBox(flightInfoBox)
                .telemetryBar(telemetryBar)
                .mapRow(mapRow)
                .titleLabel(titleLabel)
                .phaseLabel(phaseLabel)
                .elapsedLabel(elapsedLabel)
                .timeLeftLabel(timeLeftLabel)
                .kmLeftLabel(kmLeftLabel)
                .oxygenLabel(oxygenLabel)
                .altitudeLabel(altitudeLabel)
                .velocityLabel(velocityLabel)
                .cabinTempLabel(cabinTempLabel)
                .alertButton(alertButton)
                .psychButton(psychButton)
                .statusTitle(statusTitle)
                .statusScroll(statusScroll)
                .statusMessages(statusMessages)
                .statusArea(statusArea)
                .infoBox(infoBox)
                .passengerTitle(passengerTitle)
                .departureHeader(departureHeader)
                .departureLocation(departureLocation)
                .departureLabel(departureLabel)
                .arrivalHeader(arrivalHeader)
                .arrivalLocation(arrivalLocation)
                .arrivalLabel(arrivalLabel)
                .modeTitle(modeTitle)
                .modeBox(modeBox)
                .relaxRadio(relaxRadio)
                .normalRadio(normalRadio)
                .actionRadio(actionRadio)
                .build());

        presenter.setStatusMessageSink(this::addStatusMessage);
        presenter.setPsychButtonVisibilitySink(visible -> {
            psychButton.setVisible(visible);
            psychButton.setManaged(visible);
        });
        presenter.setRootStyleSink(root::setStyle);

        presenter.applyTheme();
        presenter.applyLanguage();
    }

    public Region getRoot() {
        return root;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void update(SimulationSnapshot snapshot) {
        routeMap.update(snapshot.getShuttleState().getRouteProgress(),
                snapshot.getShuttleState().isEmergencyLanding(),
                snapshot.getEmergencyProgress());
        presenter.update(snapshot.getShuttleState(), snapshot.getEmergencyProgress());
    }


    public void updateTimes(LocalTime departure, LocalTime arrival) {
        presenter.updateTimes(departure, arrival);
    }

    public void addStatusMessage(String message) {
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        statusMessages.getChildren().add(msgLabel);
    }

    private void onLanguageChanged() {
        presenter.applyLanguage();
    }

    private void openPsychDialog() {
        Stage dialog = new Stage();
        dialog.setTitle(presenter.t("I need help", "Ich brauche Hilfe"));
        dialog.setResizable(false);

        Label titleLabel = new Label(presenter.t("I need help", "Ich brauche Hilfe"));
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");
        HBox header = new HBox(titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        Label urgencyLabel = new Label(presenter.t("How urgent is it?", "Wie dringend ist es?"));
        urgencyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2E7D32;");

        PsychSeverity[] selected = {PsychSeverity.MEDIUM};
        Button calmBtn  = severityBtn(presenter.t("Calm",  "Ruhig"),      "#A5D6A7", "#1B5E20");
        Button tenseBtn = severityBtn(presenter.t("Tense", "Angespannt"), "#FFE082", "#5D4037");
        Button panicBtn = severityBtn(presenter.t("Panic", "Panik"),      "#EF9A9A", "#B71C1C");

        Button[] btns   = {calmBtn, tenseBtn, panicBtn};
        PsychSeverity[] vals = {PsychSeverity.LOW, PsychSeverity.MEDIUM, PsychSeverity.HIGH};
        String[] colors = {"#A5D6A7", "#FFE082", "#EF9A9A"};
        String[] texts  = {"#1B5E20", "#5D4037", "#B71C1C"};

        Runnable refreshStyles = () -> {
            for (int i = 0; i < 3; i++) {
                boolean active = selected[0] == vals[i];
                btns[i].setStyle(
                        "-fx-background-color: " + colors[i] + "; -fx-text-fill: " + texts[i] + "; " +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                        "-fx-cursor: hand; -fx-padding: 10 0; " +
                        (active ? "-fx-border-color: " + texts[i] + "; -fx-border-width: 3; -fx-border-radius: 10;"
                                : "-fx-border-color: transparent; -fx-border-width: 3; -fx-border-radius: 10;")
                );
            }
        };
        calmBtn.setOnAction(e  -> { selected[0] = PsychSeverity.LOW;    refreshStyles.run(); });
        tenseBtn.setOnAction(e -> { selected[0] = PsychSeverity.MEDIUM; refreshStyles.run(); });
        panicBtn.setOnAction(e -> { selected[0] = PsychSeverity.HIGH;   refreshStyles.run(); });
        for (Button b : btns) { HBox.setHgrow(b, Priority.ALWAYS); b.setMaxWidth(Double.MAX_VALUE); }
        HBox severityRow = new HBox(8, calmBtn, tenseBtn, panicBtn);
        refreshStyles.run();

        Label symptomsLabel = new Label(presenter.t("What's wrong?", "Was ist los?"));
        symptomsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2E7D32;");

        String chipBase = "-fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 10 18; -fx-font-size: 14px;";
        String chipOff = chipBase + "-fx-background-color: #DCEDC8; -fx-text-fill: #1B5E20; -fx-border-color: transparent; -fx-border-width: 2; -fx-border-radius: 20;";
        String chipOn  = chipBase + "-fx-background-color: #A5D6A7; -fx-text-fill: #1B5E20; -fx-font-weight: bold; -fx-border-color: #2E7D32; -fx-border-width: 2; -fx-border-radius: 20;";

        ToggleButton[] chips = {
            new ToggleButton(presenter.t("Anxiety",        "Angst")),
            new ToggleButton(presenter.t("Nausea",         "Übelkeit")),
            new ToggleButton(presenter.t("Panic attack",   "Panikattacke")),
            new ToggleButton(presenter.t("Disorientation", "Desorientierung"))
        };
        for (ToggleButton chip : chips) {
            chip.setStyle(chipOff);
            chip.selectedProperty().addListener((obs, was, is) -> chip.setStyle(is ? chipOn : chipOff));
        }
        FlowPane chipPane = new FlowPane(8, 8, chips);
        chipPane.setPrefWrapLength(444);

        Button sendBtn = new Button(presenter.t("Send Help", "Hilfe senden"));
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setStyle(
                "-fx-background-color: #66BB6A; -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand; -fx-padding: 14 0;"
        );
        sendBtn.setOnAction(e -> {
            String symptoms = Arrays.stream(chips)
                    .filter(ToggleButton::isSelected)
                    .map(ToggleButton::getText)
                    .collect(Collectors.joining(", "));
            String message = symptoms.isEmpty()
                    ? presenter.t("Passenger requested psychological support",
                                  "Passagier bat um psychologische Unterstützung")
                    : symptoms;
            presenter.onPsychHelpRequested(selected[0], message);
            dialog.close();
        });

        Button cancelBtn = new Button(presenter.t("Cancel", "Abbrechen"));
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle(
                "-fx-background-color: #BDBDBD; -fx-text-fill: #424242; " +
                "-fx-font-size: 13px; -fx-background-radius: 12; -fx-cursor: hand; -fx-padding: 10 0;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        VBox content = new VBox(20, header, urgencyLabel, severityRow, symptomsLabel, chipPane, sendBtn, cancelBtn);
        content.setPadding(new Insets(28));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: #F1FBF5;");
        dialog.setScene(new Scene(content));
        dialog.centerOnScreen();
        dialog.show();
    }

    private static Label telemetryBox(String text) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        return label;
    }

    private static RadioButton radio(String text, ExperienceMode mode, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setUserData(mode);
        return rb;
    }

    private static Button severityBtn(String label, String bg, String fg) {
        Button btn = new Button(label);
        btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-padding: 10 0; " +
                "-fx-border-color: transparent; -fx-border-width: 3; -fx-border-radius: 10;"
        );
        return btn;
    }
}
