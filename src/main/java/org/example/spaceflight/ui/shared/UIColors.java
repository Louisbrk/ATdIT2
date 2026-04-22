package org.example.spaceflight.ui.shared;

/**
 * Centralized color constants used across multiple UI components.
 * Only colors that appear in 2+ files are listed here.
 */
public final class UIColors {

    private UIColors() {}

    // --- Health status colors ---
    public static final String HEALTH_GREEN  = "#388E3C";
    public static final String HEALTH_YELLOW = "#F9A825";
    public static final String HEALTH_RED    = "#C62828";

    // --- Health status colors (lighter variants for text/warnings) ---
    public static final String HEALTH_YELLOW_DARK = "#F57F17";

    // --- General UI action colors ---
    public static final String ACTION_GREEN  = "#4CAF50";
    public static final String ACTION_RED    = "#F44336";
    public static final String ALERT_RED     = "#E53935";
    public static final String EMERGENCY_RED = "#FF1744";

    // --- Text colors ---
    public static final String TEXT_PRIMARY = "#212121";
    public static final String TEXT_MUTED   = "#607D8B";

    // --- Background colors ---
    public static final String BG_LIGHT_BLUE = "#E3F2FD";
    public static final String BG_LIGHT_GRAY = "#ECEFF1";

    // --- Accent ---
    public static final String PURPLE = "#7B1FA2";
}
