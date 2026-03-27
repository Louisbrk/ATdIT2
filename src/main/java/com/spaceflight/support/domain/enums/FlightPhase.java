package com.spaceflight.support.domain.enums;

public enum FlightPhase {
    LAUNCH_LOCKDOWN(
            "Launch / strapped-in phase",
            false,
            "During launch all passengers and crew remain strapped in. The seat display, calming guidance, and base-station fallback stay available."
    ),
    ORBIT_SERVICE(
            "Orbit / cabin service phase",
            true,
            "During orbit onboard support can move through the cabin, assess incidents directly, and continue the normal workflow."
    ),
    LANDING_LOCKDOWN(
            "Landing / strapped-in phase",
            false,
            "During landing the cabin returns to a strapped-in state. Requests still remain visible, logged, and remotely supportable."
    );

    private final String displayName;
    private final boolean onboardMovementAllowed;
    private final String operationalNote;

    FlightPhase(String displayName, boolean onboardMovementAllowed, String operationalNote) {
        this.displayName = displayName;
        this.onboardMovementAllowed = onboardMovementAllowed;
        this.operationalNote = operationalNote;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOnboardMovementAllowed() {
        return onboardMovementAllowed;
    }

    public String getOperationalNote() {
        return operationalNote;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
