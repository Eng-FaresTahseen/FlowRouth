package com.ft.model.signalFlow.data;
import java.util.ArrayList;

public class SignalFlowGraph {
    private final ArrayList<GraphVertex> vertices = new ArrayList<>();
    private final ArrayList<DirectedEdge> edges = new ArrayList<>();


    // You will need to implement more methods

    public void addVertex(GraphVertex vertex) {
        vertices.add(vertex);
    }

    public void addEdge(DirectedEdge edge) {
        edges.add(edge);
    }
}
