package org.example.spaceflight.health;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Gender;

import java.util.Map;

/** Provides the population vital-sign baselines for a given demographic segment. */
public interface IVitalProfileProvider {

    Map<VitalType, VitalProfile> lookup(int age, Gender gender, ExperienceMode mode);
}
