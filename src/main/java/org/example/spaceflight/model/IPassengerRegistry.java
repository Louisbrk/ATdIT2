package org.example.spaceflight.model;

import java.util.List;

/** Provides access to the passenger manifest and crew. */
public interface IPassengerRegistry {

    Stewardess getStewardess();

    List<Passenger> getPassengers();

    List<Passenger> getAllPersons();
}
