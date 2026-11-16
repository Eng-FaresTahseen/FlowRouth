package com.ft;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.ft.controller.MainController;
import com.ft.view.HomeView;
import com.ft.view.MainView;
import com.ft.view.RouthView;
import com.ft.view.SignalFlowView;

/**
 * JavaFX App
 */
public class App extends Application {

    private MainController mainController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        MainView mainView = new MainView();
        HomeView homeView = new HomeView();
        SignalFlowView signalFlowView = new SignalFlowView();
        RouthView routhView = new RouthView();

        mainController = new MainController(mainView, homeView, signalFlowView, routhView);
        mainController.showHomeScreen();

        Scene scene = new Scene(mainView, 1200 , 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        Image icon = new Image(getClass().getResourceAsStream("/icon.png"));

        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("FlowRouth");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}