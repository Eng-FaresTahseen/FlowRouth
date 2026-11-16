package com.ft.controller;
import com.ft.view.HomeView;
import com.ft.view.MainView;
import com.ft.view.RouthView;
import com.ft.view.SignalFlowView;

public class MainController {
    private final MainView mainView;
    private final HomeView homeView;
    private final SignalFlowView signalFlowView;
    private final RouthView routhView;

    public MainController(MainView mainView , HomeView homeView , SignalFlowView signalFlowView , RouthView routhView) {
        this.mainView = mainView;
        this.homeView = homeView;
        this.signalFlowView = signalFlowView;
        this.routhView = routhView;
        homeView.getSignalFlowButton().setOnAction(e -> showSignalFlowScreen());
        homeView.getRouthCriterionButton().setOnAction(e -> showRouthCriterionScreen());
    }

    public void showHomeScreen() {
        mainView.showScreen(homeView);
    }

    public void showSignalFlowScreen() {
        mainView.showScreen(signalFlowView);
    }

    public void showRouthCriterionScreen() {
        mainView.showScreen(routhView);
    }
}
