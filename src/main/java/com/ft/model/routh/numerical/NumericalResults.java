package com.ft.model.routh.numerical;

import org.apache.commons.numbers.complex.Complex;

public class NumericalResults {

    private final Complex[] roots;

    private final int rightHalfPlaneCount;
    private final int leftHalfPlaneCount;
    private final int imaginaryAxisCount;
    private final boolean isStable;

    public NumericalResults(
            Complex[] roots,
            int rightHalfPlaneCount,
            int leftHalfPlaneCount,
            int imaginaryAxisCount,
            boolean isStable) {

        this.roots = roots.clone();
        this.rightHalfPlaneCount = rightHalfPlaneCount;
        this.leftHalfPlaneCount = leftHalfPlaneCount;
        this.imaginaryAxisCount = imaginaryAxisCount;
        this.isStable = isStable;
    }

    public Complex[] getRoots() {
        return roots.clone();
    }

    public int getRightHalfPlaneCount() {
        return rightHalfPlaneCount;
    }

    public int getLeftHalfPlaneCount() {
        return leftHalfPlaneCount;
    }

    public int getImaginaryAxisCount() {
        return imaginaryAxisCount;
    }

    public int getTotalRoots() {
        return roots.length;
    }
    
    public boolean isStable() {
        return isStable;
    }
}