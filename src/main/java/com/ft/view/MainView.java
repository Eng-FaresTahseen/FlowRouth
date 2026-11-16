package com.ft.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class MainView extends BorderPane {

    public MainView() {

        ImageView imageView = new ImageView("logo-horizontal.png");
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        Button signalFlowButton = new Button("Signal Flow");

        Button routhCriterionButton = new Button("Routh Criterion");

        VBox homeContent = new VBox(30);

        homeContent.setAlignment(Pos.CENTER);

        homeContent.getChildren().addAll(
                imageView,
                signalFlowButton,
                routhCriterionButton
        );

        setCenter(homeContent);
    }
}