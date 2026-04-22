package org.example.spaceflight.ui.passenger.theme;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Holds references to all style-affected and data-bound nodes in PassengerDashboardView. */
public final class DashboardSkin {

    // Layout containers
    public final VBox root;
    public final HBox titleBar;
    public final VBox leftContent;
    public final VBox sidebar;
    public final VBox flightInfoBox;
    public final HBox telemetryBar;
    public final HBox mapRow;

    // Title
    public final Label titleLabel;

    // Flight info labels
    public final Label phaseLabel;
    public final Label elapsedLabel;
    public final Label timeLeftLabel;
    public final Label kmLeftLabel;

    // Telemetry labels (array for bulk styling; also individually accessible)
    public final Label oxygenLabel;
    public final Label altitudeLabel;
    public final Label velocityLabel;
    public final Label cabinTempLabel;
    public final Label[] telemetryLabels;

    // Action buttons
    public final Button alertButton;
    public final Button psychButton;

    // Status area
    public final Label statusTitle;
    public final ScrollPane statusScroll;
    public final VBox statusMessages;
    public final VBox statusArea;

    // Sidebar info box
    public final VBox infoBox;
    public final Label passengerTitle;
    public final Label departureHeader;
    public final Label departureLocation;
    public final Label departureLabel;
    public final Label arrivalHeader;
    public final Label arrivalLocation;
    public final Label arrivalLabel;

    // Mode section
    public final Label modeTitle;
    public final VBox modeBox;
    public final RadioButton relaxRadio;
    public final RadioButton normalRadio;
    public final RadioButton actionRadio;
    public final RadioButton[] modeRadios;

    private DashboardSkin(Builder b) {
        root          = b.root;
        titleBar      = b.titleBar;
        leftContent   = b.leftContent;
        sidebar       = b.sidebar;
        flightInfoBox = b.flightInfoBox;
        telemetryBar  = b.telemetryBar;
        mapRow        = b.mapRow;

        titleLabel    = b.titleLabel;

        phaseLabel    = b.phaseLabel;
        elapsedLabel  = b.elapsedLabel;
        timeLeftLabel = b.timeLeftLabel;
        kmLeftLabel   = b.kmLeftLabel;

        oxygenLabel    = b.oxygenLabel;
        altitudeLabel  = b.altitudeLabel;
        velocityLabel  = b.velocityLabel;
        cabinTempLabel = b.cabinTempLabel;
        telemetryLabels = new Label[]{oxygenLabel, altitudeLabel, velocityLabel, cabinTempLabel};

        alertButton  = b.alertButton;
        psychButton  = b.psychButton;

        statusTitle    = b.statusTitle;
        statusScroll   = b.statusScroll;
        statusMessages = b.statusMessages;
        statusArea     = b.statusArea;

        infoBox           = b.infoBox;
        passengerTitle    = b.passengerTitle;
        departureHeader   = b.departureHeader;
        departureLocation = b.departureLocation;
        departureLabel    = b.departureLabel;
        arrivalHeader     = b.arrivalHeader;
        arrivalLocation   = b.arrivalLocation;
        arrivalLabel      = b.arrivalLabel;

        modeTitle   = b.modeTitle;
        modeBox     = b.modeBox;
        relaxRadio  = b.relaxRadio;
        normalRadio = b.normalRadio;
        actionRadio = b.actionRadio;
        modeRadios  = new RadioButton[]{relaxRadio, normalRadio, actionRadio};
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        VBox root, leftContent, sidebar, flightInfoBox, statusMessages, statusArea, infoBox, modeBox;
        HBox titleBar, telemetryBar, mapRow;
        Label titleLabel, phaseLabel, elapsedLabel, timeLeftLabel, kmLeftLabel;
        Label oxygenLabel, altitudeLabel, velocityLabel, cabinTempLabel;
        Label statusTitle, passengerTitle;
        Label departureHeader, departureLocation, departureLabel;
        Label arrivalHeader, arrivalLocation, arrivalLabel;
        Label modeTitle;
        Button alertButton, psychButton;
        ScrollPane statusScroll;
        RadioButton relaxRadio, normalRadio, actionRadio;

        public Builder root(VBox v)              { root = v;              return this; }
        public Builder titleBar(HBox v)          { titleBar = v;          return this; }
        public Builder leftContent(VBox v)       { leftContent = v;       return this; }
        public Builder sidebar(VBox v)           { sidebar = v;           return this; }
        public Builder flightInfoBox(VBox v)     { flightInfoBox = v;     return this; }
        public Builder telemetryBar(HBox v)      { telemetryBar = v;      return this; }
        public Builder mapRow(HBox v)            { mapRow = v;            return this; }
        public Builder titleLabel(Label v)       { titleLabel = v;        return this; }
        public Builder phaseLabel(Label v)       { phaseLabel = v;        return this; }
        public Builder elapsedLabel(Label v)     { elapsedLabel = v;      return this; }
        public Builder timeLeftLabel(Label v)    { timeLeftLabel = v;     return this; }
        public Builder kmLeftLabel(Label v)      { kmLeftLabel = v;       return this; }
        public Builder oxygenLabel(Label v)      { oxygenLabel = v;       return this; }
        public Builder altitudeLabel(Label v)    { altitudeLabel = v;     return this; }
        public Builder velocityLabel(Label v)    { velocityLabel = v;     return this; }
        public Builder cabinTempLabel(Label v)   { cabinTempLabel = v;    return this; }
        public Builder alertButton(Button v)     { alertButton = v;       return this; }
        public Builder psychButton(Button v)     { psychButton = v;       return this; }
        public Builder statusTitle(Label v)      { statusTitle = v;       return this; }
        public Builder statusScroll(ScrollPane v){ statusScroll = v;      return this; }
        public Builder statusMessages(VBox v)    { statusMessages = v;    return this; }
        public Builder statusArea(VBox v)        { statusArea = v;        return this; }
        public Builder infoBox(VBox v)           { infoBox = v;           return this; }
        public Builder passengerTitle(Label v)   { passengerTitle = v;    return this; }
        public Builder departureHeader(Label v)  { departureHeader = v;   return this; }
        public Builder departureLocation(Label v){ departureLocation = v; return this; }
        public Builder departureLabel(Label v)   { departureLabel = v;    return this; }
        public Builder arrivalHeader(Label v)    { arrivalHeader = v;     return this; }
        public Builder arrivalLocation(Label v)  { arrivalLocation = v;   return this; }
        public Builder arrivalLabel(Label v)     { arrivalLabel = v;      return this; }
        public Builder modeTitle(Label v)        { modeTitle = v;         return this; }
        public Builder modeBox(VBox v)           { modeBox = v;           return this; }
        public Builder relaxRadio(RadioButton v) { relaxRadio = v;        return this; }
        public Builder normalRadio(RadioButton v){ normalRadio = v;       return this; }
        public Builder actionRadio(RadioButton v){ actionRadio = v;       return this; }

        public DashboardSkin build() { return new DashboardSkin(this); }
    }
}
