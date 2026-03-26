package com.spaceflight.support.domain.enums;

public enum PassengerTier {
    PREMIUM("Premium"),
    STANDARD("Standard");

    private final String displayName;

    PassengerTier(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
