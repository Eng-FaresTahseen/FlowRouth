package com.ft.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.apache.commons.numbers.complex.Complex;

import java.util.Locale;

public class RootPlotPane extends Pane {

    private static final double EPS = 1e-10;
    private static final double FIT_PAD = 40;
    private static final double MIN_SPAN = 2.0;
    private static final double MARK = 7;
    private static final double LABEL_GAP = 12;

    private static final Color GRID     = Color.web("#e5e7eb");
    private static final Color AXIS     = Color.web("#374151");
    private static final Color TEXT     = Color.web("#6b7280");
    private static final Color FRAME    = Color.web("#d1d5db");
    private static final Color LHP_FILL = Color.web("#22c55e", 0.06);
    private static final Color RHP_FILL = Color.web("#ef4444", 0.08);
    private static final Color STABLE   = Color.web("#16a34a");
    private static final Color UNSTABLE = Color.web("#dc2626");
    private static final Color MARGINAL = Color.web("#d97706");

    private final Canvas canvas = new Canvas();

    private Complex[] roots = new Complex[0];
    private double originX;
    private double originY;
    private double scale = 1;

    public RootPlotPane() {
        setMinSize(0, 0);
        setPrefSize(600, 400);
        getChildren().add(canvas);
    }

    public void setRoots(Complex[] newRoots) {
        this.roots = newRoots == null ? new Complex[0] : newRoots.clone();
        draw();
    }

    public void clear() {
        setRoots(null);
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
        if (w < 10 || h < 10) return;

        updateTransform(w, h);

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        drawHalfPlanes(g, w, h);

        double step = niceStep(Math.min(w, h) / scale / 8.0);
        drawGrid(g, w, h, step);
        drawAxes(g, w, h);
        drawTickLabels(g, w, h, step);
        drawCaptions(g, w, h);
        drawRoots(g);

        g.setStroke(FRAME);
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, w - 1, h - 1);
    }

    private void updateTransform(double w, double h) {
        double xmin = 0, xmax = 0, ymin = 0, ymax = 0;
        for (Complex r : roots) {
            xmin = Math.min(xmin, r.getReal());
            xmax = Math.max(xmax, r.getReal());
            ymin = Math.min(ymin, r.getImaginary());
            ymax = Math.max(ymax, r.getImaginary());
        }

        double[] bx = expand(xmin, xmax);
        double[] by = expand(ymin, ymax);

        double availW = Math.max(w - 2 * FIT_PAD, 1);
        double availH = Math.max(h - 2 * FIT_PAD, 1);
        scale = Math.min(availW / (bx[1] - bx[0]), availH / (by[1] - by[0]));

        originX = w / 2 - (bx[0] + bx[1]) / 2 * scale;
        originY = h / 2 + (by[0] + by[1]) / 2 * scale;
    }

    private static double[] expand(double lo, double hi) {
        if (hi - lo < MIN_SPAN) {
            double mid = (lo + hi) / 2;
            lo = mid - MIN_SPAN / 2;
            hi = mid + MIN_SPAN / 2;
        }
        return new double[]{lo, hi};
    }

    private void drawHalfPlanes(GraphicsContext g, double w, double h) {
        g.setFill(LHP_FILL);
        g.fillRect(0, 0, originX, h);
        g.setFill(RHP_FILL);
        g.fillRect(originX, 0, w - originX, h);
    }

    private void drawGrid(GraphicsContext g, double w, double h, double step) {
        g.setStroke(GRID);
        g.setLineWidth(1);

        double xLo = -originX / scale;
        double xHi = (w - originX) / scale;
        double yHi = originY / scale;
        double yLo = (originY - h) / scale;

        for (long k = (long) Math.ceil(xLo / step); k <= (long) Math.floor(xHi / step); k++) {
            double px = snap(originX + k * step * scale);
            g.strokeLine(px, 0, px, h);
        }
        for (long k = (long) Math.ceil(yLo / step); k <= (long) Math.floor(yHi / step); k++) {
            double py = snap(originY - k * step * scale);
            g.strokeLine(0, py, w, py);
        }
    }

    private void drawAxes(GraphicsContext g, double w, double h) {
        g.setStroke(AXIS);
        g.setLineWidth(1.5);
        g.strokeLine(0, snap(originY), w, snap(originY));
        g.strokeLine(snap(originX), 0, snap(originX), h);
    }

    private void drawTickLabels(GraphicsContext g, double w, double h, double step) {
        g.setFont(Font.font(11));
        g.setFill(TEXT);

        double xLo = -originX / scale;
        double xHi = (w - originX) / scale;
        double yHi = originY / scale;
        double yLo = (originY - h) / scale;

        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.TOP);
        double labelY = clamp(originY + LABEL_GAP, 2, h - 16);
        for (long k = (long) Math.ceil(xLo / step); k <= (long) Math.floor(xHi / step); k++) {
            if (k == 0) continue;
            g.fillText(tick(k * step, step), originX + k * step * scale, labelY);
        }

        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.CENTER);
        double labelX = clamp(originX + LABEL_GAP, 2, w - 40);
        for (long k = (long) Math.ceil(yLo / step); k <= (long) Math.floor(yHi / step); k++) {
            if (k == 0) continue;
            g.fillText(tick(k * step, step), labelX, originY - k * step * scale);
        }
    }

    private void drawCaptions(GraphicsContext g, double w, double h) {
        g.setFont(Font.font("System", FontWeight.BOLD, 12));
        g.setTextBaseline(VPos.TOP);

        g.setFill(STABLE);
        g.setTextAlign(TextAlignment.LEFT);
        g.fillText("Left half-plane (Stable)", 10, 10);

        g.setFill(UNSTABLE);
        g.setTextAlign(TextAlignment.RIGHT);
        g.fillText("Right half-plane (Unstable)", w - 10, 10);

        g.setFill(AXIS);
        g.setTextAlign(TextAlignment.LEFT);
        g.fillText("Im (jω)", clamp(originX + LABEL_GAP, 4, w - 60), 30);

        g.setTextAlign(TextAlignment.RIGHT);
        g.setTextBaseline(VPos.BOTTOM);
        g.fillText("Re (σ)", w - 10, clamp(originY - LABEL_GAP, 20, h - 4));
    }

    private void drawRoots(GraphicsContext g) {
        g.setLineWidth(2.5);
        for (Complex r : roots) {
            double px = originX + r.getReal() * scale;
            double py = originY - r.getImaginary() * scale;

            g.setStroke(colorOf(r.getReal()));
            g.strokeLine(px - MARK, py - MARK, px + MARK, py + MARK);
            g.strokeLine(px - MARK, py + MARK, px + MARK, py - MARK);
        }
    }

    private static Color colorOf(double real) {
        if (real > EPS) return UNSTABLE;
        if (real < -EPS) return STABLE;
        return MARGINAL;
    }

    private static double niceStep(double raw) {
        double exp = Math.floor(Math.log10(raw));
        double base = Math.pow(10, exp);
        double f = raw / base;
        double nice = f < 1.5 ? 1 : f < 3.5 ? 2 : f < 7.5 ? 5 : 10;
        return nice * base;
    }

    private static String tick(double v, double step) {
        int decimals = (int) Math.max(0, -Math.floor(Math.log10(step) + 1e-9));
        return String.format(Locale.US, "%." + decimals + "f", v);
    }

    private static double snap(double v) {
        return Math.round(v) + 0.5;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
