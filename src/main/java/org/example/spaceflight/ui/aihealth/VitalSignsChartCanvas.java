package org.example.spaceflight.ui.aihealth;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.spaceflight.model.HealthStatus;
import org.example.spaceflight.ui.shared.UIColors;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

/**
 * Compact canvas chart for a single vital sign trend.
 * Generic over Double values; line color is set at construction time.
 */
/** Canvas that draws a real-time line chart of the last N vital-sign readings for one passenger. */
public class VitalSignsChartCanvas extends Canvas {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private static final Color BG_COLOR   = Color.web("#F5F7FA");
    private static final Color WAIT_COLOR = Color.web("#B0BEC5");

    private final Color lineColor;
    private Deque<Double> history;
    private HealthStatus status;

    public VitalSignsChartCanvas(double width, double height, Color lineColor) {
        super(width, height);
        this.lineColor = lineColor;
        draw();
    }

    /** Updates chart data. statusOverride may be null to always use the fixed line color. */
    public void update(Deque<Double> history, HealthStatus statusOverride) {
        this.history = history;
        this.status  = statusOverride;
        draw();
    }

    // --- Drawing ---

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.clearRect(0, 0, w, h);
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        if (history == null || history.size() < 2) {
            gc.setFill(WAIT_COLOR);
            gc.setFont(Font.font("System", 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("—", w / 2, h / 2 + 3);
            return;
        }

        List<Double> points = new ArrayList<>(history);
        int n = points.size();

        double valMin = points.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double valMax = points.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double range  = valMax - valMin;
        if (range < 1) {
            double mid = (valMin + valMax) / 2.0;
            valMin = mid - 0.5;
            valMax = mid + 0.5;
            range  = 1;
        }
        double pad = range * 0.15;
        valMin -= pad;
        valMax += pad;
        range = valMax - valMin;

        double yPad   = 3;
        double xPad   = 2;
        double chartH = h - 2 * yPad;
        double chartW = w - 2 * xPad;

        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = xPad + (i / (double) (n - 1)) * chartW;
            double normalized = (points.get(i) - valMin) / range;
            ys[i] = yPad + (1.0 - normalized) * chartH;
        }

        Color color = resolvedColor();

        // Filled area under curve
        double[] areaXs = new double[n + 2];
        double[] areaYs = new double[n + 2];
        areaXs[0]     = xs[0];      areaYs[0]     = h - yPad;
        areaXs[n + 1] = xs[n - 1]; areaYs[n + 1] = h - yPad;
        System.arraycopy(xs, 0, areaXs, 1, n);
        System.arraycopy(ys, 0, areaYs, 1, n);
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.12));
        gc.fillPolygon(areaXs, areaYs, n + 2);

        // Line
        gc.setStroke(color);
        gc.setLineWidth(1.2);
        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) gc.lineTo(xs[i], ys[i]);
        gc.stroke();

        // Current value — tiny label top-right
        double lastVal  = points.get(n - 1);
        String valText  = (lastVal == Math.floor(lastVal)) ?
                String.valueOf((int) lastVal) :
                String.format("%.1f", lastVal);
        gc.setFill(color);
        gc.setFont(Font.font("System", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(valText, w - 3, 10);
    }

    private Color resolvedColor() {
        if (status == null) return lineColor;
        return switch (status) {
            case GREEN  -> Color.web(UIColors.HEALTH_GREEN);
            case YELLOW -> Color.web(UIColors.HEALTH_YELLOW_DARK);
            case RED    -> Color.web(UIColors.HEALTH_RED);
        };
    }
}