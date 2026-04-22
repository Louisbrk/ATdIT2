package org.example.spaceflight.ui.passenger;

import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.alert.PsychSeverity;
import org.example.spaceflight.alert.PsychologicalSupportService;
import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.ShuttleState;
import org.example.spaceflight.simulation.ExperienceModeService;
import org.example.spaceflight.ui.passenger.theme.DashboardSkin;
import org.example.spaceflight.ui.passenger.theme.ThemeFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Mediates between PassengerDashboardView and domain services.
 * Owns all domain mutations, formatting, and logging — view only handles layout.
 */
public final class PassengerDashboardPresenter {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final Passenger passenger;
    private final AlertService alertService;
    private final PsychologicalSupportService psychService;
    private final ExperienceModeService modeService;
    private final PassengerSettingsDialog settings;

    private DashboardSkin skin;

    // Cached raw telemetry for label refresh on language/mode switch
    private double lastAltitudeKm   = 0;
    private double lastVelocityKmph = 0;
    private double lastOxygen       = 0;
    private double lastCabinTemp    = 0;

    // Callback so the view can append status messages
    private Consumer<String> statusMessageSink;

    // Callback to toggle psych button visibility when mode changes
    private Consumer<Boolean> psychButtonVisibilitySink;

    // Callback to set background style on the root BorderPane (not in DashboardSkin)
    private Consumer<String> rootStyleSink;

    public PassengerDashboardPresenter(Passenger passenger,
                                       AlertService alertService,
                                       PsychologicalSupportService psychService,
                                       ExperienceModeService modeService,
                                       PassengerSettingsDialog settings) {
        this.passenger    = passenger;
        this.alertService = alertService;
        this.psychService = psychService;
        this.modeService  = modeService;
        this.settings     = settings;
    }

    public void setSkin(DashboardSkin skin) {
        this.skin = skin;
    }

    public void setRootStyleSink(Consumer<String> sink) {
        this.rootStyleSink = sink;
    }

    public void setStatusMessageSink(Consumer<String> sink) {
        this.statusMessageSink = sink;
    }

    public void setPsychButtonVisibilitySink(Consumer<Boolean> sink) {
        this.psychButtonVisibilitySink = sink;
    }

    // --- Domain commands ---

    public void onModeSelected(ExperienceMode mode) {
        modeService.changeMode(passenger, mode);
        updatePsychVisibility();
        applyTheme();
        applyLanguage();
    }

    public void onAlertClicked() {
        log.info("Alert triggered by passenger " + passenger.getName());
        alertService.raiseAlert(passenger, "Manual alert triggered by passenger");
        post(t("> Alert triggered!", "> Alarm ausgelöst!"));
    }

    public void onPsychHelpRequested(PsychSeverity severity, String message) {
        psychService.raiseRequest(passenger, severity, message);
        post(t("> Psychological support requested", "> Psychologische Unterstützung angefordert")
                + " (" + severity.name().toLowerCase() + ")");
        log.info("Psychological help requested by " + passenger.getName() + " [" + severity + "] – " + message);
    }

    // --- State updates ---

    public void update(ShuttleState state, double unusedEmergencyProgress) {
        lastAltitudeKm   = state.getAltitudeKm();
        lastVelocityKmph = state.getVelocityKmph();
        lastOxygen       = state.getOxygenPercent();
        lastCabinTemp    = state.getCabinTemperature();
        refreshFlightLabels(state);
        refreshTelemetryLabels();
    }

    public void updateTimes(LocalTime departure, LocalTime arrival) {
        if (skin == null) return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("h:mm a");
        skin.departureLabel.setText(departure.format(fmt));
        skin.arrivalLabel.setText(arrival.format(fmt));
    }

    public void applyTheme() {
        ExperienceMode mode = passenger.getExperienceMode();
        ThemeFactory.forMode(mode).applyTo(skin);
        if (rootStyleSink != null) {
            String bg = switch (mode) {
                case RELAXED -> "-fx-background-color: #F1FBF5;";
                case NORMAL  -> "-fx-background-color: white;";
                case ACTION  -> "-fx-background-color: #1A1A2E;";
            };
            rootStyleSink.accept(bg);
        }
    }

    public void applyLanguage() {
        ExperienceMode mode = passenger.getExperienceMode();

        skin.titleLabel.setText(t("Flight Dashboard", "Flug-Dashboard"));
        skin.passengerTitle.setText(t("Passenger", "Passagier"));
        skin.departureHeader.setText(t("Departure", "Abflug"));
        skin.departureLocation.setText(t("Base Station", "Basisstation"));
        skin.arrivalHeader.setText(t("Arrival", "Ankunft"));
        skin.arrivalLocation.setText(t("Base Station", "Basisstation"));
        skin.modeTitle.setText(t("Experience Modes", "Erlebnis-Modi"));
        skin.relaxRadio.setText(t("Relax", "Entspannt"));
        skin.normalRadio.setText("Normal");
        skin.actionRadio.setText("Action");
        skin.statusTitle.setText("Status");
        skin.psychButton.setText(t("Psychological\nHelp", "Psychologische\nHilfe"));

        skin.alertButton.setText(switch (mode) {
            case RELAXED -> t("Alert",     "Alarm");
            case NORMAL  -> t("Alert!",    "Alarm!");
            case ACTION  -> t("⚠ ALERT!", "⚠ ALARM!");
        });

        refreshFlightLabelPrefixes();
        refreshTelemetryLabels();
    }

