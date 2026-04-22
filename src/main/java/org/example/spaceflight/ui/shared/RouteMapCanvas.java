package org.example.spaceflight.ui.shared;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/** Draws the animated flight route on a JavaFX Canvas, showing the shuttle position and waypoints. */
public class RouteMapCanvas extends Canvas {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    // Earth sits at the bottom-center of the canvas
    private static final double EARTH_CX = 0.50;
    private static final double EARTH_CY = 1.45;
    // Earth radius as fraction of min(w,h)
    private static final double EARTH_R = 0.8;

    // Route: both endpoints sit on the top edge of the Earth circle.
    // The arc goes up to space and comes back down symmetrically.
    private static final double[][] WAYPOINTS = {
            {0.22, 0.82},   // Launch point – top-left of Earth surface
            {0.25, 0.55},   // Ascent waypoint
            {0.35, 0.22},   // Ascent high
            {0.50, 0.10},   // Orbit peak (center-top)
            {0.65, 0.22},   // Descent high
            {0.75, 0.55},   // Descent waypoint
            {0.78, 0.82}    // Landing point – top-right of Earth surface
    };

    private double routeProgress = 0.0;
    private boolean emergencyLanding = false;
    private boolean emergencyLatched = false;  // true once start/target are set
    private boolean emergencyLanded = false;   // true once shuttle has reached the surface
    private double emergencyProgress = 0.0;
    private double emergencyStartX = 0;
    private double emergencyStartY = 0;
    // Nearest point on the Earth surface — latched once when emergency activates
    private double emergencyTargetX = 0;
    private double emergencyTargetY = 0;

    private final Image earthImage;

    public RouteMapCanvas(double width, double height) {
        super(width, height);
        Image img = null;
        try {
            var stream = RouteMapCanvas.class.getResourceAsStream(
                    "/org/example/spaceflight/images/earth.jpg");
            if (stream != null) {
                img = new Image(stream);
            }
        } catch (Exception e) {
            log.warning("Could not load earth.jpg: " + e.getMessage());
        }
        earthImage = img;
        draw();
    }

    public void update(double routeProgress, boolean emergencyLanding, double emergencyProgress) {
        // Once landed, ignore all further state updates — shuttle stays on Earth
        if (emergencyLanded) {
            draw();
            return;
        }

        this.routeProgress = Math.max(0, Math.min(1, routeProgress));
        this.emergencyLanding = emergencyLanding;
        this.emergencyProgress = Math.max(0, Math.min(1, emergencyProgress));

        // Latch shuttle position and nearest Earth surface point — only once per emergency
        if (emergencyLanding && !emergencyLatched) {
            emergencyLatched = true;
            double[] pos = interpolatePosition(routeProgress, getWidth(), getHeight());
            emergencyStartX = pos[0];
            emergencyStartY = pos[1];

            double[] target = nearestEarthSurfacePoint(pos[0], pos[1], getWidth(), getHeight());
            emergencyTargetX = target[0];
            emergencyTargetY = target[1];
            log.info(String.format("Emergency latched: shuttle=(%.1f,%.1f) target=(%.1f,%.1f)",
                    emergencyStartX, emergencyStartY, emergencyTargetX, emergencyTargetY));
        }

        // Once landed, set flag — checked at top of next call
        if (this.emergencyProgress >= 1.0 && emergencyLatched) {
            emergencyLanded = true;
        }

        draw();
    }

    public void update(double routeProgress) {
        update(routeProgress, false, 0);
    }

    public double getRouteProgress() {
        return routeProgress;
    }

    // -------------------------------------------------------------------------

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // Background — pure black space
        gc.setFill(Color.BLACK);
        gc.fillRoundRect(0, 0, w, h, 12, 12);

        drawBackgroundStars(gc, w, h);
        drawEarth(gc, w, h);
        drawRouteLine(gc, w, h);
        drawWaypointStars(gc, w, h);

