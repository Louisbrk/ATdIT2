package org.example.spaceflight.simulation;

import org.example.spaceflight.model.SimulationConfig;

/** Controls the simulation tick loop — start, pause, resume, stop, and speed adjustment. */
public interface SimulationService {

    void start(SimulationConfig config);

    void pause();

    void resume();

    void stop();

    void setSpeed(double multiplier);

    boolean isRunning();

    boolean isPaused();

    void addTickListener(TickListener listener);

    @FunctionalInterface
    interface TickListener {
        void onTick(long tickCount);
    }
}
