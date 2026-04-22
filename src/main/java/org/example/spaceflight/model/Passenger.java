package org.example.spaceflight.model;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/** Represents a space tourism passenger with personal data, health state, and experience mode. */
public class Passenger {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final String name;
    private final int age;
    private final Gender gender;
    private final String role;
    private ExperienceMode experienceMode;
    private VitalSigns vitalSigns;
    private HealthStatus healthStatus;
    private boolean manualOverride;

    public Passenger(String name, int age, Gender gender, String role) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.experienceMode = ExperienceMode.NORMAL;
        this.healthStatus = HealthStatus.GREEN;
        this.manualOverride = false;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public Gender getGender() { return gender; }
    public String getRole() { return role; }

    public ExperienceMode getExperienceMode() { return experienceMode; }
    public void setExperienceMode(ExperienceMode experienceMode) { this.experienceMode = experienceMode; }

    public VitalSigns getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(VitalSigns vitalSigns) { this.vitalSigns = vitalSigns; }

    public HealthStatus getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatus healthStatus) {
        if (this.healthStatus != healthStatus) {
            log.fine(name + " health: " + this.healthStatus + " -> " + healthStatus);
        }
        this.healthStatus = healthStatus;
    }

    public boolean isManualOverride() { return manualOverride; }
    public void setManualOverride(boolean manualOverride) {
        if (this.manualOverride != manualOverride) {
            log.info(name + " manual override: " + manualOverride);
        }
        this.manualOverride = manualOverride;
    }

    public boolean isCrewMember() { return false; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Passenger other)) return false;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
