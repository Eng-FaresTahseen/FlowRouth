package com.ft.view;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane {

    public void showScreen(Parent view) {
        setCenter(view);
    }

}