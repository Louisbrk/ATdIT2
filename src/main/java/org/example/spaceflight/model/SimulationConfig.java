package org.example.spaceflight.model;

import java.time.LocalTime;

/** Holds the user-defined settings that control how the simulation runs. */
public class SimulationConfig {

    private int emergencyPassengerCount;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private int tickIntervalMs;

    public SimulationConfig() {
        this.emergencyPassengerCount = 0;
        this.departureTime = LocalTime.of(7, 0);
        this.arrivalTime = LocalTime.of(7, 10);
        this.tickIntervalMs = 500;
    }

    public int getEmergencyPassengerCount() { return emergencyPassengerCount; }
    public void setEmergencyPassengerCount(int count) { this.emergencyPassengerCount = count; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getTickIntervalMs() { return tickIntervalMs; }
    public void setTickIntervalMs(int tickIntervalMs) { this.tickIntervalMs = tickIntervalMs; }
}
