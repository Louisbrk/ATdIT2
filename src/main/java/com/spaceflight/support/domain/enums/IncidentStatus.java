package com.spaceflight.support.domain.enums;

public enum IncidentStatus {
    NEW("New"),
    ASSESSING("Assessing"),
    MONITORING("Monitoring"),
    ESCALATED("Escalated"),
    RESOLVED("Resolved");

    private final String displayName;

    IncidentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
