package com.ft.model.signalFlow.data;

public class GraphVertex {
    private String name;
    private final int id; // Must be unique for each vertex

    public GraphVertex(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

}
