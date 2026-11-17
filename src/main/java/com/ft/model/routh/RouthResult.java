package com.ft.model.routh;

import java.util.List;

public class RouthResult {

    private final double[][] table;
    private final List<RouthStep> steps;
    private final int rightHalfPlaneRoots;
    private final boolean stable;

    public RouthResult(double[][] table,
                       List<RouthStep> steps,
                       int rightHalfPlaneRoots,
                       boolean stable) {

        this.table = copyTable(table);
        this.steps = List.copyOf(steps);
        this.rightHalfPlaneRoots = rightHalfPlaneRoots;
        this.stable = stable;
    }

    public double[][] getTable() {
        return copyTable(table);
    }

    public List<RouthStep> getSteps() {
        return steps;
    }

    public int getRightHalfPlaneRoots() {
        return rightHalfPlaneRoots;
    }

    public boolean isStable() {
        return stable;
    }

    private static double[][] copyTable(double[][] source) {

        double[][] copy = new double[source.length][];

        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }

        return copy;
    }
}
