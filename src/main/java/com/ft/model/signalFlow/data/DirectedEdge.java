package com.ft.model.signalFlow.data;

public class DirectedEdge {
    private final GraphVertex source;
    private final GraphVertex target;
    private double gain;

    public DirectedEdge(GraphVertex source, GraphVertex target , double gain) {
        this.source = source;
        this.target = target;
        this.gain = gain;
    }

    public double getGain() {
        return gain;
    }

    public void setGain(double gain) {
        this.gain = gain;
    }

    public GraphVertex getSource() {
        return source;
    }


    public GraphVertex getTarget() {
        return target;
    }

}
