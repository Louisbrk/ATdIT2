package org.example.spaceflight.ui.passenger.theme;

/** Neutral blue-grey theme for NORMAL experience mode. */
public final class NormalTheme implements PassengerDashboardTheme {

    @Override
    public void applyTo(DashboardSkin s) {
        s.titleBar.setStyle("-fx-background-color: #78909C;");
        s.leftContent.setStyle("-fx-background-color: white;");
        s.sidebar.setStyle("-fx-background-color: #E3F2FD;");
        s.flightInfoBox.setStyle("-fx-background-color: #B3D9F2; -fx-background-radius: 8;");
        s.telemetryBar.setStyle("-fx-background-color: #B0BEC5; -fx-background-radius: 8;");
        s.mapRow.setStyle(null);

        String infoStyle = "-fx-font-size: 14px; -fx-font-weight: bold;";
        s.phaseLabel.setStyle(infoStyle);
        s.elapsedLabel.setStyle(infoStyle);
        s.timeLeftLabel.setStyle(infoStyle);
        s.kmLeftLabel.setStyle(infoStyle);

        s.alertButton.setStyle(
                "-fx-background-color: #F44336; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        s.psychButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-alignment: center;"
        );

        String telStyle =
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: #5C8A9E; -fx-background-radius: 6; " +
                "-fx-padding: 8 12; -fx-text-alignment: center;";
        for (var l : s.telemetryLabels) l.setStyle(telStyle);

        s.statusTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.statusScroll.setStyle("-fx-background: white; -fx-background-color: white;");
        s.statusMessages.setStyle(null);
        s.statusArea.setStyle(null);

        s.infoBox.setStyle("-fx-background-color: #ECEFF1; -fx-background-radius: 8;");
        s.passengerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.departureHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.departureLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        s.departureLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        s.arrivalHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.arrivalLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        s.arrivalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        s.modeTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        s.modeBox.setStyle(null);
        s.relaxRadio.setStyle("-fx-text-fill: #212121;");
        s.normalRadio.setStyle("-fx-text-fill: #212121;");
        s.actionRadio.setStyle("-fx-text-fill: #212121;");
    }
}
