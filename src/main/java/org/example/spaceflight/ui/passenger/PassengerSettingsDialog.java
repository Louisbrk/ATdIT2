package org.example.spaceflight.ui.passenger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/**
 * Settings dialog for a single passenger dashboard.
 * Manages volume (mock), brightness (live opacity), and language (EN/DE).
 */
/** Settings dialog for passengers to adjust language, brightness, and volume preferences. */
public class PassengerSettingsDialog {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    public enum Language { EN, DE }

    private final Region targetRoot;
    private final Runnable onLanguageChanged;

    private Language currentLanguage   = Language.EN;
    private double   currentBrightness = 1.0;
    private double   currentVolume     = 50.0;

    /**
     * @param targetRoot       the root node whose opacity is adjusted by the brightness slider
     * @param onLanguageChanged callback invoked after the language changes (so the dashboard can retranslate)
     */
    public PassengerSettingsDialog(Region targetRoot, Runnable onLanguageChanged) {
        this.targetRoot = targetRoot;
        this.onLanguageChanged = onLanguageChanged;
    }

    public Language getLanguage() { return currentLanguage; }

    /** Two-branch translation helper. */
    public String t(String en, String de) {
        return currentLanguage == Language.EN ? en : de;
    }

    /** Opens the non-blocking settings dialog. */
    public void show() {
        Stage dialog = new Stage();
        dialog.setTitle(t("Settings", "Einstellungen"));
        dialog.setResizable(false);

        // --- Volume (mock) ---
        Label volumeHeader = new Label(t("Volume", "Lautstärke"));
        volumeHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Slider volumeSlider = new Slider(0, 100, currentVolume);
        volumeSlider.setMajorTickUnit(25);
        volumeSlider.setBlockIncrement(10);
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);
        Label volumeVal = new Label(String.valueOf((int) currentVolume));
        volumeVal.setMinWidth(30);
        volumeSlider.valueProperty().addListener((obs, o, n) -> {
            currentVolume = n.doubleValue();
            volumeVal.setText(String.valueOf((int) currentVolume));
        });
        HBox volumeRow = new HBox(8, volumeSlider, volumeVal);
        volumeRow.setAlignment(Pos.CENTER_LEFT);

        // --- Brightness (live opacity) ---
        Label brightnessHeader = new Label(t("Brightness", "Helligkeit"));
        brightnessHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Slider brightnessSlider = new Slider(0.2, 1.0, currentBrightness);
        brightnessSlider.setMajorTickUnit(0.2);
        brightnessSlider.setBlockIncrement(0.1);
        HBox.setHgrow(brightnessSlider, Priority.ALWAYS);
        Label brightnessVal = new Label(String.format("%.0f%%", currentBrightness * 100));
        brightnessVal.setMinWidth(38);
        brightnessSlider.valueProperty().addListener((obs, o, n) -> {
            currentBrightness = n.doubleValue();
            targetRoot.setOpacity(currentBrightness);
            brightnessVal.setText(String.format("%.0f%%", currentBrightness * 100));
        });
        HBox brightnessRow = new HBox(8, brightnessSlider, brightnessVal);
        brightnessRow.setAlignment(Pos.CENTER_LEFT);

        // --- Language ---
        Label langHeader = new Label(t("Language", "Sprache"));
        langHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        ToggleGroup langGroup = new ToggleGroup();
        RadioButton enRadio = new RadioButton("English");
        enRadio.setToggleGroup(langGroup);
        enRadio.setUserData(Language.EN);
        RadioButton deRadio = new RadioButton("Deutsch");
        deRadio.setToggleGroup(langGroup);
        deRadio.setUserData(Language.DE);
        if (currentLanguage == Language.EN) enRadio.setSelected(true);
        else deRadio.setSelected(true);
        langGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null) {
                currentLanguage = (Language) n.getUserData();
                onLanguageChanged.run();
            }
        });
        HBox langRow = new HBox(20, enRadio, deRadio);
        langRow.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10,
                volumeHeader, volumeRow,
                brightnessHeader, brightnessRow,
                langHeader, langRow
        );
        content.setPadding(new Insets(20));
        content.setPrefWidth(320);

        dialog.setScene(new Scene(content));
        dialog.show();
        log.info("Settings dialog opened");
    }
}
