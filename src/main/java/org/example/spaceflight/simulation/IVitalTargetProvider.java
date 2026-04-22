package org.example.spaceflight.simulation;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.FlightPhase;

public interface IVitalTargetProvider {

    /**
     * Compute the vital-sign target (center + range per vital) for the given
     * passenger profile, flight phase, experience mode, and emergency state.
     */
    PhaseTarget buildTarget(PersonalProfile profile, FlightPhase phase,
                            ExperienceMode mode, boolean emergency);
}
