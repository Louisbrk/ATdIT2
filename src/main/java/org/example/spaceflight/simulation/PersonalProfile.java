package org.example.spaceflight.simulation;

import org.example.spaceflight.model.Passenger;

import java.util.Random;

/** Passenger-specific vital-sign baseline, derived once from demographics at construction. */
class PersonalProfile {

    final int    baseBpm;
    final double baseSpO2;
    final int    baseSystolic;
    final int    baseDiastolic;
    final int    baseResp;

    PersonalProfile(Passenger p) {
        Random r = new Random(p.getName().hashCode()); // deterministic per passenger
        int ageFactor = Math.max(0, p.getAge() - 40) / 5;
        baseBpm       = 62 + r.nextInt(16);                  // 62–77
        baseSpO2      = 97.0 + r.nextDouble() * 1.5;         // 97.0–98.5
        baseSystolic  = 110 + ageFactor * 2 + r.nextInt(15); // 110–130 adjusted for age
        baseDiastolic = 68  + ageFactor     + r.nextInt(10); // 68–78 adjusted for age
        baseResp      = 13  + r.nextInt(5);                  // 13–17
    }
}
