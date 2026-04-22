package org.example.spaceflight.model;

/**
 * Immutable data snapshot of a single passenger at one simulation tick.
 * Decouples views from live Passenger objects — designed so that this
 * class can be serialised to JSON later when moving to a client-server setup.
 */
public class PassengerSnapshot {

    private final String id;
    private final String name;
    private final int age;
    private final Gender gender;
    private final boolean crewMember;
    private final ExperienceMode experienceMode;
    private final VitalSigns vitalSigns;
    private final HealthStatus healthStatus;
    private final boolean manualOverride;

    public PassengerSnapshot(Passenger p) {
        this.id             = p.getName();
        this.name           = p.getName();
        this.age            = p.getAge();
        this.gender         = p.getGender();
        this.crewMember     = p.isCrewMember();
        this.experienceMode = p.getExperienceMode();
        this.vitalSigns     = p.getVitalSigns();
        this.healthStatus   = p.getHealthStatus();
        this.manualOverride = p.isManualOverride();
    }

    public String getId()                    { return id; }
    public String getName()                  { return name; }
    public int getAge()                      { return age; }
    public Gender getGender()                { return gender; }
    public boolean isCrewMember()            { return crewMember; }
    public ExperienceMode getExperienceMode() { return experienceMode; }
    public VitalSigns getVitalSigns()        { return vitalSigns; }
    public HealthStatus getHealthStatus()    { return healthStatus; }
    public boolean isManualOverride()        { return manualOverride; }
}
