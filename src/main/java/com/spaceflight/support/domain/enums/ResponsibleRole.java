package com.spaceflight.support.domain.enums;

public enum ResponsibleRole {
    PASSENGER("Passenger"),
    ONBOARD_SUPPORT("Onboard Support"),
    BASE_STATION("Base Station"),
    SYSTEM("System");

    private final String displayName;

    ResponsibleRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
