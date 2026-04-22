package org.example.spaceflight.health;

/** Population baseline for one vital sign in one demographic segment. Immutable. */
public final class VitalProfile {

    /** Expected population mean for this vital in this segment. */
    final double mean;
    /** Population standard deviation — controls how quickly z-score rises. */
    final double stdDev;
    /** Importance weight used in the composite health score (all weights sum to 1.0). */
    final double weight;

    public VitalProfile(double mean, double stdDev, double weight) {
        this.mean   = mean;
        this.stdDev = stdDev;
        this.weight = weight;
    }
}
