package org.example.spaceflight.ui.passenger.theme;

/** Dark high-contrast theme for ACTION experience mode. */
public final class ActionTheme implements PassengerDashboardTheme {

    @Override
    public void applyTo(DashboardSkin s) {
        s.titleBar.setStyle("-fx-background-color: #E65100;");
        s.leftContent.setStyle("-fx-background-color: #1A1A2E;");
        s.sidebar.setStyle("-fx-background-color: #16213E;");
        s.flightInfoBox.setStyle("-fx-background-color: #0F3460; -fx-background-radius: 8;");
        s.telemetryBar.setStyle("-fx-background-color: #0F3460; -fx-background-radius: 8;");
        s.mapRow.setStyle("-fx-background-color: #1A1A2E;");

        s.phaseLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FF6E40;");
        s.elapsedLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #CFD8DC;");
        s.timeLeftLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #CFD8DC;");
        s.kmLeftLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF6E40;");

        s.alertButton.setStyle(
                "-fx-background-color: #FF1744; -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-padding: 18; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        s.psychButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-alignment: center;"
        );

        String telStyle =
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FF6E40; " +
                "-fx-background-color: #1E3A5F; -fx-background-radius: 6; " +
                "-fx-padding: 10 14; -fx-text-alignment: center;";
        for (var l : s.telemetryLabels) l.setStyle(telStyle);

        s.statusTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        s.statusScroll.setStyle("-fx-background: #0F3460; -fx-background-color: #0F3460;");
        s.statusMessages.setStyle("-fx-background-color: #0F3460;");
        s.statusArea.setStyle("-fx-background-color: #1A1A2E;");

        s.infoBox.setStyle("-fx-background-color: #0F3460; -fx-background-radius: 8;");
        s.passengerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        s.departureHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        s.departureLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #90CAF9;");
        s.departureLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        s.arrivalHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        s.arrivalLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #90CAF9;");
        s.arrivalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        s.modeTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        s.modeBox.setStyle("-fx-background-color: transparent;");
        s.relaxRadio.setStyle("-fx-text-fill: white;");
        s.normalRadio.setStyle("-fx-text-fill: white;");
        s.actionRadio.setStyle("-fx-text-fill: white;");
    }
}
