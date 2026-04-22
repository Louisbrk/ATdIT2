package org.example.spaceflight.alert;

import org.example.spaceflight.model.Passenger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** A psychological support request raised by a passenger during the flight. */
public class PsychologicalIncident implements Incident {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String id;
    private final Passenger passenger;
    private final String reason;
    private final PsychSeverity severity;
    private final LocalDateTime timestamp;
    private boolean resolved;
    private String stewardessNote = "";

    public PsychologicalIncident(String id, Passenger passenger, PsychSeverity severity, String reason) {
        this.id = id;
        this.passenger = passenger;
        this.severity = severity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
        this.resolved = false;
    }

    @Override public String getId() { return id; }
    @Override public Passenger getPassenger() { return passenger; }
    @Override public String getReason() { return reason; }
    @Override public String getFormattedTime() { return timestamp.format(FMT); }
    @Override public boolean isResolved() { return resolved; }
    @Override public void setResolved(boolean resolved) { this.resolved = resolved; }
    @Override public String getStewardessNote() { return stewardessNote; }
    @Override public void setStewardessNote(String note) { this.stewardessNote = note; }

    public PsychSeverity getSeverity() { return severity; }
}
