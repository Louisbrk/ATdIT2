package org.example.spaceflight.model;

public enum ExperienceMode {

    RELAXED {
        @Override
        public double getPhaseFactor(FlightPhase phase) {
            return switch (phase) {
                case PRE_FLIGHT -> 0.4;
                case ASCENT     -> 0.7;
                case ORBIT      -> 1.0;
                case DESCENT    -> 0.8;
                case LANDED     -> 1.2;
            };
        }
    },

    NORMAL {
        @Override
        public double getPhaseFactor(FlightPhase phase) {
            return 1.0;
        }
    },

    ACTION {
        @Override
        public double getPhaseFactor(FlightPhase phase) {
            return switch (phase) {
                case PRE_FLIGHT -> 1.5;
                case ASCENT     -> 1.2;
                case ORBIT      -> 1.0;
                case DESCENT    -> 1.2;
                case LANDED     -> 0.7;
            };
        }
    };

    /** Returns how strongly this mode amplifies the phase's physiological effect. */
    public abstract double getPhaseFactor(FlightPhase phase);
}
