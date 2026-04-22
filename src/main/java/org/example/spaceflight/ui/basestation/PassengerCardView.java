package org.example.spaceflight.ui.basestation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.model.Passenger;
import org.example.spaceflight.ui.shared.UIColors;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Compact card showing one passenger's health status, experience mode, and any active alerts. */
public class PassengerCardView {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private static final String COLOR_BLUE   = "#42A5F5";
    private static final String COLOR_RED    = UIColors.ALERT_RED;
    private static final String COLOR_PURPLE = UIColors.PURPLE;

    private final VBox root;
    private final Circle alertIndicator;
    private final Passenger passenger;
    private boolean alertActive = false;
    private boolean psychActive = false;

    public PassengerCardView(Passenger passenger, Consumer<Passenger> onInfoClicked) {
        this.passenger = passenger;

        Label nameLabel = new Label(passenger.getName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        nameLabel.setWrapText(true);

        Label roleLabel = new Label(passenger.getRole());
        roleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ddd;");

        alertIndicator = new Circle(6);
        alertIndicator.setFill(Color.TRANSPARENT);

        Button infoButton = new Button("Info");
        infoButton.setStyle(
                "-fx-background-color: #B0BEC5; -fx-text-fill: #333; " +
                "-fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 4;"
        );
        infoButton.setOnAction(e -> {
            log.info("Info clicked for " + passenger.getName());
            onInfoClicked.accept(passenger);
        });

        root = new VBox(8, alertIndicator, nameLabel, roleLabel, infoButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(12));
        root.setPrefSize(140, 130);
        root.setStyle("-fx-background-color: " + COLOR_BLUE + "; -fx-background-radius: 10;");
    }

    public Region getRoot() {
        return root;
    }

    public void updateAlertIndicator(HealthStatus status) {
        // Only recolor the dot; never touch background when an alert or psych is active
        if (!alertActive && !psychActive) {
            Color color = switch (status) {
                case GREEN -> Color.LIMEGREEN;
                case YELLOW -> Color.YELLOW;
                case RED -> Color.RED;
            };
            alertIndicator.setFill(color);
        }
    }

    /** Called when a medical alert is raised or resolved for this passenger. */
    public void setAlertActive(boolean active) {
        this.alertActive = active;
        recomputeCardColor();
    }

    /** Called when a psychological support request is raised or resolved for this passenger. */
    public void setPsychActive(boolean active) {
        this.psychActive = active;
        recomputeCardColor();
    }

    public Passenger getPassenger() {
        return passenger;
    }

    private void recomputeCardColor() {
        String bg;
        Color dot;
        if (alertActive) {
            bg = COLOR_RED;
            dot = Color.RED;
        } else if (psychActive) {
            bg = COLOR_PURPLE;
            dot = Color.VIOLET;
        } else {
            bg = COLOR_BLUE;
            dot = Color.LIMEGREEN;
        }
        root.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;");
        alertIndicator.setFill(dot);
    }
}
