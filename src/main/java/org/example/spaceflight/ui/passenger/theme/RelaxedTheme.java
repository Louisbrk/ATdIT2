package org.example.spaceflight.ui.passenger.theme;

/** Calm green theme for RELAXED experience mode. */
public final class RelaxedTheme implements PassengerDashboardTheme {

    @Override
    public void applyTo(DashboardSkin s) {
        s.titleBar.setStyle("-fx-background-color: #66BB6A;");
        s.leftContent.setStyle("-fx-background-color: #F1FBF5;");
        s.sidebar.setStyle("-fx-background-color: #E8F5E9;");
        s.flightInfoBox.setStyle("-fx-background-color: #C8E6C9; -fx-background-radius: 8;");
        s.telemetryBar.setStyle("-fx-background-color: #A5D6A7; -fx-background-radius: 8;");
        s.mapRow.setStyle(null);

        String infoStyle = "-fx-font-size: 13px; -fx-font-weight: normal; -fx-text-fill: #2E7D32;";
        s.phaseLabel.setStyle(infoStyle);
        s.elapsedLabel.setStyle(infoStyle);
        s.timeLeftLabel.setStyle(infoStyle);
        s.kmLeftLabel.setStyle(infoStyle);

        s.alertButton.setStyle(
                "-fx-background-color: #EF9A9A; -fx-text-fill: #B71C1C; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        s.psychButton.setStyle(
                "-fx-background-color: #A5D6A7; -fx-text-fill: #1B5E20; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-alignment: center;"
        );

        String telStyle =
                "-fx-font-size: 12px; -fx-font-weight: normal; -fx-text-fill: #1B5E20; " +
                "-fx-background-color: #81C784; -fx-background-radius: 6; " +
                "-fx-padding: 7 10; -fx-text-alignment: center;";
        for (var l : s.telemetryLabels) l.setStyle(telStyle);

        s.statusTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.statusScroll.setStyle("-fx-background: white; -fx-background-color: white;");
        s.statusMessages.setStyle(null);
        s.statusArea.setStyle(null);

        s.infoBox.setStyle("-fx-background-color: #DCEDC8; -fx-background-radius: 8;");
        s.passengerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.departureHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.departureLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        s.departureLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        s.arrivalHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.arrivalLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        s.arrivalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        s.modeTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.modeBox.setStyle(null);
        s.relaxRadio.setStyle("-fx-text-fill: #212121;");
        s.normalRadio.setStyle("-fx-text-fill: #212121;");
        s.actionRadio.setStyle("-fx-text-fill: #212121;");
    }
}