    // --- Translation helper (delegates to settings dialog) ---

    public String t(String en, String de) {
        return settings.t(en, de);
    }

    // --- Private helpers ---

    private void post(String message) {
        if (statusMessageSink != null) statusMessageSink.accept(message);
    }

    private boolean isPsychAvailable() {
        return passenger.getExperienceMode() == ExperienceMode.RELAXED;
    }

    private void updatePsychVisibility() {
        if (psychButtonVisibilitySink != null) psychButtonVisibilitySink.accept(isPsychAvailable());
    }

    private void refreshFlightLabels(ShuttleState state) {
        if (skin == null) return;
        ExperienceMode mode = passenger.getExperienceMode();
        String rawPhase = formatPhase(state.getFlightPhase().name());
        skin.phaseLabel.setText(mode == ExperienceMode.ACTION
                ? rawPhase.toUpperCase()
                : t("Flight Phase: ", "Flugphase: ") + rawPhase);
        skin.elapsedLabel.setText(t("Elapsed: ", "Vergangen: ") + formatTime(state.getElapsedSeconds()));
        double remaining = Math.max(0, state.getTotalFlightSeconds() - state.getElapsedSeconds());
        skin.timeLeftLabel.setText(t("Remaining: ", "Verbleibend: ") + formatTime(remaining));
        skin.kmLeftLabel.setText(String.format(t("Altitude: ", "Höhe: ") + "%.0f km", state.getAltitudeKm()));
    }

    // Retranslates flight labels without a new ShuttleState (language/mode switch)
    private void refreshFlightLabelPrefixes() {
        if (skin == null) return;
        ExperienceMode mode = passenger.getExperienceMode();

        String rawPhase = skin.phaseLabel.getText();
        for (String p : new String[]{"Flight Phase: ", "Flugphase: "}) {
            if (rawPhase.startsWith(p)) { rawPhase = rawPhase.substring(p.length()); break; }
        }
        if (mode != ExperienceMode.ACTION) {
            skin.phaseLabel.setText(t("Flight Phase: ", "Flugphase: ") + rawPhase);
        }

        String elapsed = skin.elapsedLabel.getText();
        for (String p : new String[]{"Elapsed: ", "Vergangen: "}) {
            if (elapsed.startsWith(p)) { elapsed = elapsed.substring(p.length()); break; }
        }
        skin.elapsedLabel.setText(t("Elapsed: ", "Vergangen: ") + elapsed);

        String remaining = skin.timeLeftLabel.getText();
        for (String p : new String[]{"Remaining: ", "Verbleibend: "}) {
            if (remaining.startsWith(p)) { remaining = remaining.substring(p.length()); break; }
        }
        skin.timeLeftLabel.setText(t("Remaining: ", "Verbleibend: ") + remaining);

        if (lastAltitudeKm > 0) {
            skin.kmLeftLabel.setText(String.format(t("Altitude: ", "Höhe: ") + "%.0f km", lastAltitudeKm));
        }
    }

    private void refreshTelemetryLabels() {
        if (skin == null) return;
        ExperienceMode mode = passenger.getExperienceMode();
        if (mode == ExperienceMode.ACTION) {
            skin.oxygenLabel.setText(fmt(lastOxygen,       t("O2 LEVEL", "O2 STAND"),  "%%"));
            skin.altitudeLabel.setText(fmt(lastAltitudeKm, t("Altitude",  "Höhe"),      " km"));
            skin.velocityLabel.setText(fmt(lastVelocityKmph, t("SPEED",   "TEMPO"),     " km/h"));
            skin.cabinTempLabel.setText(fmt(lastCabinTemp,  "TEMP",                     "°C"));
        } else if (mode == ExperienceMode.RELAXED) {
            skin.oxygenLabel.setText(fmt(lastOxygen,       t("Oxygen",     "Sauerstoff"),       "%%"));
            skin.altitudeLabel.setText(fmt(lastAltitudeKm, t("Altitude",   "Höhe"),             " km"));
            skin.velocityLabel.setText(fmt(lastVelocityKmph, t("Velocity", "Geschwindigkeit"),  " km/h"));
            skin.cabinTempLabel.setText(fmt(lastCabinTemp,  t("Cabin Temp","Kabinentemp"),      "°C"));
        } else {
            skin.oxygenLabel.setText(fmt(lastOxygen,       t("Oxygen Level","Sauerstoff"),      "%%"));
            skin.altitudeLabel.setText(fmt(lastAltitudeKm, t("Altitude",    "Höhe"),            " km"));
            skin.velocityLabel.setText(fmt(lastVelocityKmph, t("Velocity",  "Geschwindigkeit"), " km/h"));
            skin.cabinTempLabel.setText(fmt(lastCabinTemp,   t("Cabin Temp","Kabinentemp"),     "°C"));
        }
    }

    private String fmt(double value, String label, String unit) {
        if (value == 0) return "--\n" + label;
        String formatted = unit.contains("%")
                ? String.format("%.0f%%", value)
                : String.format("%.0f%s", value, unit);
        return formatted + "\n" + label;
    }

    private String formatPhase(String phase) {
        String lower = phase.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String formatTime(double seconds) {
        int totalSec = (int) seconds;
        return String.format("%d:%02d", totalSec / 60, totalSec % 60);
    }
}
