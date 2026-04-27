package org.example.spaceflight.alert;

import org.example.spaceflight.model.Passenger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Manages active alert incidents and notifies registered listeners when alerts are raised or resolved. */
public class DefaultAlertService implements AlertService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final List<AlertIncident> incidents = new ArrayList<>();
    private final List<Consumer<AlertIncident>> onAlertRaisedListeners = new ArrayList<>();
    private final List<Consumer<AlertIncident>> onAlertResolvedListeners = new ArrayList<>();

    @Override
    public void raiseAlert(Passenger passenger, String reason) {
        AlertIncident incident = new AlertIncident(UUID.randomUUID().toString(), passenger, reason);
        incidents.add(incident);
        log.warning("Alert raised for " + passenger.getName() + ": " + reason);
        onAlertRaisedListeners.forEach(l -> l.accept(incident));
    }

    @Override
    public void resolveAlert(String alertId) {
        for (AlertIncident incident : incidents) {
            if (incident.getId().equals(alertId)) {
                incident.setResolved(true);
                log.info("Alert resolved for " + incident.getPassenger().getName());
                onAlertResolvedListeners.forEach(l -> l.accept(incident));
                return;
            }
        }
    }

    @Override
    public List<AlertIncident> getAlertsForPassenger(Passenger passenger) {
        List<AlertIncident> result = new ArrayList<>();
        for (AlertIncident incident : incidents) {
            if (incident.getPassenger().equals(passenger) && !incident.isResolved()) {
                result.add(incident);
            }
        }
        return result;
    }

    @Override
    public List<AlertIncident> getAllAlertsForPassenger(Passenger passenger) {
        List<AlertIncident> result = new ArrayList<>();
        for (AlertIncident incident : incidents) {
            if (incident.getPassenger().equals(passenger)) result.add(incident);
        }
        return result;
    }

    /** Registers a callback that fires whenever a new alert is raised. */
    public void setOnAlertRaised(Consumer<AlertIncident> handler) {
        onAlertRaisedListeners.add(handler);
    }

    /** Registers a callback that fires whenever an alert is resolved. */
    public void setOnAlertResolved(Consumer<AlertIncident> handler) {
        onAlertResolvedListeners.add(handler);
    }
}
