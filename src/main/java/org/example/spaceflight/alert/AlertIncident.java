package org.example.spaceflight.alert;

import org.example.spaceflight.model.Passenger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** An active or resolved alert raised for a specific passenger during the flight. */
public class AlertIncident implements Incident {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String id;
    private final Passenger passenger;
    private final String reason;
    private final LocalDateTime timestamp;
    private boolean resolved;
    private String stewardessNote = "";

    public AlertIncident(String id, Passenger passenger, String reason) {
        this.id = id;
        this.passenger = passenger;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
        this.resolved = false;
    }

    public String getId() { return id; }
    public Passenger getPassenger() { return passenger; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getFormattedTime() { return timestamp.format(FMT); }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getStewardessNote() { return stewardessNote; }
    public void setStewardessNote(String note) { this.stewardessNote = note; }
}
