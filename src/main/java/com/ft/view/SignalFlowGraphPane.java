package com.ft.view;

import com.ft.model.signalFlow.data.DirectedEdge;
import com.ft.model.signalFlow.data.GraphVertex;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignalFlowGraphPane extends Pane {

    private static final double NODE_RADIUS = 19.0;
    private static final double ARROW_SIZE = 8.5;

    private final DecimalFormat df =
            new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.US));

    private final Canvas canvas = new Canvas();

    private List<GraphVertex> vertices = new ArrayList<>();
    private List<DirectedEdge> edges = new ArrayList<>();
    private GraphVertex sourceVertex;
    private GraphVertex targetVertex;

    private final Map<Integer, double[]> nodePositions = new HashMap<>();

    public SignalFlowGraphPane() {
        setMinSize(300, 220);
        setPrefSize(900, 250);
        getChildren().add(canvas);
    }

    public void setGraph(List<GraphVertex> vertices,
                         List<DirectedEdge> edges,
                         GraphVertex source,
                         GraphVertex target) {
        this.vertices = new ArrayList<>(vertices);
        this.edges = new ArrayList<>(edges);
        this.sourceVertex = source;
        this.targetVertex = target;
        layoutNodes();
        draw();
    }

    private void layoutNodes() {
        nodePositions.clear();
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            double x = (n == 1) ? 0.5 : 0.08 + (0.84 * i) / (n - 1);
            nodePositions.put(vertices.get(i).getId(), new double[]{x, 0.5});
        }
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        draw();
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w < 20 || h < 20) return;

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        for (DirectedEdge e : edges) {
            drawStraightEdge(g, e, w, h);
        }

        for (GraphVertex v : vertices) {
            drawVertex(g, v, w, h);
        }

        g.setStroke(Color.web("#d1d5db"));
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, w - 1, h - 1);
    }

    private void drawStraightEdge(GraphicsContext g, DirectedEdge edge, double w, double h) {
        double[] p1 = getPos(edge.getSource().getId(), w, h);
        double[] p2 = getPos(edge.getTarget().getId(), w, h);

        double dx = p2[0] - p1[0];
        double dy = p2[1] - p1[1];
        double len = Math.hypot(dx, dy);
        if (len < 1e-3) return;

        double ux = dx / len;
        double uy = dy / len;

        double x1 = p1[0] + ux * NODE_RADIUS;
        double y1 = p1[1] + uy * NODE_RADIUS;
        double x2 = p2[0] - ux * NODE_RADIUS;
        double y2 = p2[1] - uy * NODE_RADIUS;

        g.setStroke(Color.web("#0284c7"));
        g.setLineWidth(2.0);
        g.strokeLine(x1, y1, x2, y2);

        drawArrow(g, x2, y2, Math.atan2(dy, dx), Color.web("#0284c7"));

        g.setFill(Color.web("#111827"));
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.BOTTOM);
        g.fillText(df.format(edge.getGain()), (x1 + x2) / 2, (y1 + y2) / 2 - 4);
    }

    private void drawArrow(GraphicsContext g, double tipX, double tipY, double angle, Color color) {
        double a1 = angle - Math.toRadians(150);
        double a2 = angle + Math.toRadians(150);
        double[] xs = {tipX, tipX + Math.cos(a1) * ARROW_SIZE, tipX + Math.cos(a2) * ARROW_SIZE};
        double[] ys = {tipY, tipY + Math.sin(a1) * ARROW_SIZE, tipY + Math.sin(a2) * ARROW_SIZE};
        g.setFill(color);
        g.fillPolygon(xs, ys, 3);
    }

    private void drawVertex(GraphicsContext g, GraphVertex v, double w, double h) {
        double[] p = getPos(v.getId(), w, h);

        boolean isSrc = sourceVertex != null && sourceVertex.getId() == v.getId();
        boolean isDst = targetVertex != null && targetVertex.getId() == v.getId();

        g.setFill(isSrc ? Color.web("#dcfce7") : isDst ? Color.web("#dbeafe") : Color.web("#f8fafc"));
        g.fillOval(p[0] - NODE_RADIUS, p[1] - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        g.setStroke(isSrc ? Color.web("#16a34a") : isDst ? Color.web("#2563eb") : Color.web("#374151"));
        g.setLineWidth(2.0);
        g.strokeOval(p[0] - NODE_RADIUS, p[1] - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        g.setFill(Color.web("#111827"));
        g.setFont(Font.font("System", FontWeight.BOLD, 13));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText(v.getName(), p[0], p[1]);
    }

    private double[] getPos(int id, double w, double h) {
        double[] n = nodePositions.getOrDefault(id, new double[]{0.5, 0.5});
        return new double[]{n[0] * w, n[1] * h};
    }
}
