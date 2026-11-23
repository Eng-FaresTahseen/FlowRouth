package com.ft.view;

import com.ft.model.signalFlow.data.DirectedEdge;
import com.ft.model.signalFlow.data.GraphVertex;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
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
    private Integer draggedId = null;

    public SignalFlowGraphPane() {
        setMinSize(300, 220);
        setPrefSize(900, 250);
        getChildren().add(canvas);

        canvas.setOnMousePressed(this::onPressed);
        canvas.setOnMouseDragged(this::onDragged);
        canvas.setOnMouseReleased(e -> draggedId = null);
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

        Map<Integer, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            indexMap.put(vertices.get(i).getId(), i);
        }

        for (DirectedEdge e : edges) {
            drawEdge(g, e, indexMap, w, h);
        }

        for (GraphVertex v : vertices) {
            drawVertex(g, v, w, h);
        }

        g.setStroke(Color.web("#d1d5db"));
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, w - 1, h - 1);
    }

    private void drawEdge(GraphicsContext g,
                          DirectedEdge edge,
                          Map<Integer, Integer> indexMap,
                          double w,
                          double h) {
        int id1 = edge.getSource().getId();
        int id2 = edge.getTarget().getId();
        double[] p1 = getPos(id1, w, h);
        double[] p2 = getPos(id2, w, h);

        if (id1 == id2) {
            drawSelfLoop(g, p1[0], p1[1], edge.getGain());
            return;
        }

        int i1 = indexMap.getOrDefault(id1, 0);
        int i2 = indexMap.getOrDefault(id2, 0);
        boolean forward = i2 > i1;
        int span = Math.abs(i2 - i1);

        Color color = forward ? Color.web("#0284c7") : Color.web("#dc2626");
        g.setStroke(color);
        g.setLineWidth(2.0);

        if (forward && span == 1) {
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

            g.strokeLine(x1, y1, x2, y2);
            drawArrow(g, x2, y2, Math.atan2(dy, dx), color);
            drawGainLabel(g, (x1 + x2) / 2, (y1 + y2) / 2 - 12, df.format(edge.getGain()), color);
            return;
        }

        double midX = (p1[0] + p2[0]) / 2.0;
        double midY = (p1[1] + p2[1]) / 2.0;
        double sign = forward ? -1.0 : 1.0;
        double arc = Math.min(h * 0.40, 40.0 + span * 22.0);

        double ctrlX = midX;
        double ctrlY = Math.max(22, Math.min(h - 22, midY + sign * arc * 1.3));

        double aStart = Math.atan2(ctrlY - p1[1], ctrlX - p1[0]);
        double aEnd = Math.atan2(p2[1] - ctrlY, p2[0] - ctrlX);

        double x1 = p1[0] + Math.cos(aStart) * NODE_RADIUS;
        double y1 = p1[1] + Math.sin(aStart) * NODE_RADIUS;
        double x2 = p2[0] - Math.cos(aEnd) * NODE_RADIUS;
        double y2 = p2[1] - Math.sin(aEnd) * NODE_RADIUS;

        g.beginPath();
        g.moveTo(x1, y1);
        g.quadraticCurveTo(ctrlX, ctrlY, x2, y2);
        g.stroke();

        double topX = 0.25 * x1 + 0.5 * ctrlX + 0.25 * x2;
        double topY = 0.25 * y1 + 0.5 * ctrlY + 0.25 * y2;
        double tan = Math.atan2(y2 - y1, x2 - x1);

        drawArrow(g, x2, y2, aEnd, color);
        drawArrow(g, topX + Math.cos(tan) * 4, topY + Math.sin(tan) * 4, tan, color);
        drawGainLabel(g, topX, topY + (forward ? -13 : 13), df.format(edge.getGain()), color);
    }

    private void drawSelfLoop(GraphicsContext g, double cx, double cy, double gain) {
        Color color = Color.web("#7c3aed");
        g.setStroke(color);
        g.setLineWidth(2.0);

        double r = 17.0;
        double loopY = cy - NODE_RADIUS - r + 3;
        g.strokeOval(cx - r, loopY - r, r * 2, r * 2);

        drawArrow(g, cx + r * 0.7, cy - NODE_RADIUS - 3, Math.toRadians(115), color);
        drawGainLabel(g, cx, loopY - r - 10, df.format(gain), color);
    }

    private void drawArrow(GraphicsContext g, double tipX, double tipY, double angle, Color color) {
        double a1 = angle - Math.toRadians(150);
        double a2 = angle + Math.toRadians(150);
        double[] xs = {tipX, tipX + Math.cos(a1) * ARROW_SIZE, tipX + Math.cos(a2) * ARROW_SIZE};
        double[] ys = {tipY, tipY + Math.sin(a1) * ARROW_SIZE, tipY + Math.sin(a2) * ARROW_SIZE};
        g.setFill(color);
        g.fillPolygon(xs, ys, 3);
    }

    private void drawGainLabel(GraphicsContext g, double x, double y, String text, Color color) {
        double bw = Math.max(22, text.length() * 7.5 + 10);
        double bh = 17;

        g.setFill(Color.WHITE);
        g.fillRoundRect(x - bw / 2, y - bh / 2, bw, bh, 6, 6);
        g.setStroke(color);
        g.setLineWidth(1.0);
        g.strokeRoundRect(x - bw / 2, y - bh / 2, bw, bh, 6, 6);

        g.setFill(Color.web("#111827"));
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText(text, x, y);
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

    private void onPressed(MouseEvent e) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        for (GraphVertex v : vertices) {
            double[] p = getPos(v.getId(), w, h);
            if (Math.hypot(e.getX() - p[0], e.getY() - p[1]) <= NODE_RADIUS + 4) {
                draggedId = v.getId();
                return;
            }
        }
    }

    private void onDragged(MouseEvent e) {
        if (draggedId == null) return;
        double w = Math.max(canvas.getWidth(), 1);
        double h = Math.max(canvas.getHeight(), 1);
        double nx = Math.max(0.05, Math.min(0.95, e.getX() / w));
        double ny = Math.max(0.12, Math.min(0.88, e.getY() / h));
        nodePositions.put(draggedId, new double[]{nx, ny});
        draw();
    }
}
