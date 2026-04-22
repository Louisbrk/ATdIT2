package org.example.spaceflight.health;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Gender;

import java.lang.invoke.MethodHandles;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Lookup table of population baselines for all demographic segments.
 *
 * 18 entries: 3 AgeGroups × 2 Genders × 3 ExperienceModes.
 *
 * Baseline values are derived from published clinical reference ranges
 * (AHA / ESC normal ranges, segmented by age and sex).
 * ExperienceMode shifts BPM and systolic targets to reflect expected
 * physiological arousal during the flight experience.
 */
public class VitalProfileTable implements IVitalProfileProvider {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    // SpO2 and systolic BP are the fastest-acting crisis indicators in aerospace medicine.
    private static final double W_SPO2  = 0.30;
    private static final double W_SYS   = 0.25;
    private static final double W_BPM   = 0.20;
    private static final double W_DIAS  = 0.15;
    private static final double W_RR    = 0.10;

    private final Map<String, Map<VitalType, VitalProfile>> table;

    public VitalProfileTable() {
        table = new HashMap<>();
        buildTable();
        log.info("VitalProfileTable ready: " + table.size() + " demographic profiles loaded");
    }

    /** Returns the baseline profile for the given demographics and mode. */
    public Map<VitalType, VitalProfile> lookup(int age, Gender gender, ExperienceMode mode) {
        String key = buildKey(AgeGroup.fromAge(age), gender, mode);
        Map<VitalType, VitalProfile> profile = table.get(key);
        if (profile == null) {
            log.warning("No profile found for key=" + key + " — falling back to MIDDLE/MALE/NORMAL");
            profile = table.get(buildKey(AgeGroup.MIDDLE, Gender.MALE, ExperienceMode.NORMAL));
        }
        return profile;
    }

    // -------------------------------------------------------------------------

    private void buildTable() {
        for (AgeGroup ag : AgeGroup.values()) {
            for (Gender g : Gender.values()) {
                for (ExperienceMode m : ExperienceMode.values()) {
                    table.put(buildKey(ag, g, m), buildProfile(ag, g, m));
                }
            }
        }
    }

    private Map<VitalType, VitalProfile> buildProfile(AgeGroup ag, Gender g, ExperienceMode mode) {

        // --- Base NORMAL-mode values by age and gender ---

        double bpmMean, sysMean, diasMean, spO2Mean, rrMean;
        double bpmSd, sysSd, diasSd, spO2Sd, rrSd;

        // SpO2 and RR have small gender differences; age matters more.
        switch (ag) {
            case YOUNG -> {
                spO2Mean = 98.0; spO2Sd = 1.5;
                rrMean   = (g == Gender.FEMALE) ? 16.0 : 15.0; rrSd = 4.0;
                if (g == Gender.MALE) {
                    bpmMean = 68; bpmSd = 14; sysMean = 118; sysSd = 13; diasMean = 76; diasSd = 10;
                } else {
                    bpmMean = 72; bpmSd = 14; sysMean = 112; sysSd = 13; diasMean = 72; diasSd = 10;
                }
            }
            case MIDDLE -> {
                spO2Mean = 97.5; spO2Sd = 1.5;
                rrMean   = (g == Gender.FEMALE) ? 16.0 : 15.0; rrSd = 4.0;
                if (g == Gender.MALE) {
                    bpmMean = 70; bpmSd = 15; sysMean = 125; sysSd = 15; diasMean = 82; diasSd = 11;
                } else {
                    bpmMean = 74; bpmSd = 15; sysMean = 118; sysSd = 15; diasMean = 78; diasSd = 11;
                }
            }
            default -> { // SENIOR
                spO2Mean = 97.0; spO2Sd = 1.8;
                rrMean   = (g == Gender.FEMALE) ? 17.0 : 16.0; rrSd = 4.0;
                if (g == Gender.MALE) {
                    bpmMean = 72; bpmSd = 16; sysMean = 135; sysSd = 18; diasMean = 86; diasSd = 12;
                } else {
                    bpmMean = 76; bpmSd = 16; sysMean = 128; sysSd = 18; diasMean = 82; diasSd = 12;
                }
            }
        }

        // --- Mode adjustment: shifts mean to reflect expected physiological state ---
        // ACTION shifts match DefaultVitalSignsGenerator ACTION mode offsets (+15 BPM, +12 systolic)
        // so z-scores are computed against the correct expected baseline for the experience.
        switch (mode) {
            case ACTION  -> { bpmMean += 15; sysMean += 12; diasMean += 6; }
            case RELAXED -> { bpmMean -= 8;  sysMean -= 6;  diasMean -= 4; }
            case NORMAL  -> { /* baseline */ }
        }

        Map<VitalType, VitalProfile> profile = new EnumMap<>(VitalType.class);
        profile.put(VitalType.BPM,          new VitalProfile(bpmMean,  bpmSd,  W_BPM));
        profile.put(VitalType.SPO2,         new VitalProfile(spO2Mean, spO2Sd, W_SPO2));
        profile.put(VitalType.SYSTOLIC_BP,  new VitalProfile(sysMean,  sysSd,  W_SYS));
        profile.put(VitalType.DIASTOLIC_BP, new VitalProfile(diasMean, diasSd, W_DIAS));
        profile.put(VitalType.RESP_RATE,    new VitalProfile(rrMean,   rrSd,   W_RR));
        return profile;
    }

    private static String buildKey(AgeGroup ag, Gender g, ExperienceMode m) {
        return ag.name() + "_" + g.name() + "_" + m.name();
    }

    // -------------------------------------------------------------------------

}
