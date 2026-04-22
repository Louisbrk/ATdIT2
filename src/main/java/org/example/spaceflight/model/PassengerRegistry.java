package org.example.spaceflight.model;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;

/** Holds the fixed passenger manifest and stewardess for the demo flight. */
public class PassengerRegistry implements IPassengerRegistry {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final Stewardess stewardess;
    private final List<Passenger> passengers;

    /** Initialises the fixed passenger manifest and crew for the demo flight. */
    public PassengerRegistry() {
        stewardess = new Stewardess("Anne Bright", 27, Gender.FEMALE);

        passengers = List.of(
                new Passenger("Jennifer Monroe", 35, Gender.FEMALE, "CEO"),
                new Passenger("Ben Cooper", 51, Gender.MALE, "Engineer"),
                new Passenger("Peter Mayer", 15, Gender.MALE, "Student"),
                new Passenger("Sarah Chen", 42, Gender.FEMALE, "Scientist"),
                new Passenger("Marcus Webb", 29, Gender.MALE, "Journalist"),
                new Passenger("Lisa Berger", 38, Gender.FEMALE, "Researcher")
        );

        // Set experience modes matching the drafts
        passengers.get(0).setExperienceMode(ExperienceMode.RELAXED);
        passengers.get(1).setExperienceMode(ExperienceMode.NORMAL);
        passengers.get(2).setExperienceMode(ExperienceMode.RELAXED);
        passengers.get(3).setExperienceMode(ExperienceMode.ACTION);
        passengers.get(4).setExperienceMode(ExperienceMode.NORMAL);
        passengers.get(5).setExperienceMode(ExperienceMode.ACTION);
        log.info("PassengerRegistry initialised: " + passengers.size() + " passengers + stewardess");
    }

    public Stewardess getStewardess() {
        return stewardess;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public List<Passenger> getAllPersons() {
        List<Passenger> all = new java.util.ArrayList<>();
        all.add(stewardess);
        all.addAll(passengers);
        return all;
    }
}
