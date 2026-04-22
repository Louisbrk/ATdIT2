package org.example.spaceflight.alert;

import org.example.spaceflight.model.Passenger;

import java.util.List;
import java.util.function.Consumer;

/** Service for raising and resolving passenger alert incidents during the flight. */
public interface AlertService {

    void raiseAlert(Passenger passenger, String reason);

    void resolveAlert(String alertId);

    List<AlertIncident> getActiveAlerts();

    List<AlertIncident> getAlertsForPassenger(Passenger passenger);

    List<AlertIncident> getAllAlertsForPassenger(Passenger passenger);

    void setOnAlertRaised(Consumer<AlertIncident> handler);

    void setOnAlertResolved(Consumer<AlertIncident> handler);
}
