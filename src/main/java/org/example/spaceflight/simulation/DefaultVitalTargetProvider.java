package org.example.spaceflight.simulation;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.FlightPhase;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/**
 * Computes per-tick vital-sign targets based on flight phase, experience mode,
 * passenger personal baseline, and whether an emergency is active.
 *
 * Knows the physiological rules (e.g. ascent raises BPM by 20× a phase factor);
 * the generator handles the stochastic step mechanics.
 */
public class DefaultVitalTargetProvider implements IVitalTargetProvider {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    @Override
    public PhaseTarget buildTarget(PersonalProfile p, FlightPhase phase,
                                   ExperienceMode mode, boolean emergency) {
        PhaseTarget t = new PhaseTarget();

        // Base targets from personal profile
        t.bpmCenter  = p.baseBpm;
        t.spo2Center = p.baseSpO2;
        t.sysCenter  = p.baseSystolic;
        t.diasCenter = p.baseDiastolic;
        t.respCenter = p.baseResp;

        // --- Experience mode first: sets the physiological context for the whole flight ---
        switch (mode) {
            case RELAXED -> {
                // Calm, rested state – lower arousal across all vitals
                t.bpmCenter  -= 8;
                t.spo2Center += 0.3;  // relaxed breathing → slightly better oxygenation
                t.sysCenter  -= 6;
                t.diasCenter -= 4;
                t.respCenter -= 2;
                // Narrow ranges: relaxed passengers are more stable
                t.bpmRange  = 4;
                t.spo2Range = 0.5;
                t.sysRange  = 5;
                t.diasRange = 4;
                t.respRange = 1;
            }
            case NORMAL -> {
                // Standard ranges
                t.bpmRange  = 6;
                t.spo2Range = 0.7;
                t.sysRange  = 8;
                t.diasRange = 6;
                t.respRange = 2;
            }
            case ACTION -> {
                // High arousal / excitement – elevated and more variable
                t.bpmCenter  += 15;
                t.spo2Center -= 0.5;  // higher metabolism, slightly more O2 consumed
                t.sysCenter  += 12;
                t.diasCenter += 6;
                t.respCenter += 4;
                // Wider ranges: active passengers fluctuate more
                t.bpmRange  = 10;
                t.spo2Range = 1.0;
                t.sysRange  = 12;
                t.diasRange = 8;
                t.respRange = 3;
            }
        }

        // --- Phase modifiers on top of mode baseline ---
        switch (phase) {
            case PRE_FLIGHT -> {
                // Pre-launch nervousness, mode-scaled
                double nerveFactor = mode.getPhaseFactor(phase);
                t.bpmCenter  += 8  * nerveFactor;
                t.sysCenter  += 7  * nerveFactor;
                t.diasCenter += 3  * nerveFactor;
                t.respCenter += 2  * nerveFactor;
                t.spo2Center -= 0.3 * nerveFactor; // shallow breathing from nerves
            }
            case ASCENT -> {
                // G-forces + pressure change – strongest phase effect
                double ascentFactor = mode.getPhaseFactor(phase);
                t.bpmCenter  += 20 * ascentFactor;
                t.spo2Center -= 2.0 * ascentFactor;
                t.sysCenter  += 18 * ascentFactor;
                t.diasCenter += 10 * ascentFactor;
                t.respCenter += 6  * ascentFactor;
            }
            case ORBIT -> {
                // Microgravity adaptation – fluid shift, mild tachycardia
                t.bpmCenter  += 8;
                t.spo2Center -= 0.8;
                t.sysCenter  += 6;
                t.diasCenter -= 2;  // fluid shift can slightly lower diastolic
                t.respCenter += 2;
            }
            case DESCENT -> {
                // Re-entry stress – G-forces return, cabin pressure rises
                double descentFactor = mode.getPhaseFactor(phase);
                t.bpmCenter  += 18 * descentFactor;
                t.spo2Center -= 1.5 * descentFactor;
                t.sysCenter  += 15 * descentFactor;
                t.diasCenter += 8  * descentFactor;
                t.respCenter += 5  * descentFactor;
            }
            case LANDED -> {
                // Relief and rapid recovery – values drop below personal baseline briefly
                double reliefFactor = mode.getPhaseFactor(phase);
                t.bpmCenter  -= 5 * reliefFactor;
                t.sysCenter  -= 5 * reliefFactor;
                t.respCenter -= 2 * reliefFactor;
                t.spo2Center += 0.3 * reliefFactor; // deep breaths on landing
            }
        }

        // Emergency override – push targets well into critical territory
        if (emergency) {
            t.bpmCenter  = 138;
            t.spo2Center = 89.0;
            t.sysCenter  = 175;
            t.diasCenter = 108;
            t.respCenter = 28;
            t.bpmRange  = 10;
            t.spo2Range = 1.2;
            t.sysRange  = 12;
            t.diasRange = 8;
            t.respRange = 3;
            log.fine("Emergency target applied for phase=" + phase + " mode=" + mode);
        }

        return t;
    }
}
