package com.ft.controller;

import com.ft.model.signalFlow.data.DirectedEdge;
import com.ft.model.signalFlow.data.GraphVertex;
import com.ft.model.signalFlow.data.SignalFlowGraph;
import com.ft.model.signalFlow.data.SignalFlowGraph.Loop;
import com.ft.model.signalFlow.data.SignalFlowGraph.Path;
import com.ft.view.SignalFlowView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignalFlowController {

    private static final double EPS = 1e-10;

    private final SignalFlowView view;
    private final DecimalFormat df =
            new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.US));

    public SignalFlowController(SignalFlowView view) {
        this.view = view;
        this.view.setOnCalculate(this::onCalculate);
        this.view.setOnAddBranch(this::onAddBranch);
        this.view.setOnClear(this::onClear);

        this.view.setInputText(
                "R->x1:1, x1->x2:2, x2->x3:3, x3->x4:4, x4->x5:5, x5->C:1, "
                + "x1->x4:10, x2->x1:-2, x3->x2:-1, x5->x4:-3");
        onCalculate();
    }

    private void onAddBranch() {
        String from = view.getFromText();
        String to = view.getToText();
        String gainStr = view.getGainText();

        if (from == null || from.isBlank() || to == null || to.isBlank() || gainStr == null || gainStr.isBlank()) {
            view.showError("Please enter From node, To node, and numerical Gain.");
            return;
        }

        double gain;
        try {
            gain = Double.parseDouble(gainStr.trim());
        } catch (NumberFormatException ex) {
            view.showError("Invalid gain: " + gainStr);
            return;
        }

        String entry = from.trim() + "->" + to.trim() + ":" + fmt(gain);
        String current = view.getInputText();
        if (current == null || current.isBlank()) {
            view.setInputText(entry);
        } else {
            view.setInputText(current.trim() + ", " + entry);
        }

        view.clearBranchFields();
        onCalculate();
    }

    private void onClear() {
        view.setInputText("");
        view.clearBranchFields();
        view.updateNodes(List.of(), null, null);
        view.getGraphPane().setGraph(List.of(), List.of(), null, null);
        view.showPaths(List.of());
        view.showLoops(List.of());
        view.showNonTouching(List.of(), "");
        view.showDeterminants(List.of(), "");
        view.showMason(List.of(), "", false);
        view.setBadges("T = —", false, "Δ = —", false, 0, 0);
    }

    private void onCalculate() {
        String text = view.getInputText();
        if (text == null || text.isBlank()) {
            view.showError("Please enter the graph branches.");
            return;
        }

        SignalFlowGraph graph = new SignalFlowGraph();
        List<String> nodeNames = new ArrayList<>();
        Map<DirectedEdge, String> edgeNames = new HashMap<>();

        String[] parts = text.trim().split("\\s*[,;]\\s*");
        int edgeIndex = 1;

        for (String part : parts) {
            if (part.isBlank()) continue;
            String[] arrow = part.split("\\s*->\\s*");
            if (arrow.length != 2) {
                view.showError("Invalid branch format: " + part);
                return;
            }
            String uName = arrow[0].trim();
            String[] colon = arrow[1].split("\\s*:\\s*");
            if (colon.length != 2) {
                view.showError("Missing branch gain in: " + part);
                return;
            }
            String vName = colon[0].trim();
            double gain;
            try {
                gain = Double.parseDouble(colon[1].trim());
            } catch (NumberFormatException ex) {
                view.showError("Invalid number in: " + part);
                return;
            }

            GraphVertex u = graph.getVertexByName(uName);
            if (u == null) {
                u = new GraphVertex(uName, nodeNames.size() + 1);
                graph.addVertex(u);
                nodeNames.add(uName);
            }

            GraphVertex v = graph.getVertexByName(vName);
            if (v == null) {
                v = new GraphVertex(vName, nodeNames.size() + 1);
                graph.addVertex(v);
                nodeNames.add(vName);
            }

            DirectedEdge edge = new DirectedEdge(u, v, gain);
            graph.addEdge(edge);
            edgeNames.put(edge, "w" + edgeIndex++);
        }

        String prevSrc = view.getSelectedSource();
        String prevDst = view.getSelectedTarget();
        view.updateNodes(nodeNames, prevSrc, prevDst);

        GraphVertex src = graph.getVertexByName(view.getSelectedSource());
        GraphVertex dst = graph.getVertexByName(view.getSelectedTarget());

        view.showError("");
        view.getGraphPane().setGraph(graph.getVertices(), graph.getEdges(), src, dst);

        if (src == null || dst == null) return;

        List<Path> paths = graph.findForwardPaths(src, dst);
        List<String[]> pathRows = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            Path p = paths.get(i);
            String name = "M" + (i + 1);
            pathRows.add(new String[]{
                    name,
                    formatVertices(p.getVertices()),
                    formatProduct(name, p.getEdges(), edgeNames, p.getGain()),
                    fmt(p.getGain())
            });
        }
        view.showPaths(pathRows);

        List<Loop> loops = graph.findLoops();
        List<String[]> loopRows = new ArrayList<>();
        for (int i = 0; i < loops.size(); i++) {
            Loop l = loops.get(i);
            String name = "L" + (i + 1);
            loopRows.add(new String[]{
                    name,
                    formatLoopVertices(l.getEdges()),
                    formatProduct(name, l.getEdges(), edgeNames, l.getGain()),
                    fmt(l.getGain())
            });
        }
        view.showLoops(loopRows);

        Map<Integer, List<List<Integer>>> combosByOrder = findNonTouchingCombos(loops);
        List<String[]> ntRows = new ArrayList<>();
        StringBuilder ntNote = new StringBuilder();

        double sum1 = 0;
        for (Loop l : loops) sum1 += l.getGain();
        ntNote.append("Sum of individual loop gains: Σ(L) = ").append(fmt(sum1));

        for (Map.Entry<Integer, List<List<Integer>>> entry : combosByOrder.entrySet()) {
            int k = entry.getKey();
            double sumK = 0;
            for (List<Integer> idxs : entry.getValue()) {
                double prod = 1.0;
                List<String> lNames = new ArrayList<>();
                List<String> lNodes = new ArrayList<>();
                List<String> lVals = new ArrayList<>();
                for (int idx : idxs) {
                    Loop l = loops.get(idx);
                    prod *= l.getGain();
                    lNames.add("L" + (idx + 1));
                    lNodes.add("{" + formatUniqueNodes(l) + "}");
                    lVals.add(paren(l.getGain()));
                }
                sumK += prod;
                String sub = String.join(" × ", lNames) + " = " + String.join(" × ", lVals) + " = " + fmt(prod);
                ntRows.add(new String[]{
                        k + " Non-Touching",
                        String.join(", ", lNames),
                        String.join(" , ", lNodes),
                        sub,
                        fmt(prod)
                });
            }
            ntNote.append("   |   Sum of ").append(k).append(" non-touching products = ").append(fmt(sumK));
        }
        if (combosByOrder.isEmpty()) {
            ntNote.append("   |   No combinations of 2 or more non-touching loops.");
        }
        view.showNonTouching(ntRows, ntNote.toString());

        double delta = graph.calculateDelta(loops);
        List<String[]> deltaRows = new ArrayList<>();
        deltaRows.add(new String[]{
                "Δ (Graph Determinant)",
                loops.isEmpty() ? "None" : allLoopNames(loops.size()),
                buildDeltaExpression("Δ", loops, combosByOrder, delta),
                fmt(delta)
        });

        double[] deltaI = new double[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            Path p = paths.get(i);
            List<Loop> subLoops = new ArrayList<>();
            List<Integer> keptIdx = new ArrayList<>();
            for (int j = 0; j < loops.size(); j++) {
                if (!p.touches(loops.get(j))) {
                    subLoops.add(loops.get(j));
                    keptIdx.add(j);
                }
            }
            deltaI[i] = graph.calculateDelta(subLoops);
            String dName = "Δ" + (i + 1) + " (for M" + (i + 1) + ")";
            String keptStr = keptIdx.isEmpty() ? "None (all loops touch M" + (i + 1) + ")" : formatIdxNames(keptIdx);
            deltaRows.add(new String[]{
                    dName,
                    keptStr,
                    buildCofactorExpression("Δ" + (i + 1), keptIdx, subLoops, deltaI[i]),
                    fmt(deltaI[i])
            });
        }
        view.showDeterminants(deltaRows,
                "Formula: Δ = 1 − Σ(loop gains) + Σ(2 non-touching products) − Σ(3 non-touching products) + ...   |   Δ = " + fmt(delta));

        List<String[]> masonRows = new ArrayList<>();
        double numerator = 0;
        List<String> symSum = new ArrayList<>();
        List<String> numSum = new ArrayList<>();

        for (int i = 0; i < paths.size(); i++) {
            double mi = paths.get(i).getGain();
            double di = deltaI[i];
            double prod = mi * di;
            numerator += prod;

            String termName = "M" + (i + 1) + " · Δ" + (i + 1);
            symSum.add("M" + (i + 1) + "Δ" + (i + 1));
            numSum.add(paren(prod));

            masonRows.add(new String[]{
                    "Path Term " + (i + 1),
                    termName,
                    fmt(mi) + " × " + fmt(di),
                    fmt(prod)
            });
        }

        masonRows.add(new String[]{
                "Numerator",
                symSum.isEmpty() ? "0" : String.join(" + ", symSum),
                numSum.isEmpty() ? "0" : String.join(" + ", numSum),
                fmt(numerator)
        });

        masonRows.add(new String[]{
                "Denominator",
                "Δ",
                buildDeltaExpression("Δ", loops, combosByOrder, delta),
                fmt(delta)
        });

        double tf = graph.calculateTransferFunction(src, dst);
        boolean valid = !paths.isEmpty() && Math.abs(delta) > EPS && Double.isFinite(tf);
        String finalStr = valid ? formatCompactTransfer(numerator, delta, tf) : (paths.isEmpty() ? "0" : "Undefined");

        masonRows.add(new String[]{
                "Transfer Function T",
                "(" + (symSum.isEmpty() ? "0" : String.join(" + ", symSum)) + ") / Δ",
                fmt(numerator) + " / " + fmt(delta),
                finalStr
        });

        String ratio = "T = " + dst.getName() + " / " + src.getName();
        view.showMason(masonRows,
                "Mason's Gain Formula:  " + ratio + " = ("
                + (symSum.isEmpty() ? "0" : String.join(" + ", symSum))
                + ") / Δ = " + fmt(numerator) + " / " + fmt(delta) + " = " + finalStr,
                valid);

        view.setBadges(
                ratio + " = " + finalStr, valid,
                "Δ = " + fmt(delta), Math.abs(delta) > EPS,
                paths.size(), loops.size());
    }

    private Map<Integer, List<List<Integer>>> findNonTouchingCombos(List<Loop> loops) {
        Map<Integer, List<List<Integer>>> map = new LinkedHashMap<>();
        for (int k = 2; k <= loops.size(); k++) {
            List<List<Integer>> list = new ArrayList<>();
            backtrackCombos(loops, k, 0, new ArrayList<>(), list);
            if (!list.isEmpty()) {
                map.put(k, list);
            } else {
                break;
            }
        }
        return map;
    }

    private void backtrackCombos(List<Loop> loops,
                                 int k,
                                 int start,
                                 List<Integer> current,
                                 List<List<Integer>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        int rem = k - current.size();
        for (int i = start; i <= loops.size() - rem; i++) {
            Loop cand = loops.get(i);
            boolean touches = false;
            for (int idx : current) {
                if (cand.touches(loops.get(idx))) {
                    touches = true;
                    break;
                }
            }
            if (!touches) {
                current.add(i);
                backtrackCombos(loops, k, i + 1, current, result);
                current.remove(current.size() - 1);
            }
        }
    }

    private String buildDeltaExpression(String sym,
                                        List<Loop> loops,
                                        Map<Integer, List<List<Integer>>> combos,
                                        double val) {
        if (loops.isEmpty()) return sym + " = 1";

        StringBuilder s = new StringBuilder(sym + " = 1");
        StringBuilder n = new StringBuilder("1");

        List<String> lSyms = new ArrayList<>();
        List<String> lNums = new ArrayList<>();
        for (int i = 0; i < loops.size(); i++) {
            lSyms.add("L" + (i + 1));
            lNums.add(paren(loops.get(i).getGain()));
        }
        s.append(" − (").append(String.join(" + ", lSyms)).append(")");
        n.append(" − (").append(String.join(" + ", lNums)).append(")");

        for (Map.Entry<Integer, List<List<Integer>>> entry : combos.entrySet()) {
            int k = entry.getKey();
            String sign = (k % 2 == 0) ? " + " : " − ";
            List<String> cSyms = new ArrayList<>();
            List<String> cNums = new ArrayList<>();
            for (List<Integer> idxs : entry.getValue()) {
                List<String> names = new ArrayList<>();
                double p = 1.0;
                for (int idx : idxs) {
                    names.add("L" + (idx + 1));
                    p *= loops.get(idx).getGain();
                }
                cSyms.add(String.join("·", names));
                cNums.add(paren(p));
            }
            s.append(sign).append("(").append(String.join(" + ", cSyms)).append(")");
            n.append(sign).append("(").append(String.join(" + ", cNums)).append(")");
        }
        return s + " = " + n + " = " + fmt(val);
    }

    private String buildCofactorExpression(String sym,
                                           List<Integer> origIdx,
                                           List<Loop> subLoops,
                                           double val) {
        if (subLoops.isEmpty()) return sym + " = 1";

        List<String> lSyms = new ArrayList<>();
        List<String> lNums = new ArrayList<>();
        for (int i = 0; i < subLoops.size(); i++) {
            lSyms.add("L" + (origIdx.get(i) + 1));
            lNums.add(paren(subLoops.get(i).getGain()));
        }

        StringBuilder s = new StringBuilder(sym + " = 1 − (" + String.join(" + ", lSyms) + ")");
        StringBuilder n = new StringBuilder("1 − (" + String.join(" + ", lNums) + ")");

        Map<Integer, List<List<Integer>>> subCombos = findNonTouchingCombos(subLoops);
        for (Map.Entry<Integer, List<List<Integer>>> entry : subCombos.entrySet()) {
            int k = entry.getKey();
            String sign = (k % 2 == 0) ? " + " : " − ";
            List<String> cSyms = new ArrayList<>();
            List<String> cNums = new ArrayList<>();
            for (List<Integer> localList : entry.getValue()) {
                List<String> names = new ArrayList<>();
                double p = 1.0;
                for (int loc : localList) {
                    names.add("L" + (origIdx.get(loc) + 1));
                    p *= subLoops.get(loc).getGain();
                }
                cSyms.add(String.join("·", names));
                cNums.add(paren(p));
            }
            s.append(sign).append("(").append(String.join(" + ", cSyms)).append(")");
            n.append(sign).append("(").append(String.join(" + ", cNums)).append(")");
        }
        return s + " = " + n + " = " + fmt(val);
    }

    private String formatCompactTransfer(double num, double den, double ratio) {
        long rNum = Math.round(num * 1000);
        long rDen = Math.round(den * 1000);
        if (Math.abs(num * 1000 - rNum) < 1e-4 && Math.abs(den * 1000 - rDen) < 1e-4 && rDen != 0) {
            long g = gcd(Math.abs(rNum), Math.abs(rDen));
            long a = rNum / g;
            long b = rDen / g;
            if (b < 0) {
                a = -a;
                b = -b;
            }
            if (b == 1) return String.valueOf(a);
            return a + "/" + b + " (" + fmt(ratio) + ")";
        }
        return fmt(ratio);
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a == 0 ? 1 : a;
    }

    private String formatProduct(String label,
                                 List<DirectedEdge> edges,
                                 Map<DirectedEdge, String> edgeNames,
                                 double gain) {
        List<String> syms = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        for (DirectedEdge e : edges) {
            syms.add(edgeNames.getOrDefault(e, "w"));
            vals.add(paren(e.getGain()));
        }
        return label + " = " + String.join(" × ", syms)
                + " = " + String.join(" × ", vals)
                + " = " + fmt(gain);
    }

    private static String allLoopNames(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) list.add("L" + i);
        return String.join(", ", list);
    }

    private static String formatIdxNames(List<Integer> idxs) {
        List<String> list = new ArrayList<>();
        for (int idx : idxs) list.add("L" + (idx + 1));
        return String.join(", ", list);
    }

    private static String formatUniqueNodes(Loop l) {
        List<String> list = new ArrayList<>();
        for (DirectedEdge e : l.getEdges()) {
            String n = e.getSource().getName();
            if (!list.contains(n)) list.add(n);
        }
        return String.join(", ", list);
    }

    private static String formatVertices(List<GraphVertex> list) {
        List<String> names = new ArrayList<>();
        for (GraphVertex v : list) names.add(v.getName());
        return String.join(" → ", names);
    }

    private static String formatLoopVertices(List<DirectedEdge> edges) {
        if (edges.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        names.add(edges.get(0).getSource().getName());
        for (DirectedEdge e : edges) names.add(e.getTarget().getName());
        return String.join(" → ", names);
    }

    private String paren(double v) {
        String s = fmt(v);
        return v < 0 ? "(" + s + ")" : s;
    }

    private String fmt(double v) {
        if (Math.abs(v) < EPS) return "0";
        return df.format(v);
    }
}
