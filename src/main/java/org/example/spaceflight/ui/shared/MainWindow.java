package org.example.spaceflight.ui.shared;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** Root window container that holds the navigation bar and swaps the active content panel. */
public class MainWindow {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private final BorderPane root;
    private final NavigationBar navigationBar;
    private final Map<NavigationBar.Tab, Node> views = new HashMap<>();

    public MainWindow() {
        root = new BorderPane();
        navigationBar = new NavigationBar();
        root.setTop(navigationBar.getRoot());

        navigationBar.setOnTabSelected(this::showView);
    }

    public void registerView(NavigationBar.Tab tab, Node view) {
        views.put(tab, view);
    }

    public void showView(NavigationBar.Tab tab) {
        Node view = views.get(tab);
        if (view != null) {
            root.setCenter(view);
        } else {
            Label placeholder = new Label(tab.getLabel() + " - Coming soon");
            placeholder.setStyle("-fx-font-size: 20px; -fx-text-fill: #999;");
            BorderPane wrapper = new BorderPane(placeholder);
            wrapper.setStyle("-fx-padding: 40;");
            root.setCenter(wrapper);
        }
    }

    public void setCenter(Node node) {
        root.setCenter(node);
    }

    public Region getRoot() {
        return root;
    }

    public NavigationBar getNavigationBar() {
        return navigationBar;
    }
}
