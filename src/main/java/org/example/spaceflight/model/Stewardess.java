package org.example.spaceflight.model;

/** Crew member (flight stewardess) — extends Passenger with crew-member identity. */
public class Stewardess extends Passenger {

    public Stewardess(String name, int age, Gender gender) {
        super(name, age, gender, "Stewardess");
    }

    @Override
    public boolean isCrewMember() { return true; }
}
