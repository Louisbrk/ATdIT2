package org.example.spaceflight.ui.basestation;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import org.example.spaceflight.model.Passenger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Grid panel containing a PassengerCardView for every person on the flight. */
public class PassengerOverviewPanel {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final javafx.scene.layout.VBox root;
    private final List<PassengerCardView> cards = new ArrayList<>();

    public PassengerOverviewPanel(List<Passenger> passengers, Consumer<Passenger> onInfoClicked) {
        Label title = new Label("Passenger Overview");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        for (int i = 0; i < passengers.size(); i++) {
            PassengerCardView card = new PassengerCardView(passengers.get(i), onInfoClicked);
            cards.add(card);
            grid.add(card.getRoot(), i % 2, i / 2);
        }

        root = new javafx.scene.layout.VBox(10, title, grid);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 8;");
    }

    public Region getRoot() {
        return root;
    }

    public List<PassengerCardView> getCards() {
        return cards;
    }

    public PassengerCardView getCardForPassenger(Passenger passenger) {
        return cards.stream()
                .filter(c -> c.getPassenger().equals(passenger))
                .findFirst()
                .orElse(null);
    }
}
