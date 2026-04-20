package features.core.dashboardpanel.captain;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * BaseCardPanel
 *
 * A base Swing panel that renders a styled card UI with rounded corners and
 * a subtle drop shadow effect. Intended to be extended by specific card-type
 * panels such as BarChartPanel and DonutChartPanel.
 *
 * The panel is non-opaque to allow the custom paintComponent to handle all
 * background rendering. It draws a semi-transparent shadow layer beneath a
 * solid rounded rectangle to simulate card elevation.
 *
 * If a non-empty title is provided, a bold JLabel is added to the NORTH
 * position of the panel's BorderLayout.
 *
 * Layout:
 * - NORTH : titleLabel (optional) — rendered only if title is non-null
 * and non-empty
 * - CENTER : reserved for subclass content
 */
public class BaseCardPanel extends JPanel {

    // -------------------------------------------------------------------------
    // CONSTANTS / DEFAULTS
    // -------------------------------------------------------------------------

    /**
     * The arc radius in pixels used for rounding the corners of the card
     * background and shadow shapes. Defaults to 20.
     */
    protected int cornerRadius = 20;

    /**
     * The background fill color of the card's rounded rectangle.
     * Defaults to Color.WHITE.
     */
    protected Color cardBackground = Color.WHITE;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    // -------------------------------------------------------------------------
    // CONSTRUCTOR-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * titleLabel : JLabel
     * Displays the card title at the top of the panel using
     * Segoe UI Bold 18pt font in dark color (33, 37, 41).
     * Only created and added if the provided title is non-null
     * and non-empty. Added to the NORTH position of the BorderLayout.
     */

    /**
     * Constructs a BaseCardPanel with an optional title label.
     *
     * Sets the layout to BorderLayout with 10px gaps, makes the panel
     * non-opaque for custom painting, and applies a 15px empty border on
     * all sides for inner padding. If a valid title is provided, a styled
     * JLabel is added to the top of the panel.
     *
     * @param title The title text to display at the top of the card.
     *              If null or empty, no title label is added.
     */
    public BaseCardPanel(String title) {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(new Color(33, 37, 41));
            add(titleLabel, BorderLayout.NORTH);
        }
    }

    // =========================================================================
    // METHODS
    // =========================================================================

    // -------------------------------------------------------------------------
    // PAINT-LEVEL VARIABLES (paintComponent)
    // -------------------------------------------------------------------------

    /*
     * g2 : Graphics2D
     * Derived 2D graphics context from the base Graphics object.
     * Antialiasing is enabled on this context. Disposed at the end
     * of each paint cycle before delegating to super.paintComponent.
     *
     * Shadow shape (inline — no variable assigned):
     * RoundRectangle2D.Float offset by (2, 4) with width reduced by 4px
     * and height reduced by 4px relative to the panel bounds. Filled with
     * a semi-transparent black (alpha 20) to simulate a soft drop shadow.
     *
     * Card shape (inline — no variable assigned):
     * RoundRectangle2D.Float anchored at (0, 0) with width reduced by 4px
     * and height reduced by 4px to sit above the shadow. Filled with
     * cardBackground color (default: Color.WHITE).
     */

    /**
     * Renders the card background with a rounded shape and a subtle drop shadow.
     *
     * Draws a semi-transparent black rounded rectangle offset slightly downward
     * to simulate elevation, then draws the solid card background on top. After
     * the custom painting is complete, delegates to super.paintComponent to allow
     * child components to render normally.
     *
     * @param g The base Graphics context provided by the Swing paint system.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 20));
        g2.fill(new RoundRectangle2D.Float(2, 4, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius));

        g2.setColor(cardBackground);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius));

        g2.dispose();
        super.paintComponent(g);
    }
}