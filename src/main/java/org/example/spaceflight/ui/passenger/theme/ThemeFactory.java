package org.example.spaceflight.ui.passenger.theme;

import org.example.spaceflight.model.ExperienceMode;

/** Returns the correct theme implementation for a given ExperienceMode. */
public final class ThemeFactory {

    private ThemeFactory() {}

    public static PassengerDashboardTheme forMode(ExperienceMode mode) {
        return switch (mode) {
            case RELAXED -> new RelaxedTheme();
            case NORMAL  -> new NormalTheme();
            case ACTION  -> new ActionTheme();
        };
    }
}
