package org.example.spaceflight.health;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Gender;
import org.example.spaceflight.model.HealthStatus;

/** One labelled row from the training dataset. */
class TrainingCase {

    final double rawBpm, rawSpo2, rawSys, rawDias, rawRr;
    final AgeGroup ageGroup;
    final Gender gender;
    final ExperienceMode mode;
    final HealthStatus label;
    final double[] features = new double[5]; // filled after normalisation by KNN service

    TrainingCase(double bpm, double spo2, double sys, double dias, double rr,
                 AgeGroup ag, Gender g, ExperienceMode m, HealthStatus label) {
        this.rawBpm  = bpm;  this.rawSpo2 = spo2; this.rawSys  = sys;
        this.rawDias = dias; this.rawRr   = rr;
        this.ageGroup = ag;  this.gender  = g;     this.mode    = m;
        this.label   = label;
    }
}
