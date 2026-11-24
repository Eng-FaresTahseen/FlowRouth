package com.ft.view;

import atlantafx.base.theme.Styles;
import com.ft.model.routh.RouthResult;
import com.ft.model.routh.RouthStep;
import com.ft.model.routh.numerical.NumericalResults;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.numbers.complex.Complex;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouthView extends BorderPane {

    public enum Verdict { AGREE, DISAGREE, ROUTH_FAILED, NUMERIC_FAILED, BOTH_FAILED }

    private static final double EPS = 1e-10;
    private static final String DASH = "-";

    private final DecimalFormat df =
            new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.US));

    private final TextField input = new TextField("1 2 3 4");
    private final ComboBox<String> examples = new ComboBox<>();
    private final Button calcButton = new Button("Calculate");
    private final Button backButton = new Button("Home");
    private final Label polyLabel = new Label();
    private final Label errorLabel = new Label();
    private final Label routhBadge = newStatusBox();
    private final Label numericBadge = newStatusBox();

    private final TableView<String[]> routhTable = new TableView<>();
    private final TableView<String[]> stepsTable = new TableView<>();
    private final TableView<String[]> rootsTable = new TableView<>();
    private final TableView<String[]> compareTable = new TableView<>();
    private final Label compareNote = new Label();

    private final RootPlotPane rootPlot = new RootPlotPane();

    private Runnable onCalculate = () -> {};

    public RouthView() {
        getStyleClass().add(Styles.BG_SUBTLE);
        setPadding(new Insets(16));

        setTop(buildTop());
        setCenter(buildTabs());

        calcButton.setOnAction(e -> onCalculate.run());
        input.setOnAction(e -> onCalculate.run());
        examples.setOnAction(e -> {
            String v = examples.getValue();
            if (v != null) {
                input.setText(v.split("\\s+->\\s+")[0]);
                onCalculate.run();
            }
        });
    }

    public String getInputText() {
        return input.getText();
    }

    public void setOnCalculate(Runnable action) {
        this.onCalculate = action;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }

    public void showResults(double[] coeffs,
                            RouthResult routh, String routhError,
                            NumericalResults numeric, String numericError,
                            Verdict verdict) {

        errorLabel.setText("");
        polyLabel.setText("P(s) = " + polynomialToString(coeffs) + " = 0");

        int n = coeffs.length;
        showRouth(routh, routhError, n);
        showSteps(routh, n);
        showRoots(numeric);
        showComparison(routh, numeric, n - 1);
        showVerdict(verdict, routhError, numericError);
    }

    private VBox buildTop() {
        Label title = new Label("Routh-Hurwitz Stability Criterion");
        title.getStyleClass().add(Styles.TITLE_3);

        backButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        HBox header = new HBox(12, backButton, title);
        header.setAlignment(Pos.CENTER_LEFT);

        Label coeffLbl = new Label("Coefficients:");
        coeffLbl.setMinWidth(Region.USE_PREF_SIZE);

        input.setPromptText("e.g. 1 2 3 4  ->  s^3 + 2s^2 + 3s + 4");
        HBox.setHgrow(input, Priority.ALWAYS);

        examples.setPromptText("Load Example");
        examples.getItems().addAll(
                "1 2 3 4 -> Stable (3rd order)",
                "1 3 5 4 2 -> Stable (4th order)",
                "1 1 10 72 152 240 -> Unstable (5th order)",
                "1 1 2 8 -> Unstable (3rd order)"
        );

        calcButton.getStyleClass().add(Styles.ACCENT);

        HBox inputBar = new HBox(8, coeffLbl, input, examples, calcButton);
        inputBar.setAlignment(Pos.CENTER_LEFT);

        polyLabel.getStyleClass().add(Styles.TEXT_BOLD);
        errorLabel.getStyleClass().addAll(Styles.DANGER, Styles.TEXT_BOLD);

        HBox badges = new HBox(10, routhBadge, numericBadge);
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(8, header, inputBar, polyLabel, errorLabel, badges);
        top.setPadding(new Insets(0, 0, 10, 0));
        return top;
    }

    private TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
                new Tab("Routh Table", wrap(routhTable, null)),
                new Tab("Calculation Steps", wrap(stepsTable, null)),
                new Tab("Numerical Roots", wrap(rootsTable, null)),
                new Tab("Root Plot", buildPlotTab()),
                new Tab("Comparison", wrap(compareTable, compareNote))
        );

        compareNote.setWrapText(true);
        return tabs;
    }

    private VBox buildPlotTab() {
        Label legend = new Label("Green: Left Half-Plane (Stable)   |   Red: Right Half-Plane (Unstable)   |   Amber: Imaginary Axis");
        legend.getStyleClass().add(Styles.TEXT_MUTED);

        VBox.setVgrow(rootPlot, Priority.ALWAYS);
        VBox box = new VBox(8, rootPlot, legend);
        box.setPadding(new Insets(8, 0, 0, 0));
        return box;
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

    private void showRouth(RouthResult r, String error, int n) {
        if (r == null) {
            routhTable.getColumns().clear();
            routhTable.getItems().clear();
            setStatusBox(routhBadge, "Routh: " + error, false);
            return;
        }

        double[][] t = r.getTable();
        int cols = t[0].length;

        List<String> headers = new ArrayList<>();
        headers.add("Row");
        for (int j = 0; j < cols; j++) {
            headers.add("c" + (j + 1));
        }

        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < t.length; i++) {
            String[] row = new String[cols + 1];
            row[0] = rowLabel(i, n);
            for (int j = 0; j < cols; j++) {
                row[j + 1] = fmt(t[i][j]);
            }
            rows.add(row);
        }
        fill(routhTable, headers, rows);

        @SuppressWarnings("unchecked")
        TableColumn<String[], String> firstCol =
                (TableColumn<String[], String>) routhTable.getColumns().get(1);
        firstCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().remove("neg");
                if (!empty && item != null && item.startsWith("-")) {
                    getStyleClass().add("neg");
                }
            }
        });

        int changes = r.getRightHalfPlaneRoots();
        setStatusBox(routhBadge,
                "Routh: " + (r.isStable() ? "Stable" : "Unstable")
                        + " (" + changes + " sign change" + (changes == 1 ? "" : "s") + ")",
                r.isStable());
    }

    private void showSteps(RouthResult r, int n) {
        List<String> headers = List.of("Cell", "Formula  (a*b - c*d) / a", "Result");
        List<String[]> rows = new ArrayList<>();

        if (r != null) {
            for (RouthStep s : r.getSteps()) {
                String a = fmt(s.getLowerRowFirst());
                String b = fmt(s.getUpperRowNext());
                String c = fmt(s.getUpperRowFirst());
                String d = fmt(s.getLowerRowNext());

                String formula = "(" + a + " × " + b + " − " + c + " × " + d + ") / " + a;
                String cell = rowLabel(s.getRow(), n) + " , c" + (s.getColumn() + 1);
                rows.add(new String[]{cell, formula, fmt(s.getResult())});
            }
        }
        fill(stepsTable, headers, rows, new double[]{1.2, 4, 1.2});
    }

    private void showRoots(NumericalResults r) {
        List<String> headers = List.of("#", "Real", "Imaginary", "Location");
        List<String[]> rows = new ArrayList<>();

        if (r == null) {
            fill(rootsTable, headers, rows);
            rootPlot.clear();
            setStatusBox(numericBadge, "Numerical: unavailable", false);
            return;
        }

        Complex[] roots = r.getRoots();
        for (int i = 0; i < roots.length; i++) {
            double re = roots[i].getReal();
            double im = roots[i].getImaginary();
            rows.add(new String[]{
                    String.valueOf(i + 1), fmt(re), fmt(im), location(re)});
        }
        fill(rootsTable, headers, rows);
        rootPlot.setRoots(roots);

        setStatusBox(numericBadge,
                "Numerical: " + (r.isStable() ? "Stable" : "Unstable")
                        + " (RHP: " + r.getRightHalfPlaneCount()
                        + ", LHP: " + r.getLeftHalfPlaneCount()
                        + ", Axis: " + r.getImaginaryAxisCount() + ")",
                r.isStable());
    }

    private void showComparison(RouthResult routh, NumericalResults num, int degree) {
        List<String> headers = List.of("Metric", "Routh", "Numerical (roots)", "Match");
        List<String[]> rows = new ArrayList<>();

        String rDegree = routh != null ? String.valueOf(degree) : DASH;
        String nDegree = num != null ? String.valueOf(num.getTotalRoots()) : DASH;

        String rRhp = routh != null ? String.valueOf(routh.getRightHalfPlaneRoots()) : DASH;
        String nRhp = num != null ? String.valueOf(num.getRightHalfPlaneCount()) : DASH;

        String rLhp = routh != null
                ? String.valueOf(degree - routh.getRightHalfPlaneRoots()) : DASH;
        String nLhp = num != null ? String.valueOf(num.getLeftHalfPlaneCount()) : DASH;

        String rAxis = routh != null ? "0" : DASH;
        String nAxis = num != null ? String.valueOf(num.getImaginaryAxisCount()) : DASH;

        String rStable = routh != null ? yesNo(routh.isStable()) : DASH;
        String nStable = num != null ? yesNo(num.isStable()) : DASH;

        rows.add(row("Number of roots", rDegree, nDegree));
        rows.add(row("Right half-plane (unstable)", rRhp, nRhp));
        rows.add(row("Left half-plane (stable)", rLhp, nLhp));
        rows.add(row("On imaginary axis", rAxis, nAxis));
        rows.add(row("Stable?", rStable, nStable));
        fill(compareTable, headers, rows, new double[]{3, 1.5, 1.5, 1});
    }

    private void showVerdict(Verdict verdict, String routhError, String numericError) {
        switch (verdict) {
            case AGREE -> setStatusBox(compareNote, "Both methods agree.", true);
            case DISAGREE -> setStatusBox(compareNote, "Methods disagree. Check imaginary axis roots or tolerance.", false);
            case ROUTH_FAILED -> setStatusBox(compareNote, "Routh failed: " + routhError, false);
            case NUMERIC_FAILED -> setStatusBox(compareNote, "Numerical failed: " + numericError, false);
            case BOTH_FAILED -> setStatusBox(compareNote, "Both failed.", false);
        }
    }

    private static String[] row(String metric, String routh, String numeric) {
        String match = (routh.equals(DASH) || numeric.equals(DASH))
                ? DASH
                : (routh.equals(numeric) ? "Yes" : "No");
        return new String[]{metric, routh, numeric, match};
    }

    private static String yesNo(boolean b) {
        return b ? "Yes" : "No";
    }

    private static String location(double real) {
        if (real > EPS) return "Right half-plane";
        if (real < -EPS) return "Left half-plane";
        return "Imaginary axis";
    }

    private static String rowLabel(int i, int n) {
        return "s^" + (n - 1 - i);
    }

    private String fmt(double v) {
        if (Math.abs(v) < EPS) return "0";
        return df.format(v);
    }

    private String polynomialToString(double[] c) {
        StringBuilder sb = new StringBuilder();
        int n = c.length;
        for (int i = 0; i < n; i++) {
            double v = c[i];
            if (v == 0) continue;
            int power = n - 1 - i;

            String sign = v < 0 ? "-" : "+";
            double abs = Math.abs(v);
            String coef = (abs == 1 && power != 0) ? "" : df.format(abs);
            String term = coef + (power == 0 ? "" : power == 1 ? "s" : "s^" + power);

            if (sb.length() == 0) {
                sb.append(v < 0 ? "-" : "").append(term);
            } else {
                sb.append(" ").append(sign).append(" ").append(term);
            }
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    private void fill(TableView<String[]> table, List<String> headers, List<String[]> rows) {
        double[] weights = new double[headers.size()];
        java.util.Arrays.fill(weights, 1.0);
        fill(table, headers, rows, weights);
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
                "status-box",
                Styles.BG_SUCCESS_SUBTLE, Styles.SUCCESS,
                Styles.BG_DANGER_SUBTLE, Styles.DANGER
        );
        l.getStyleClass().add("status-box");
        if (ok) {
            l.getStyleClass().addAll(Styles.BG_SUCCESS_SUBTLE, Styles.SUCCESS);
        } else {
            l.getStyleClass().addAll(Styles.BG_DANGER_SUBTLE, Styles.DANGER);
        }
    }
}
