package org.example.spaceflight.model;

/** Mutable snapshot of all shuttle telemetry values for a single simulation tick. */
public class ShuttleState {

    private double fuelPercent;
    private double distanceKm;
    private double altitudeKm;
    private double velocityKmph;
    private double oxygenPercent;
    private double cabinTemperature;
    private double routeProgress;
    private FlightPhase flightPhase;
    private boolean emergencyLanding;
    private double elapsedSeconds;
    private double totalFlightSeconds;

    public ShuttleState() {
        this.fuelPercent = 100.0;
        this.distanceKm = 0.0;
        this.altitudeKm = 0.0;
        this.velocityKmph = 0.0;
        this.oxygenPercent = 100.0;
        this.cabinTemperature = 21.0;
        this.routeProgress = 0.0;
        this.flightPhase = FlightPhase.PRE_FLIGHT;
        this.emergencyLanding = false;
        this.elapsedSeconds = 0.0;
        this.totalFlightSeconds = 3000.0; // 50 minutes simulated flight time
    }

    public double getFuelPercent() { return fuelPercent; }
    public void setFuelPercent(double fuelPercent) { this.fuelPercent = fuelPercent; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getAltitudeKm() { return altitudeKm; }
    public void setAltitudeKm(double altitudeKm) { this.altitudeKm = altitudeKm; }

    public double getVelocityKmph() { return velocityKmph; }
    public void setVelocityKmph(double velocityKmph) { this.velocityKmph = velocityKmph; }

    public double getOxygenPercent() { return oxygenPercent; }
    public void setOxygenPercent(double oxygenPercent) { this.oxygenPercent = oxygenPercent; }

    public double getCabinTemperature() { return cabinTemperature; }
    public void setCabinTemperature(double cabinTemperature) { this.cabinTemperature = cabinTemperature; }

    public double getRouteProgress() { return routeProgress; }
    public void setRouteProgress(double routeProgress) { this.routeProgress = routeProgress; }

    public FlightPhase getFlightPhase() { return flightPhase; }
    public void setFlightPhase(FlightPhase flightPhase) { this.flightPhase = flightPhase; }

    public boolean isEmergencyLanding() { return emergencyLanding; }
    public void setEmergencyLanding(boolean emergencyLanding) { this.emergencyLanding = emergencyLanding; }

    public double getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(double elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }

    public double getTotalFlightSeconds() { return totalFlightSeconds; }
    public void setTotalFlightSeconds(double totalFlightSeconds) { this.totalFlightSeconds = totalFlightSeconds; }
}
