package org.example.spaceflight.alert;

import org.example.spaceflight.model.Passenger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Manages psychological support requests and notifies listeners when requests are raised or resolved. */
public class DefaultPsychologicalSupportService implements PsychologicalSupportService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final List<PsychologicalIncident> incidents = new ArrayList<>();
    private final List<Consumer<PsychologicalIncident>> onRaisedListeners = new ArrayList<>();
    private final List<Consumer<PsychologicalIncident>> onResolvedListeners = new ArrayList<>();

    @Override
    public void raiseRequest(Passenger passenger, PsychSeverity severity, String message) {
        PsychologicalIncident incident = new PsychologicalIncident(
                UUID.randomUUID().toString(), passenger, severity, message);
        incidents.add(incident);
        log.warning("Psychological support request raised for " + passenger.getName()
                + " [" + severity + "]: " + message);
        onRaisedListeners.forEach(l -> l.accept(incident));
    }

    @Override
    public void resolveRequest(String id) {
        for (PsychologicalIncident incident : incidents) {
            if (incident.getId().equals(id)) {
                incident.setResolved(true);
                log.info("Psychological support resolved for " + incident.getPassenger().getName());
                onResolvedListeners.forEach(l -> l.accept(incident));
                return;
            }
        }
    }

    @Override
    public List<PsychologicalIncident> getActiveRequests() {
        List<PsychologicalIncident> active = new ArrayList<>();
        for (PsychologicalIncident incident : incidents) {
            if (!incident.isResolved()) active.add(incident);
        }
        return active;
    }

    @Override
    public void setOnRequestRaised(Consumer<PsychologicalIncident> handler) {
        onRaisedListeners.add(handler);
    }

    @Override
    public void setOnRequestResolved(Consumer<PsychologicalIncident> handler) {
        onResolvedListeners.add(handler);
    }
}
