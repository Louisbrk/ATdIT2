package com.spaceflight.support.domain.model;

import com.spaceflight.support.domain.enums.PassengerTier;

import java.util.Objects;
import java.util.UUID;

public class Passenger {

    private final UUID id;
    private final String fullName;
    private final PassengerTier tier;
    private final String seatLabel;
    private final String profileNotes;

    public Passenger(UUID id, String fullName, PassengerTier tier, String seatLabel, String profileNotes) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.fullName = requireText(fullName, "fullName");
        this.tier = Objects.requireNonNull(tier, "tier must not be null");
        this.seatLabel = requireText(seatLabel, "seatLabel");
        this.profileNotes = profileNotes == null ? "" : profileNotes.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public PassengerTier getTier() {
        return tier;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public String getProfileNotes() {
        return profileNotes;
    }

    @Override
    public String toString() {
        return fullName + " (" + tier.getDisplayName() + ", seat " + seatLabel + ")";
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
