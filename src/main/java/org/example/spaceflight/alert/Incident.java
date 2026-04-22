package org.example.spaceflight.alert;

import org.example.spaceflight.model.Passenger;

/** Common interface for all incident types (alerts and psychological requests). */
public interface Incident {

    String getId();
    Passenger getPassenger();
    String getReason();
    String getFormattedTime();
    boolean isResolved();
    void setResolved(boolean resolved);
    String getStewardessNote();
    void setStewardessNote(String note);
}
