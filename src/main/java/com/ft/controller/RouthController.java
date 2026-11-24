package com.ft.controller;

import com.ft.model.routh.RouthCalculator;
import com.ft.model.routh.RouthResult;
import com.ft.model.routh.numerical.NumericalCalculator;
import com.ft.model.routh.numerical.NumericalResults;
import com.ft.view.RouthView;
import com.ft.view.RouthView.Verdict;

public class RouthController {

    private final RouthView view;

    public RouthController(RouthView view) {
        this.view = view;
        this.view.setOnCalculate(this::onCalculate);
        onCalculate();
    }

    private void onCalculate() {

        double[] coeffs;
        try {
            coeffs = parseCoefficients(view.getInputText());
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            return;
        }

        // Routh
        RouthResult routh = null;
        String routhError = null;
        try {
            routh = new RouthCalculator(coeffs).buildTable();
        } catch (RuntimeException ex) {
            routhError = ex.getMessage();
        }

        // Numerical roots
        NumericalResults numeric = null;
        String numericError = null;
        try {
            numeric = new NumericalCalculator().calculateRoots(coeffs);
        } catch (RuntimeException ex) {
            numericError = ex.getMessage();
        }

        view.showResults(coeffs, routh, routhError, numeric, numericError,
                decideVerdict(routh, numeric));
    }

    private static Verdict decideVerdict(RouthResult routh, NumericalResults numeric) {
        if (routh == null && numeric == null) return Verdict.BOTH_FAILED;
        if (routh == null) return Verdict.ROUTH_FAILED;
        if (numeric == null) return Verdict.NUMERIC_FAILED;

        boolean agree = routh.isStable() == numeric.isStable()
                && routh.getRightHalfPlaneRoots() == numeric.getRightHalfPlaneCount();

        return agree ? Verdict.AGREE : Verdict.DISAGREE;
    }

    private static double[] parseCoefficients(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Please enter the polynomial coefficients.");
        }

        String[] parts = text.trim().split("[\\s,;]+");
        double[] result = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid number: \"" + parts[i] + "\"");
            }
        }
        return result;
    }
}
