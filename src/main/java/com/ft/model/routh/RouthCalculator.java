package com.ft.model.routh;

import java.util.ArrayList;
import java.util.List;

public class RouthCalculator {

	private static final double EPSILON = 1e-10;

    private final double[] coefficients;

    public RouthCalculator(double[] coefficients) {

        if (coefficients == null || coefficients.length < 2) {
            throw new IllegalArgumentException("Invalid coefficients");
        }

        for (double c : coefficients) {
            if (!Double.isFinite(c)) {
                throw new IllegalArgumentException("Coefficients must be finite");
            }
        }

        if (coefficients[0] == 0) {
            throw new IllegalArgumentException("Leading coefficient must be non-zero");
        }

        this.coefficients = coefficients.clone();
    }

    public RouthResult buildTable() {

        int rows = coefficients.length;
        int columns = (rows + 1) / 2;

        double[][] table = new double[rows][columns];
        List<RouthStep> steps = new ArrayList<>();

        for (int j = 0; j < columns; j++) {
            int first = 2 * j;
            int second = 2 * j + 1;

            if (first < rows) {
                table[0][j] = coefficients[first];
            }
            if (second < rows) {
                table[1][j] = coefficients[second];
            }
        }

        checkFirstElement(table, 1);

        for (int i = 2; i < rows; i++) {
            for (int j = 0; j < columns - 1; j++) {

                double lowerRowFirst = table[i - 1][0];
                double upperRowNext = table[i - 2][j + 1];
                double upperRowFirst = table[i - 2][0];
                double lowerRowNext = table[i - 1][j + 1];

                double result =
                    (lowerRowFirst * upperRowNext
                    - upperRowFirst * lowerRowNext)
                    / lowerRowFirst;

                table[i][j] = result;

                steps.add(new RouthStep(
                    i, j,
                    lowerRowFirst, upperRowNext,
                    upperRowFirst, lowerRowNext,
                    result
                ));
            }

            checkFirstElement(table, i);
        }

        int changes = countSignChanges(table);

        return new RouthResult(table, steps, changes, changes == 0);
    }

    private static void checkFirstElement(double[][] table, int row) {

        if (Math.abs(table[row][0]) < EPSILON) {
            throw new ArithmeticException(
                "Zero first-column element at row " + row + " (not supported)"
            );
        }
    }

    private static int countSignChanges(double[][] table) {

        int changes = 0;

        for (int i = 1; i < table.length; i++) {
            if (Math.signum(table[i - 1][0]) != Math.signum(table[i][0])) {
                changes++;
            }
        }

        return changes;
    }
}