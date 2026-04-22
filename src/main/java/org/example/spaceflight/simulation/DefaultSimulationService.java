package org.example.spaceflight.simulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.example.spaceflight.model.SimulationConfig;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** JavaFX Timeline-based simulation engine — drives the tick loop and notifies all registered listeners. */
public class DefaultSimulationService implements SimulationService {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final List<TickListener> listeners = new ArrayList<>();
    private Timeline timeline;
    private long tickCount;
    private boolean running;
    private boolean paused;
    private int baseIntervalMs = 500;

    @Override
    public void start(SimulationConfig config) {
        if (running) {
            log.warning("Simulation already running");
            return;
        }

        tickCount = 0;
        running = true;
        paused = false;
        baseIntervalMs = config.getTickIntervalMs();

        timeline = new Timeline(new KeyFrame(
                Duration.millis(config.getTickIntervalMs()),
                event -> {
                    tickCount++;
                    for (TickListener listener : listeners) {
                        listener.onTick(tickCount);
                    }
                }
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        log.info("Simulation started (tick interval: " + config.getTickIntervalMs() + "ms)");
    }

    @Override
    public void pause() {
        if (!running || paused) return;
        timeline.pause();
        paused = true;
        log.info("Simulation paused at tick " + tickCount);
    }

    @Override
    public void resume() {
        if (!running || !paused) return;
        timeline.play();
        paused = false;
        log.info("Simulation resumed at tick " + tickCount);
    }

    /** Adjusts the playback speed by rebuilding the timeline with a scaled tick interval. */
    @Override
    public void setSpeed(double multiplier) {
        if (!running || timeline == null) return;
        boolean wasPaused = paused;
        timeline.stop();

        int newInterval = (int) Math.max(50, baseIntervalMs / multiplier);
        timeline = new Timeline(new KeyFrame(
                Duration.millis(newInterval),
                event -> {
                    tickCount++;
                    for (TickListener listener : listeners) {
                        listener.onTick(tickCount);
                    }
                }
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        if (!wasPaused) timeline.play();
        log.info("Simulation speed set to " + multiplier + "x (interval: " + newInterval + "ms)");
    }

    @Override
    public void stop() {
        if (!running) return;
        timeline.stop();
        running = false;
        paused = false;
        log.info("Simulation stopped after " + tickCount + " ticks");
    }

    @Override
    public boolean isRunning() { return running; }

    @Override
    public boolean isPaused() { return paused; }

    @Override
    public void addTickListener(TickListener listener) {
        listeners.add(listener);
    }
}
