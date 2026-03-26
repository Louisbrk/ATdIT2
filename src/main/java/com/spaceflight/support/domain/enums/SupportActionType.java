package com.spaceflight.support.domain.enums;

public enum SupportActionType {
    REASSURANCE_PROTOCOL("Reassurance Protocol"),
    GUIDED_BREATHING("Guided Breathing"),
    MOTION_SICKNESS_PROTOCOL("Motion Sickness Protocol"),
    HYDRATION_PROMPT("Hydration Prompt"),
    MEDICAL_CHECKLIST("Medical Checklist");

    private final String displayName;

    SupportActionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
