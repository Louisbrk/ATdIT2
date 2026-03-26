package com.spaceflight.support;

import com.spaceflight.support.app.ApplicationBootstrap;
import com.spaceflight.support.ui.MainDashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SpaceFlightSupportApplication extends Application {

    @Override
    public void start(Stage stage) {
        ApplicationBootstrap.DashboardContext context = new ApplicationBootstrap().bootstrap();
        MainDashboardView dashboardView = new MainDashboardView(
                context.passengerService(),
                context.incidentService()
        );

        Scene scene = dashboardView.createScene();
        stage.setTitle("In-Flight Passenger Support & Emergency Escalation");
        stage.setMinWidth(1440);
        stage.setMinHeight(900);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
