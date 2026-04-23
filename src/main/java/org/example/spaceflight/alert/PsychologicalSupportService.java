package org.example.spaceflight.alert;

import org.example.spaceflight.model.Passenger;

import java.util.List;
import java.util.function.Consumer;

/** Service for raising and resolving psychological support requests from passengers. */
public interface PsychologicalSupportService {

    void raiseRequest(Passenger passenger, PsychSeverity severity, String message);

    void resolveRequest(String id);

    /** Currently not needed, but implemented as preparation for Client-Server Architecture. */
    List<PsychologicalIncident> getActiveRequests();

    void setOnRequestRaised(Consumer<PsychologicalIncident> handler);

    void setOnRequestResolved(Consumer<PsychologicalIncident> handler);
}
