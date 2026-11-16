package com.ft;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.ft.view.MainView;

/**
 * JavaFX App
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainView mainView = new MainView();
        Scene scene = new Scene(mainView, 1200 , 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        Image icon = new Image("icon.png");
        primaryStage.setFullScreen(true);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("FlowRouth");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}