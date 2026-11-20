package com.ft.model.routh.numerical;

import org.apache.commons.math4.legacy.analysis.solvers.LaguerreSolver;
import org.apache.commons.numbers.complex.Complex;

public class NumericalCalculator {

    private static final double EPSILON = 1e-10;

    public NumericalResults calculateRoots(double[] coefficients) {

        validateCoefficients(coefficients);

        int n = coefficients.length;

        // Convert descending order to ascending order
        double[] ascending = new double[n];

        for (int i = 0; i < n; i++) {
            ascending[i] = coefficients[n - 1 - i];
        }

        LaguerreSolver solver = new LaguerreSolver();

        Complex[] roots = solver.solveAllComplex(ascending, 0.0);

        int rightCount = 0;
        int leftCount = 0;
        int imaginaryCount = 0;

        for (Complex root : roots) {

            double real = root.getReal();

            if (real > EPSILON) {
                rightCount++;
            } 
            else if (real < -EPSILON) {
                leftCount++;
            } 
            else {
                imaginaryCount++;
            }
        }
        
        boolean isStable = rightCount == 0 && imaginaryCount == 0;

        return new NumericalResults(
        	    roots, rightCount, leftCount, imaginaryCount, isStable
        	);
    }

    private void validateCoefficients(double[] coefficients) {

        if (coefficients == null || coefficients.length < 2) {
            throw new IllegalArgumentException(
                "Invalid polynomial coefficients"
            );
        }

        if (coefficients[0] == 0) {
            throw new IllegalArgumentException(
                "Leading coefficient cannot be zero"
            );
        }

        for (double coefficient : coefficients) {
            if (!Double.isFinite(coefficient)) {
                throw new IllegalArgumentException(
                    "Coefficients must be finite numbers"
                );
            }
        }
    }
}