package com.ft.view;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartStylableNode;
import com.ft.model.signalFlow.data.DirectedEdge;
import com.ft.model.signalFlow.data.GraphVertex;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignalFlowGraphPane extends BorderPane {

    private static final String PROPS_TEXT =
            "vertex.allow-user-move = true\n"
            + "vertex.radius = 18\n"
            + "vertex.use-tooltip = false\n"
            + "vertex.label = true\n"
            + "edge.use-tooltip = false\n"
            + "edge.label = true\n"
            + "edge.arrow = true\n"
            + "edge.arrow-size = 7\n";

    private static final SmartGraphProperties GRAPH_PROPS = new SmartGraphProperties(
            new ByteArrayInputStream(PROPS_TEXT.getBytes(StandardCharsets.UTF_8))
    );

    private static final URI CSS_URI;
    static {
        URI uri = null;
        try {
            uri = SignalFlowGraphPane.class.getResource("/smartgraph.css").toURI();
        } catch (Exception ignored) {
        }
        CSS_URI = uri;
    }

    private static class BranchItem {
        final int index;
        final String gainLabel;
        final boolean feedback;

        BranchItem(int index, String gainLabel, boolean feedback) {
            this.index = index;
            this.gainLabel = gainLabel;
            this.feedback = feedback;
        }

        @Override
        public String toString() {
            return gainLabel;
        }
    }

    private final DecimalFormat df =
            new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.US));

    private SmartGraphPanel<String, BranchItem> graphView;
    private boolean initialized = false;

    public SignalFlowGraphPane() {
        setMinSize(300, 220);
        setPrefSize(960, 250);
        setStyle("-fx-background-color: white; -fx-border-color: #d0d7de; -fx-border-width: 1;");
    }

    public void setAutomaticLayout(boolean value) {
        if (graphView != null && initialized) {
            graphView.setAutomaticLayout(value);
        }
    }

    public void setGraph(List<GraphVertex> vertices,
                         List<DirectedEdge> edges,
                         GraphVertex source,
                         GraphVertex target) {
        getChildren().clear();
        initialized = false;

        if (vertices == null || vertices.isEmpty()) {
            graphView = null;
            return;
        }

        Digraph<String, BranchItem> digraph = new DigraphEdgeList<>();
        List<String> orderedNames = new ArrayList<>();
        Map<Integer, Integer> orderMap = new HashMap<>();

        for (int i = 0; i < vertices.size(); i++) {
            GraphVertex v = vertices.get(i);
            digraph.insertVertex(v.getName());
            orderedNames.add(v.getName());
            orderMap.put(v.getId(), i);
        }

        List<Edge<BranchItem, String>> createdEdges = new ArrayList<>();
        for (int i = 0; i < edges.size(); i++) {
            DirectedEdge e = edges.get(i);
            int i1 = orderMap.getOrDefault(e.getSource().getId(), 0);
            int i2 = orderMap.getOrDefault(e.getTarget().getId(), 0);
            boolean isFeedback = i2 < i1;
            BranchItem item = new BranchItem(i + 1, df.format(e.getGain()), isFeedback);
            Edge<BranchItem, String> inserted = digraph.insertEdge(
                    e.getSource().getName(),
                    e.getTarget().getName(),
                    item
            );
            createdEdges.add(inserted);
        }

        SmartPlacementStrategy strategy = new SmartPlacementStrategy() {
            @Override
            public <V, E> void place(double width, double height, SmartGraphPanel<V, E> panel) {
                Map<String, Vertex<V>> map = new HashMap<>();
                for (Vertex<V> v : panel.getModel().vertices()) {
                    map.put(String.valueOf(v.element()), v);
                }
                int n = orderedNames.size();
                double w = Math.max(width, 600);
                double h = Math.max(height, 220);

                for (int i = 0; i < n; i++) {
                    Vertex<V> vx = map.get(orderedNames.get(i));
                    if (vx == null) continue;
                    double x = (n == 1) ? w * 0.5 : w * (0.07 + (0.86 * i) / (n - 1));
                    double yOffset = 0;
                    if (i > 0 && i < n - 1) {
                        yOffset = (i % 2 == 1) ? -h * 0.14 : h * 0.14;
                    }
                    double y = h * 0.50 + yOffset;
                    panel.setVertexPosition(vx, x, y);
                }
            }
        };

        SmartGraphPanel<String, BranchItem> panel = new SmartGraphPanel<>(digraph, GRAPH_PROPS, strategy, CSS_URI);
        this.graphView = panel;
        setCenter(panel);

        Runnable scheduleInit = () -> Platform.runLater(() -> {
            if (!initialized && panel == this.graphView
                    && panel.getScene() != null
                    && panel.getWidth() > 0
                    && panel.getHeight() > 0) {
                initialized = true;
                panel.init();
                applyCustomStyles(source, target, createdEdges);
            }
        });

        panel.sceneProperty().addListener((obs, oldV, newV) -> scheduleInit.run());
        panel.widthProperty().addListener((obs, oldV, newV) -> scheduleInit.run());
        panel.heightProperty().addListener((obs, oldV, newV) -> scheduleInit.run());
        scheduleInit.run();
    }

    private void applyCustomStyles(GraphVertex source,
                                   GraphVertex target,
                                   List<Edge<BranchItem, String>> createdEdges) {
        if (graphView == null) return;
        if (source != null) {
            SmartStylableNode srcNode = graphView.getStylableVertex(source.getName());
            if (srcNode != null) srcNode.setStyleClass("vertex-source");
        }
        if (target != null && (source == null || source.getId() != target.getId())) {
            SmartStylableNode dstNode = graphView.getStylableVertex(target.getName());
            if (dstNode != null) dstNode.setStyleClass("vertex-target");
        }
        for (Edge<BranchItem, String> edge : createdEdges) {
            if (edge.element().feedback) {
                SmartStylableNode edgeNode = graphView.getStylableEdge(edge);
                if (edgeNode != null) edgeNode.setStyleClass("edge-feedback");
            }
        }
    }
}
