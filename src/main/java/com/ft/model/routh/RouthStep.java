package com.ft.model.routh;

public class RouthStep {

    private final int row;
    private final int column;
    private final double lowerRowFirst;
    private final double upperRowNext;
    private final double upperRowFirst;
    private final double lowerRowNext;
    private final double result;

    public RouthStep(int row, int column,
                     double lowerRowFirst,
                     double upperRowNext,
                     double upperRowFirst,
                     double lowerRowNext,
                     double result) {

        this.row = row;
        this.column = column;
        this.lowerRowFirst = lowerRowFirst;
        this.upperRowNext = upperRowNext;
        this.upperRowFirst = upperRowFirst;
        this.lowerRowNext = lowerRowNext;
        this.result = result;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public double getLowerRowFirst() {
        return lowerRowFirst;
    }

    public double getUpperRowNext() {
        return upperRowNext;
    }

    public double getUpperRowFirst() {
        return upperRowFirst;
    }

    public double getLowerRowNext() {
        return lowerRowNext;
    }

    public double getResult() {
        return result;
    }
}