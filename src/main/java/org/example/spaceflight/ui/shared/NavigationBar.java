package org.example.spaceflight.ui.shared;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Top navigation bar with tab buttons that switch the visible dashboard panel. */
public class NavigationBar {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    public enum Tab {
        BASE_STATION("Overview", "#2196F3"),
        AI_HEALTH("AI Health", "#4CAF50"),
        EMERGENCY_ALERT("Emergency Alert", "#F44336"),
        PSYCHOLOGICAL_SUPPORT("Psychological Support", "#9C27B0"),
        USER_SETTINGS("User / Settings", "#757575");

        private final String label;
        private final String color;

        Tab(String label, String color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public String getColor() { return color; }
    }

    private final HBox root;
    private Consumer<Tab> onTabSelected;

    public NavigationBar() {
        root = new HBox();
        root.setAlignment(Pos.CENTER);

        for (Tab tab : Tab.values()) {
            Button button = new Button(tab.getLabel());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMaxHeight(Double.MAX_VALUE);
            button.setStyle(buildButtonStyle(tab.getColor()));
            HBox.setHgrow(button, Priority.ALWAYS);

            button.setOnAction(e -> {
                log.info("Tab selected: " + tab.getLabel());
                if (onTabSelected != null) {
                    onTabSelected.accept(tab);
                }
            });

            root.getChildren().add(button);
        }

        root.setPrefHeight(45);
    }

    public HBox getRoot() {
        return root;
    }

    public void setOnTabSelected(Consumer<Tab> handler) {
        this.onTabSelected = handler;
    }

    private String buildButtonStyle(String color) {
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 0;" +
                "-fx-cursor: hand;";
    }
}
