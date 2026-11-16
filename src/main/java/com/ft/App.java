package com.ft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root,Color.BLACK);

        Image icon = new Image("icon.png");

        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("FlowRouth");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}