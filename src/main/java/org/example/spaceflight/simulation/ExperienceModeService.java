package org.example.spaceflight.simulation;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Passenger;

/** Commands for changing a passenger's experience mode. */
public interface ExperienceModeService {
    void changeMode(Passenger passenger, ExperienceMode newMode);
}
