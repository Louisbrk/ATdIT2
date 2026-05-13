package org.example.spaceflight.app;

import javafx.application.Application;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;


public class Launcher {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    public static void main(String[] args) {
        log.info("Starting SpaceFlight application");
        Application.launch(SpaceFlightApp.class, args);
    }
}
