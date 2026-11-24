package com.ft.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class HomeView extends BorderPane {

    private final Button signalFlowButton;
    private final Button routhCriterionButton;

    public HomeView() {
        getStyleClass().add(Styles.BG_SUBTLE);

        ImageView flowIcon = new ImageView("data-flow.png");
        flowIcon.setFitWidth(160);
        flowIcon.setPreserveRatio(true);
        flowIcon.setMouseTransparent(true);

        ImageView routhIcon = new ImageView("coordinate.png");
        routhIcon.setFitWidth(160);
        routhIcon.setPreserveRatio(true);
        routhIcon.setMouseTransparent(true);

        this.signalFlowButton = new Button();
        this.signalFlowButton.setGraphic(flowIcon);
        this.signalFlowButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.signalFlowButton.setPrefSize(240, 240);
        this.signalFlowButton.setPickOnBounds(true);

        this.routhCriterionButton = new Button();
        this.routhCriterionButton.setGraphic(routhIcon);
        this.routhCriterionButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.routhCriterionButton.setPrefSize(240, 240);
        this.routhCriterionButton.setPickOnBounds(true);

        HBox box = new HBox(48, signalFlowButton, routhCriterionButton);
        box.setAlignment(Pos.CENTER);

        setCenter(box);
    }

    public Button getSignalFlowButton() {
        return signalFlowButton;
    }

    public Button getRouthCriterionButton() {
        return routhCriterionButton;
    }
}
