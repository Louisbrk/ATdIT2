package org.example.spaceflight.ui.shared;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/**
 * LUMEIA brand mark. Loads the image once at high resolution; each factory method
 * returns a fresh node sized for a specific dashboard slot.
 */
public final class LogoView {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final String RESOURCE = "/org/example/spaceflight/images/LUMEIA.png";
    // Decode large enough that any downscaling stays crisp on HiDPI screens.
    private static final Image IMAGE = load(512);

    private LogoView() {}

    /** Small fixed-size badge (~56 px) for header / footer rows. Clicks pass through. */
    public static StackPane header() {
        ImageView view = baseImageView();
        view.setFitHeight(56);

        StackPane wrap = wrapper(view);
        wrap.setPadding(new Insets(2, 8, 2, 8));
        return wrap;
    }

    /** Horizontal inset (px) between the logo and the column edges. Lower = closer to full width. */
    private static final double COLUMN_INSET = 16;

    /**
     * Sidebar logo locked to the column's width. The image width follows the parent column
     * directly (minus a small inset on each side); height follows by aspect ratio. No proportional
     * scaling — when the column grows, the logo grows; otherwise it stays put.
     */
    public static StackPane medium() {
        ImageView view = baseImageView();

        StackPane wrap = wrapper(view);
        wrap.setMaxWidth(Double.MAX_VALUE);
        wrap.setPadding(new Insets(6));
        view.fitWidthProperty().bind(wrap.widthProperty().subtract(COLUMN_INSET));
        return wrap;
    }

    /** Large, centered logo for the start screen (~180 px tall). */
    public static StackPane hero() {
        ImageView view = baseImageView();
        view.setFitHeight(180);

        StackPane wrap = wrapper(view);
        wrap.setPadding(new Insets(8));
        return wrap;
    }

    private static ImageView baseImageView() {
        ImageView view = new ImageView(IMAGE);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setCache(true);
        Tooltip.install(view, new Tooltip("LUMEIA — Experience Space. Discover You."));
        return view;
    }

    private static StackPane wrapper(ImageView view) {
        StackPane wrap = new StackPane(view);
        wrap.setAlignment(Pos.CENTER);
        wrap.setMouseTransparent(true);
        wrap.setPickOnBounds(false);
        return wrap;
    }

    private static Image load(double targetHeightPx) {
        try {
            var stream = LogoView.class.getResourceAsStream(RESOURCE);
            if (stream != null) {
                return new Image(stream, 0, targetHeightPx, true, true);
            }
            log.warning("LUMEIA.png not found at " + RESOURCE);
        } catch (Exception e) {
            log.warning("Could not load LUMEIA.png: " + e.getMessage());
        }
        return null;
    }
}
