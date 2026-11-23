package com.ft.model.signalFlow.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SignalFlowGraph {
    private final ArrayList<GraphVertex> vertices = new ArrayList<>();
    private final ArrayList<DirectedEdge> edges = new ArrayList<>();

    // ------------------------------------------------------------
    // Basic graph construction / access
    // ------------------------------------------------------------

    public void addVertex(GraphVertex vertex) {
        vertices.add(vertex);
    }

    public void addEdge(DirectedEdge edge) {
        edges.add(edge);
    }

    public List<GraphVertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<DirectedEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public GraphVertex getVertexById(int id) {
        for (GraphVertex v : vertices) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }

    public GraphVertex getVertexByName(String name) {
        for (GraphVertex v : vertices) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    public List<DirectedEdge> getOutgoingEdges(GraphVertex vertex) {
        List<DirectedEdge> result = new ArrayList<>();
        for (DirectedEdge edge : edges) {
            if (edge.getSource().getId() == vertex.getId()) {
                result.add(edge);
            }
        }
        return result;
    }

    public List<DirectedEdge> getIncomingEdges(GraphVertex vertex) {
        List<DirectedEdge> result = new ArrayList<>();
        for (DirectedEdge edge : edges) {
            if (edge.getTarget().getId() == vertex.getId()) {
                result.add(edge);
            }
        }
        return result;
    }

    // ------------------------------------------------------------
    // Mason's gain formula
    // ------------------------------------------------------------

    public double calculateTransferFunction(GraphVertex source, GraphVertex target) {
        if (source == null || target == null) {
            return 0.0;
        }
        if (source.getId() == target.getId()) {
            return 1.0;
        }

        List<Path> forwardPaths = findForwardPaths(source, target);
        if (forwardPaths.isEmpty()) {
            return 0.0;
        }

        List<Loop> loops = findLoops();
        double delta = calculateDelta(loops);

        if (Math.abs(delta) < 1e-12) {
            return Double.NaN; // singular / undefined transfer function
        }

        double numerator = 0.0;
        for (Path path : forwardPaths) {
            List<Loop> nonTouchingLoops = new ArrayList<>();
            for (Loop loop : loops) {
                if (!path.touches(loop)) {
                    nonTouchingLoops.add(loop);
                }
            }
            double deltaK = calculateDelta(nonTouchingLoops);
            numerator += path.getGain() * deltaK;
        }

        return numerator / delta;
    }

    public double calculateTransferFunction(int sourceId, int targetId) {
        return calculateTransferFunction(getVertexById(sourceId), getVertexById(targetId));
    }

    public List<Path> findForwardPaths(GraphVertex source, GraphVertex target) {
        List<Path> paths = new ArrayList<>();
        if (source == null || target == null || source.getId() == target.getId()) {
            return paths;
        }
        Set<Integer> visited = new HashSet<>();
        visited.add(source.getId());
        dfsForwardPaths(source, target, visited, new ArrayList<>(), paths);
        return paths;
    }

    private void dfsForwardPaths(GraphVertex current,
                                 GraphVertex target,
                                 Set<Integer> visited,
                                 List<DirectedEdge> currentPath,
                                 List<Path> paths) {
        for (DirectedEdge edge : getOutgoingEdges(current)) {
            GraphVertex next = edge.getTarget();
            if (next.getId() == target.getId()) {
                currentPath.add(edge);
                paths.add(new Path(currentPath));
                currentPath.remove(currentPath.size() - 1);
            } else if (!visited.contains(next.getId())) {
                visited.add(next.getId());
                currentPath.add(edge);
                dfsForwardPaths(next, target, visited, currentPath, paths);
                currentPath.remove(currentPath.size() - 1);
                visited.remove(next.getId());
            }
        }
    }

    public List<Loop> findLoops() {
        List<Loop> loops = new ArrayList<>();
        for (GraphVertex start : vertices) {
            Set<Integer> visited = new HashSet<>();
            visited.add(start.getId());
            dfsLoops(start, start, visited, new ArrayList<>(), loops);
        }
        return loops;
    }

    private void dfsLoops(GraphVertex start,
                          GraphVertex current,
                          Set<Integer> visited,
                          List<DirectedEdge> currentPath,
                          List<Loop> loops) {
        for (DirectedEdge edge : getOutgoingEdges(current)) {
            GraphVertex next = edge.getTarget();
            if (next.getId() == start.getId()) {
                List<DirectedEdge> loopEdges = new ArrayList<>(currentPath);
                loopEdges.add(edge);
                loops.add(new Loop(loopEdges));
            } else if (next.getId() > start.getId() && !visited.contains(next.getId())) {
                visited.add(next.getId());
                currentPath.add(edge);
                dfsLoops(start, next, visited, currentPath, loops);
                currentPath.remove(currentPath.size() - 1);
                visited.remove(next.getId());
            }
        }
    }

    public double calculateDelta(List<Loop> loops) {
        double delta = 1.0;
        for (int k = 1; k <= loops.size(); k++) {
            double sum = sumNonTouchingLoopProducts(loops, 0, k, 1.0, new ArrayList<>());
            if (k % 2 == 1) {
                delta -= sum;
            } else {
                delta += sum;
            }
        }
        return delta;
    }

    private double sumNonTouchingLoopProducts(List<Loop> loops,
                                              int startIndex,
                                              int remaining,
                                              double product,
                                              List<Loop> selectedLoops) {
        if (remaining == 0) {
            return product;
        }
        double sum = 0.0;
        for (int i = startIndex; i <= loops.size() - remaining; i++) {
            Loop candidate = loops.get(i);
            boolean touching = false;
            for (Loop selected : selectedLoops) {
                if (candidate.touches(selected)) {
                    touching = true;
                    break;
                }
            }
            if (!touching) {
                selectedLoops.add(candidate);
                sum += sumNonTouchingLoopProducts(
                        loops, i + 1, remaining - 1,
                        product * candidate.getGain(), selectedLoops);
                selectedLoops.remove(selectedLoops.size() - 1);
            }
        }
        return sum;
    }

    // ------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------

    public static class Path {
        private final List<DirectedEdge> edges;
        private final List<GraphVertex> vertices;
        private final Set<Integer> vertexIds;
        private final double gain;

        public Path(List<DirectedEdge> edges) {
            this.edges = new ArrayList<>(edges);
            this.vertices = new ArrayList<>();
            this.vertexIds = new HashSet<>();
            double g = 1.0;
            if (!this.edges.isEmpty()) {
                GraphVertex firstSource = this.edges.get(0).getSource();
                vertices.add(firstSource);
                vertexIds.add(firstSource.getId());
                for (DirectedEdge edge : this.edges) {
                    g *= edge.getGain();
                    vertices.add(edge.getTarget());
                    vertexIds.add(edge.getTarget().getId());
                }
            }
            this.gain = g;
        }

        public List<DirectedEdge> getEdges() { return Collections.unmodifiableList(edges); }
        public List<GraphVertex> getVertices() { return Collections.unmodifiableList(vertices); }
        public double getGain() { return gain; }

        public boolean touches(Loop loop) {
            for (Integer id : loop.getVertexIds()) {
                if (vertexIds.contains(id)) return true;
            }
            return false;
        }
    }

    public static class Loop {
        private final List<DirectedEdge> edges;
        private final Set<Integer> vertexIds;
        private final double gain;

        public Loop(List<DirectedEdge> edges) {
            this.edges = new ArrayList<>(edges);
            this.vertexIds = new HashSet<>();
            double g = 1.0;
            for (DirectedEdge edge : this.edges) {
                g *= edge.getGain();
                vertexIds.add(edge.getSource().getId());
                vertexIds.add(edge.getTarget().getId());
            }
            this.gain = g;
        }

        public List<DirectedEdge> getEdges() { return Collections.unmodifiableList(edges); }
        public Set<Integer> getVertexIds() { return Collections.unmodifiableSet(vertexIds); }
        public double getGain() { return gain; }

        public boolean touches(Loop other) {
            for (Integer id : other.vertexIds) {
                if (vertexIds.contains(id)) return true;
            }
            return false;
        }
    }
}