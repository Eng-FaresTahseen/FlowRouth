package com.ft.view;

import atlantafx.base.theme.Styles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class SignalFlowView extends BorderPane {

    private static final String EX1_EDGES =
            "R->x1:1, x1->x2:2, x2->x3:3, x3->x4:4, x4->x5:5, x5->C:1, x1->x4:10, x2->x1:-2, x3->x2:-1, x5->x4:-3";
    private static final String EX2_EDGES =
            "R->x1:1, x1->x2:2, x2->x3:1, x3->x4:3, x4->x5:1, x5->x6:4, x6->C:1, x1->x3:5, x3->x5:2, x2->x1:-1, x4->x3:-2, x6->x5:-1";
    private static final String EX3_EDGES =
            "R->x1:1, x1->x2:4, x2->x3:2, x3->x4:3, x4->C:1, x1->x3:6, x2->x1:-1, x4->x3:-2, x2->x2:-0.5, x4->x1:-1";

    private final TextField input = new TextField();
    private final ComboBox<String> examples = new ComboBox<>();
    private final Button calcButton = new Button("Calculate");
    private final Button backButton = new Button("Home");

    private final TextField fromField = new TextField();
    private final TextField toField = new TextField();
    private final TextField gainField = new TextField();
    private final Button addBranchButton = new Button("Add Branch");
    private final Button clearButton = new Button("Clear");

    private final ComboBox<String> sourceBox = new ComboBox<>();
    private final ComboBox<String> targetBox = new ComboBox<>();
    private final CheckBox autoLayoutCheck = new CheckBox("Auto Layout");

    private final Label errorLabel = new Label();
    private final Label tfBadge = newStatusBox();
    private final Label deltaBadge = newStatusBox();
    private final Label pathsBadge = newStatusBox();
    private final Label loopsBadge = newStatusBox();

    private final SignalFlowGraphPane graphPane = new SignalFlowGraphPane();

    private final TableView<String[]> pathsTable = new TableView<>();
    private final TableView<String[]> loopsTable = new TableView<>();
    private final TableView<String[]> nonTouchingTable = new TableView<>();
    private final TableView<String[]> deltaTable = new TableView<>();
    private final TableView<String[]> masonTable = new TableView<>();

    private final Label nonTouchingNote = new Label();
    private final Label deltaNote = new Label();
    private final Label masonNote = new Label();

    private Runnable onCalculate = () -> {};
    private Runnable onAddBranch = () -> {};
    private Runnable onClear = () -> {};

    public SignalFlowView() {
        getStyleClass().add(Styles.BG_SUBTLE);
        setPadding(new Insets(16));

        setTop(buildTop());
        setCenter(buildCenter());

        calcButton.setOnAction(e -> onCalculate.run());
        input.setOnAction(e -> onCalculate.run());
        addBranchButton.setOnAction(e -> onAddBranch.run());
        gainField.setOnAction(e -> onAddBranch.run());
        clearButton.setOnAction(e -> onClear.run());
        autoLayoutCheck.setOnAction(e -> graphPane.setAutomaticLayout(autoLayoutCheck.isSelected()));

        examples.setOnAction(e -> {
            String v = examples.getValue();
            if (v == null) return;
            if (v.startsWith("Example 1")) {
                input.setText(EX1_EDGES);
            } else if (v.startsWith("Example 2")) {
                input.setText(EX2_EDGES);
            } else if (v.startsWith("Example 3")) {
                input.setText(EX3_EDGES);
            }
            autoLayoutCheck.setSelected(false);
            sourceBox.getItems().clear();
            targetBox.getItems().clear();
            onCalculate.run();
        });

        sourceBox.setOnAction(e -> onCalculate.run());
        targetBox.setOnAction(e -> onCalculate.run());
    }

    private VBox buildTop() {
        Label title = new Label("Signal Flow Graph & Mason's Gain Formula");
        title.getStyleClass().add(Styles.TITLE_3);

        backButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        HBox header = new HBox(12, backButton, title);
        header.setAlignment(Pos.CENTER_LEFT);

        input.setPromptText("Branches (e.g. R->x1:1, x1->x2:2, x2->C:1, x2->x1:-1)");
        HBox.setHgrow(input, Priority.ALWAYS);

        examples.setPromptText("Load Example");
        examples.setPrefWidth(240);
        examples.setMaxWidth(240);
        examples.getItems().addAll(
                "Example 1 (2 Paths, 3 Loops)",
                "Example 2 (4 Paths, 3 Non-Touching)",
                "Example 3 (Self-Loop System)"
        );

        calcButton.getStyleClass().add(Styles.ACCENT);

        HBox inputBar = new HBox(8, minLabel("Branches:"), input, examples, calcButton);
        inputBar.setAlignment(Pos.CENTER_LEFT);

        fromField.setPromptText("From");
        fromField.setPrefWidth(80);
        toField.setPromptText("To");
        toField.setPrefWidth(80);
        gainField.setPromptText("Gain");
        gainField.setPrefWidth(80);

        addBranchButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        clearButton.getStyleClass().add(Styles.BUTTON_OUTLINED);

        sourceBox.setPrefWidth(85);
        sourceBox.setMinWidth(80);
        targetBox.setPrefWidth(85);
        targetBox.setMinWidth(80);

        HBox builderBar = new HBox(8,
                minLabel("From:"), fromField,
                minLabel("To:"), toField,
                minLabel("Gain:"), gainField,
                addBranchButton, clearButton,
                minLabel("Input:"), sourceBox,
                minLabel("Output:"), targetBox,
                autoLayoutCheck);
        builderBar.setAlignment(Pos.CENTER_LEFT);

        errorLabel.getStyleClass().addAll(Styles.DANGER, Styles.TEXT_BOLD);

        HBox badges = new HBox(10, tfBadge, deltaBadge, pathsBadge, loopsBadge);
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(8, header, inputBar, builderBar, errorLabel, badges);
        top.setPadding(new Insets(0, 0, 10, 0));
        return top;
    }

    private static Label minLabel(String text) {
        Label l = new Label(text);
        l.setMinWidth(Region.USE_PREF_SIZE);
        return l;
    }

    private VBox buildCenter() {
        graphPane.setPrefHeight(250);
        graphPane.setMinHeight(220);

        TabPane tabs = buildTabs();
        VBox.setVgrow(tabs, Priority.ALWAYS);

        return new VBox(10, graphPane, tabs);
    }

    private TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        nonTouchingNote.setWrapText(true);
        nonTouchingNote.getStyleClass().addAll("status-box", Styles.BG_DEFAULT);

        deltaNote.setWrapText(true);
        deltaNote.getStyleClass().addAll("status-box", Styles.BG_DEFAULT);

        masonNote.setWrapText(true);
        masonNote.getStyleClass().addAll("status-box", Styles.BG_SUCCESS_SUBTLE, Styles.SUCCESS);

        tabs.getTabs().addAll(
                new Tab("Forward Paths", wrap(pathsTable, null)),
                new Tab("Individual Loops", wrap(loopsTable, null)),
                new Tab("Non-Touching Loops", wrap(nonTouchingTable, nonTouchingNote)),
                new Tab("Determinants (Δ & Δi)", wrap(deltaTable, deltaNote)),
                new Tab("Mason's Formula", wrap(masonTable, masonNote))
        );
        return tabs;
    }

    private VBox wrap(TableView<String[]> table, Label note) {
        table.setPlaceholder(new Label("No data"));
        table.getStyleClass().addAll(Styles.BORDERED, Styles.STRIPED, Styles.DENSE);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox box = new VBox(8);
        box.setPadding(new Insets(8, 0, 0, 0));
        box.getChildren().add(table);
        if (note != null) {
            box.getChildren().add(note);
        }
        return box;
    }

    public Button getBackButton() {
        return backButton;
    }

    public SignalFlowGraphPane getGraphPane() {
        return graphPane;
    }

    public String getInputText() {
        return input.getText();
    }

    public void setInputText(String text) {
        input.setText(text);
    }

    public String getFromText() {
        return fromField.getText();
    }

    public String getToText() {
        return toField.getText();
    }

    public String getGainText() {
        return gainField.getText();
    }

    public void clearBranchFields() {
        fromField.clear();
        toField.clear();
        gainField.clear();
    }

    public String getSelectedSource() {
        return sourceBox.getValue();
    }

    public String getSelectedTarget() {
        return targetBox.getValue();
    }

    public void updateNodes(List<String> nodes, String src, String dst) {
        sourceBox.setOnAction(null);
        targetBox.setOnAction(null);

        sourceBox.getItems().setAll(nodes);
        targetBox.getItems().setAll(nodes);

        if (src != null && nodes.contains(src)) {
            sourceBox.setValue(src);
        } else if (!nodes.isEmpty()) {
            sourceBox.setValue(nodes.get(0));
        }

        if (dst != null && nodes.contains(dst)) {
            targetBox.setValue(dst);
        } else if (!nodes.isEmpty()) {
            targetBox.setValue(nodes.get(nodes.size() - 1));
        }

        sourceBox.setOnAction(e -> onCalculate.run());
        targetBox.setOnAction(e -> onCalculate.run());
    }

    public void showError(String msg) {
        errorLabel.setText(msg == null ? "" : msg);
    }

    public void setBadges(String tfText, boolean okTf,
                          String deltaText, boolean okDelta,
                          int pathsCount, int loopsCount) {
        setStatusBox(tfBadge, tfText, okTf);
        setStatusBox(deltaBadge, deltaText, okDelta);
        setStatusBox(pathsBadge, "Forward Paths: " + pathsCount, true);
        setStatusBox(loopsBadge, "Individual Loops: " + loopsCount, true);
    }

    public void showPaths(List<String[]> rows) {
        fill(pathsTable,
                List.of("Path", "Nodes", "Branch Product Formula & Substitution", "Gain"),
                rows,
                new double[]{0.7, 2.2, 4.1, 0.9});
    }

    public void showLoops(List<String[]> rows) {
        fill(loopsTable,
                List.of("Loop", "Loop Nodes", "Branch Product Formula & Substitution", "Gain"),
                rows,
                new double[]{0.7, 2.2, 4.1, 0.9});
    }

    public void showNonTouching(List<String[]> rows, String summary) {
        fill(nonTouchingTable,
                List.of("Order", "Loops", "Loop Nodes", "Product Substitution", "Gain Product"),
                rows,
                new double[]{1.2, 1.1, 2.1, 2.6, 1.0});
        nonTouchingNote.setText(summary);
    }

    public void showDeterminants(List<String[]> rows, String deltaFormulaNote) {
        fill(deltaTable,
                List.of("Quantity", "Included Loops", "Formula & Numerical Substitution", "Final Value"),
                rows,
                new double[]{1.3, 1.6, 4.4, 0.8});
        deltaNote.setText(deltaFormulaNote);
    }

    public void showMason(List<String[]> rows, String finalFormulaNote, boolean ok) {
        fill(masonTable,
                List.of("Step / Term", "Formula", "Numerical Substitution", "Value"),
                rows,
                new double[]{1.3, 2.1, 3.5, 1.1});
        masonNote.setText(finalFormulaNote);
    }

    public void setOnCalculate(Runnable r) {
        this.onCalculate = r;
    }

    public void setOnAddBranch(Runnable r) {
        this.onAddBranch = r;
    }

    public void setOnClear(Runnable r) {
        this.onClear = r;
    }

    private void fill(TableView<String[]> table, List<String> headers,
                      List<String[]> rows, double[] weights) {
        double total = 0;
        for (double w : weights) total += w;

        table.getColumns().clear();
        for (int i = 0; i < headers.size(); i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(headers.get(i));
            col.setSortable(false);
            col.setMinWidth(40);
            col.setCellValueFactory(d -> new SimpleStringProperty(
                    idx < d.getValue().length ? d.getValue()[idx] : ""));
            col.prefWidthProperty().bind(
                    table.widthProperty().subtract(20).multiply(weights[i] / total));
            table.getColumns().add(col);
        }
        table.setItems(FXCollections.observableArrayList(rows));
    }

    private static Label newStatusBox() {
        Label l = new Label();
        l.getStyleClass().addAll("status-box", Styles.TEXT_BOLD);
        return l;
    }

    private static void setStatusBox(Label l, String text, boolean ok) {
        l.setText(text);
        l.getStyleClass().removeAll(
                Styles.BG_SUCCESS_SUBTLE, Styles.SUCCESS,
                Styles.BG_DANGER_SUBTLE, Styles.DANGER
        );
        if (ok) {
            l.getStyleClass().addAll(Styles.BG_SUCCESS_SUBTLE, Styles.SUCCESS);
        } else {
            l.getStyleClass().addAll(Styles.BG_DANGER_SUBTLE, Styles.DANGER);
        }
    }
}
