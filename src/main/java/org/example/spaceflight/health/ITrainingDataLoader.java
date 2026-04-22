package org.example.spaceflight.health;

import java.util.List;

public interface ITrainingDataLoader {

    /** Load all labelled training cases. Returns an empty list on failure. */
    List<TrainingCase> load();
}
