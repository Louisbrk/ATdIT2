package org.example.spaceflight.ui.basestation;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.example.spaceflight.alert.AlertService;
import org.example.spaceflight.alert.PsychologicalSupportService;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.model.ShuttleState;
import org.example.spaceflight.ui.shared.MainWindow;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;

/** Base Station dashboard — shows flight telemetry, the passenger grid, and all active incidents. */
public class BaseStationView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final HBox root;
    private final FlightInfoPanel flightInfoPanel;
    private final PassengerOverviewPanel passengerOverviewPanel;
    private final MainWindow mainWindow;
    private final AlertService alertService;
    private PassengerDetailView activeDetailView;

    public BaseStationView(List<Passenger> passengers, MainWindow mainWindow,
                           AlertService alertService) {
        this.mainWindow = mainWindow;
        this.alertService = alertService;

        flightInfoPanel = new FlightInfoPanel();
        passengerOverviewPanel = new PassengerOverviewPanel(passengers, this::showPassengerDetail);

        HBox.setHgrow(flightInfoPanel.getRoot(), Priority.ALWAYS);
        HBox.setHgrow(passengerOverviewPanel.getRoot(), Priority.ALWAYS);

        root = new HBox(15, flightInfoPanel.getRoot(), passengerOverviewPanel.getRoot());
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        alertService.setOnAlertRaised(incident ->
                Platform.runLater(() -> setCardAlert(incident.getPassenger(), true)));
        alertService.setOnAlertResolved(incident ->
                Platform.runLater(() -> setCardAlert(incident.getPassenger(), false)));
    }

    public Region getRoot() {
        return root;
    }

    public FlightInfoPanel getFlightInfoPanel() {
        return flightInfoPanel;
    }

    public PassengerOverviewPanel getPassengerOverviewPanel() {
        return passengerOverviewPanel;
    }

    public void updateFlightInfo(ShuttleState state) {
        flightInfoPanel.update(state);
    }

    public void updatePassengerCards(List<Passenger> passengers) {
        List<PassengerCardView> cards = passengerOverviewPanel.getCards();
        for (int i = 0; i < cards.size() && i < passengers.size(); i++) {
            cards.get(i).updateAlertIndicator(passengers.get(i).getHealthStatus());
        }
    }

    public void updateDetailView() {
        if (activeDetailView != null) {
            activeDetailView.update();
        }
    }

    public void setOnEmergencyLanding(Runnable handler) {
        flightInfoPanel.setOnEmergencyLanding(handler);
    }

    private void showPassengerDetail(Passenger passenger) {
        activeDetailView = new PassengerDetailView(passenger, alertService, this::showOverview);
        activeDetailView.update();
        mainWindow.setCenter(activeDetailView.getRoot());
    }

    private void showOverview() {
        activeDetailView = null;
        mainWindow.setCenter(root);
    }

    public void subscribePsychService(PsychologicalSupportService psychService) {
        psychService.setOnRequestRaised(incident ->
                Platform.runLater(() -> setCardPsych(incident.getPassenger(), true)));
        psychService.setOnRequestResolved(incident ->
                Platform.runLater(() -> {
                    // Only clear purple if no active medical alert remains
                    boolean hasAlert = alertService.getAlertsForPassenger(incident.getPassenger())
                            .stream().anyMatch(a -> !a.isResolved());
                    if (!hasAlert) setCardPsych(incident.getPassenger(), false);
                }));
    }

    private void setCardPsych(Passenger passenger, boolean active) {
        PassengerCardView card = passengerOverviewPanel.getCardForPassenger(passenger);
        if (card != null) {
            card.setPsychActive(active);
        }
    }

    private void setCardAlert(Passenger passenger, boolean active) {
        PassengerCardView card = passengerOverviewPanel.getCardForPassenger(passenger);
        if (card != null) {
            card.setAlertActive(active);
        }
    }
}