        if (emergencyLanded) {
            // Shuttle stays fixed on the landing point after touchdown
            drawEmergencyRoute(gc);
            drawShuttleDot(gc, emergencyTargetX, emergencyTargetY, Color.web("#FF1744"), Color.web("#FF8A80"));
        } else if (emergencyLanding) {
            drawEmergencyRoute(gc);
            drawShuttleOnEmergencyRoute(gc);
        } else {
            drawShuttle(gc, w, h);
        }
    }

    private void drawBackgroundStars(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.web("#ffffff", 0.35));
        int seed = 42;
        for (int i = 0; i < 35; i++) {
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            double sx = (seed % 1000) / 1000.0 * w;
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            double sy = (seed % 1000) / 1000.0 * (h * 0.75); // keep stars out of Earth area
            double size = 1.0 + (i % 3) * 0.5;
            gc.fillOval(sx, sy, size, size);
        }
    }

    private void drawEarth(GraphicsContext gc, double w, double h) {
        double radius = Math.min(w, h) * EARTH_R;
        double cx = EARTH_CX * w;
        double cy = EARTH_CY * h;

        // Clip to circle, draw image inside it
        gc.save();
        gc.beginPath();
        gc.arc(cx, cy, radius, radius, 0, 360);
        gc.closePath();
        gc.clip();

        if (earthImage != null && !earthImage.isError()) {
            // zoom < 1 scales image larger than clip circle → black border of earth.jpg falls outside
            double zoom = 0.85;
            double d = radius * 2 / zoom;
            gc.drawImage(earthImage, cx - d / 2, cy - d / 2, d, d);
        } else {
            // Fallback gradient if image missing
            gc.setFill(Color.web("#1565C0"));
            gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }

        gc.restore();
    }

    private void drawRouteLine(GraphicsContext gc, double w, double h) {
        gc.setStroke(Color.web("#90CAF9", 0.65));
        gc.setLineWidth(1.8);
        gc.setLineDashes(6, 4);

        gc.beginPath();
        for (int i = 0; i < WAYPOINTS.length; i++) {
            double x = WAYPOINTS[i][0] * w;
            double y = WAYPOINTS[i][1] * h;
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
        gc.setLineDashes();
    }

    private void drawWaypointStars(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.GOLD);
        // Stars only at the three orbit points (indices 2, 3, 4)
        for (int i = 2; i <= 4; i++) {
            drawStar(gc, WAYPOINTS[i][0] * w, WAYPOINTS[i][1] * h, 5);
        }
    }

    private void drawStar(GraphicsContext gc, double cx, double cy, double size) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? size : size * 0.4;
            xs[i] = cx + r * Math.cos(angle);
            ys[i] = cy - r * Math.sin(angle);
        }
        gc.fillPolygon(xs, ys, 10);
    }

    private void drawEmergencyRoute(GraphicsContext gc) {
        gc.setStroke(Color.web("#FF1744"));
        gc.setLineWidth(2.5);
        gc.setLineDashes(8, 5);

        double[] ctrl1 = emergencyReentryCtrl1();
        double[] ctrl2 = emergencyReentryCtrl2();

        gc.beginPath();
        gc.moveTo(emergencyStartX, emergencyStartY);
        gc.bezierCurveTo(ctrl1[0], ctrl1[1], ctrl2[0], ctrl2[1], emergencyTargetX, emergencyTargetY);
        gc.stroke();
        gc.setLineDashes();

        // Warning X at emergency start position
        gc.setStroke(Color.web("#FF1744"));
        gc.setLineWidth(2);
        double s = 5;
        gc.strokeLine(emergencyStartX - s, emergencyStartY - s, emergencyStartX + s, emergencyStartY + s);
        gc.strokeLine(emergencyStartX - s, emergencyStartY + s, emergencyStartX + s, emergencyStartY - s);
    }

    private void drawShuttleOnEmergencyRoute(GraphicsContext gc) {
        double t = emergencyProgress;
        double[] ctrl1 = emergencyReentryCtrl1();
        double[] ctrl2 = emergencyReentryCtrl2();

        // Cubic Bezier point at parameter t
        double mt = 1 - t;
        double x = mt*mt*mt * emergencyStartX
                 + 3*mt*mt*t * ctrl1[0]
                 + 3*mt*t*t  * ctrl2[0]
                 + t*t*t     * emergencyTargetX;
        double y = mt*mt*mt * emergencyStartY
                 + 3*mt*mt*t * ctrl1[1]
                 + 3*mt*t*t  * ctrl2[1]
                 + t*t*t     * emergencyTargetY;

        drawShuttleDot(gc, x, y, Color.web("#FF1744"), Color.web("#FF8A80"));
    }

    /**
     * First Bezier control point for the reentry arc.
     * Placed ahead of the shuttle in the direction of travel so the curve
     * exits horizontally before curving steeply down — like a real reentry angle.
     */
    private double[] emergencyReentryCtrl1() {
        // Direction from start to target
        double dx = emergencyTargetX - emergencyStartX;
        double dy = emergencyTargetY - emergencyStartY;
        // Control point: start + 40% of the vector, biased tangentially (perpendicular push outward)
        double perpX = -dy * 0.35;
        double perpY =  dx * 0.35;
        return new double[]{
            emergencyStartX + dx * 0.3 + perpX,
            emergencyStartY + dy * 0.3 + perpY
        };
    }

    /**
     * Second Bezier control point for the reentry arc.
     * Pulled toward the target so the final approach steepens naturally.
     */
    private double[] emergencyReentryCtrl2() {
        double dx = emergencyTargetX - emergencyStartX;
        double dy = emergencyTargetY - emergencyStartY;
        return new double[]{
            emergencyStartX + dx * 0.75,
            emergencyStartY + dy * 0.85
        };
    }

    /** Returns the point on the Earth circle surface closest to (shuttleX, shuttleY). */
    private double[] nearestEarthSurfacePoint(double shuttleX, double shuttleY, double w, double h) {
        double radius = Math.min(w, h) * EARTH_R;
        double cx = EARTH_CX * w;
        double cy = EARTH_CY * h;

        double dx = shuttleX - cx;
        double dy = shuttleY - cy;
        double len = Math.sqrt(dx * dx + dy * dy);

        if (len == 0) {
            // Shuttle exactly at Earth center — default to top of Earth
            return new double[]{cx, cy - radius};
        }

        return new double[]{cx + dx / len * radius, cy + dy / len * radius};
    }

    private void drawShuttle(GraphicsContext gc, double w, double h) {
        double[] pos = interpolatePosition(routeProgress, w, h);
        drawShuttleDot(gc, pos[0], pos[1], Color.web("#FF5722"), Color.web("#FFAB91"));
    }

    private void drawShuttleDot(GraphicsContext gc, double x, double y, Color main, Color highlight) {
        gc.setFill(main.deriveColor(0, 1, 1, 0.3));
        gc.fillOval(x - 10, y - 10, 20, 20);
        gc.setFill(main);
        gc.fillOval(x - 5, y - 5, 10, 10);
        gc.setFill(highlight);
        gc.fillOval(x - 2.5, y - 2.5, 5, 5);
    }

    private double[] interpolatePosition(double progress, double w, double h) {
        if (progress <= 0) return new double[]{WAYPOINTS[0][0] * w, WAYPOINTS[0][1] * h};
        if (progress >= 1) {
            int last = WAYPOINTS.length - 1;
            return new double[]{WAYPOINTS[last][0] * w, WAYPOINTS[last][1] * h};
        }

        double totalSegments = WAYPOINTS.length - 1;
        double segmentProgress = progress * totalSegments;
        int segment = (int) segmentProgress;
        double t = segmentProgress - segment;

        if (segment >= totalSegments) { segment = (int) totalSegments - 1; t = 1.0; }

        double x1 = WAYPOINTS[segment][0] * w;
        double y1 = WAYPOINTS[segment][1] * h;
        double x2 = WAYPOINTS[segment + 1][0] * w;
        double y2 = WAYPOINTS[segment + 1][1] * h;

        return new double[]{x1 + (x2 - x1) * t, y1 + (y2 - y1) * t};
    }
}
