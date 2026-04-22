package org.example.spaceflight.simulation;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Passenger;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public final class DefaultExperienceModeService implements ExperienceModeService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    @Override
    public void changeMode(Passenger passenger, ExperienceMode newMode) {
        ExperienceMode previous = passenger.getExperienceMode();
        passenger.setExperienceMode(newMode);
        log.info("Passenger " + passenger.getName() + " mode changed: " + previous + " -> " + newMode);
    }
}
