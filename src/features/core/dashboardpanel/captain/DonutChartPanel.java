package features.core.dashboardpanel.captain;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * DonutChartPanel
 *
 * A custom Swing panel that displays a donut (hollow pie) chart with a
 * corresponding legend. Extends BaseCardPanel to inherit the base card
 * layout and title rendering.
 *
 * The chart renders filled pie segments using Arc2D, then overlays a white
 * filled oval at the center to produce the donut hole effect. Each segment
 * corresponds to a category proportional to its value relative to the total.
 * The legend is displayed to the right of the donut, listing each category
 * with its color box and value.
 *
 * Layout:
 * - NORTH : titleLabel — displays the card title
 * - CENTER : chartPanel — holds the donut drawing area and the legend
 * - CENTER : donutPanel — renders the donut chart segments
 * - EAST : legendPanel — lists each category with its color box and value
 */
public class DonutChartPanel extends BaseCardPanel {

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /**
     * Array of category label names displayed in the legend and mapped to each
     * segment.
     */
    private String[] labels;

    /**
     * Array of integer values for each segment, used to compute arc angles
     * proportionally.
     */
    private int[] values;

    /**
     * Array of colors assigned to each segment and its matching legend color box.
     */
    private Color[] colors;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    // -------------------------------------------------------------------------
    // CONSTRUCTOR-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * titleLabel : JLabel
     * Displays the chart title at the top of the panel using
     * Segoe UI Bold 18pt font in dark color (33, 37, 41).
     * Added to the NORTH position of the panel's BorderLayout.
     *
     * chartPanel : JPanel
     * Wrapper panel using BorderLayout with a 10px horizontal gap.
     * Holds the donutPanel (CENTER) and legendPanel (EAST).
     *
     * donutPanel : JPanel (anonymous subclass)
     * Overrides paintComponent to perform all custom donut chart rendering.
     * Fixed preferred size of 150x150 pixels.
     *
     * legendPanel : JPanel
     * Uses GridLayout to display one legend row per label entry,
     * with 2px horizontal and vertical gaps between rows.
     *
     * -- Per legend entry (inside for-loop) --
     *
     * item : JPanel
     * A single legend row using FlowLayout (LEFT-aligned, 5px gap),
     * containing one color box and one label per entry.
     *
     * colorBox : JPanel
     * A small 12x12px colored square representing the segment color
     * for its corresponding category entry.
     *
     * label : JLabel
     * Displays the category name and value in the format:
     * "Label (value)" using Segoe UI plain 10pt font.
     */

    /**
     * Constructs a DonutChartPanel with the given title, data labels, values,
     * and colors.
     *
     * Initializes the donut chart drawing area and legend panel, then assembles
     * them into a BorderLayout content wrapper that is added to the card.
     *
     * @param title  The title text passed to BaseCardPanel and also rendered
     *               as a JLabel at the top of the panel.
     * @param labels Category names for each segment in the donut chart.
     * @param values Numeric values for each segment used to calculate arc
     *               angles proportionally against the total sum of all values.
     * @param colors Colors used to fill each segment and its matching legend
     *               color box.
     */
    public DonutChartPanel(String title, String[] labels, int[] values, Color[] colors) {
        super(title);
        this.labels = labels;
        this.values = values;
        this.colors = colors;

        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));
        add(titleLabel, BorderLayout.NORTH);

        JPanel chartPanel = new JPanel(new BorderLayout(10, 0));
        chartPanel.setOpaque(false);

        JPanel donutPanel = new JPanel() {

            // -----------------------------------------------------------------
            // PAINT-LEVEL VARIABLES (paintComponent)
            // -----------------------------------------------------------------

            /*
             * g2 : Graphics2D
             * Derived 2D graphics context from the base Graphics object.
             * Antialiasing is enabled on this context. Disposed at the
             * end of each paint cycle.
             *
             * size : int
             * The pixel diameter of the donut chart. Computed as the
             * smaller of the panel's current width or height, minus 20px
             * to provide edge padding.
             *
             * x : int
             * The X pixel coordinate for the top-left corner of the
             * bounding box that contains the donut chart, centering it
             * horizontally within the panel.
             *
             * y : int
             * The Y pixel coordinate for the top-left corner of the
             * bounding box that contains the donut chart, centering it
             * vertically within the panel.
             *
             * total : int
             * The sum of all values in the values array. Used as the
             * denominator when computing each segment's proportional
             * arc angle.
             *
             * startAngle : float = 90
             * The current starting angle in degrees for the next arc
             * segment. Initialized to 90 degrees (top of circle) and
             * incremented by each segment's arcAngle after drawing.
             *
             * -- Inside segment drawing loop --
             *
             * arcAngle : float
             * The sweep angle in degrees for the current segment,
             * computed as -(value / total) * 360. Negative value
             * draws the arc clockwise.
             *
             * arc : Arc2D
             * A pie-type arc shape used to render each donut segment
             * within the bounding box defined by x, y, and size.
             *
             * innerSize : int
             * The diameter of the white center circle that creates the
             * donut hole. Computed as half of size.
             *
             * innerX : int
             * The X pixel coordinate for the top-left corner of the
             * white center oval, centered within the donut bounding box.
             *
             * innerY : int
             * The Y pixel coordinate for the top-left corner of the
             * white center oval, centered within the donut bounding box.
             */

            /**
             * Renders the donut chart onto this panel.
             *
             * Draws filled pie segments proportional to each value relative
             * to the total, starting from the top (90 degrees) and sweeping
             * clockwise. Overlays a white filled oval at the center to create
             * the donut hole effect. Antialiasing is applied for smoother output.
             *
             * @param g The base Graphics context provided by the Swing paint system.
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                int total = 0;
                for (int v : values)
                    total += v;

                float startAngle = 90;
                for (int i = 0; i < values.length; i++) {
                    float arcAngle = -(float) (values[i] * 360.0 / total);
                    g2.setColor(colors[i]);
                    Arc2D arc = new Arc2D.Float(x, y, size, size, startAngle, arcAngle, Arc2D.PIE);
                    g2.fill(arc);
                    startAngle += arcAngle;
                }

                g2.setColor(Color.WHITE);
                int innerSize = size / 2;
                int innerX = x + (size - innerSize) / 2;
                int innerY = y + (size - innerSize) / 2;
                g2.fillOval(innerX, innerY, innerSize, innerSize);

                g2.dispose();
            }
        };
        donutPanel.setOpaque(false);
        donutPanel.setPreferredSize(new Dimension(150, 150));
        chartPanel.add(donutPanel, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new GridLayout(labels.length, 1, 2, 2));
        legendPanel.setOpaque(false);

        for (int i = 0; i < labels.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            item.setOpaque(false);

            JPanel colorBox = new JPanel();
            colorBox.setBackground(colors[i]);
            colorBox.setPreferredSize(new Dimension(12, 12));

            JLabel label = new JLabel(labels[i] + " (" + values[i] + ")");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            label.setForeground(new Color(108, 117, 125));

            item.add(colorBox);
            item.add(label);
            legendPanel.add(item);
        }

        chartPanel.add(legendPanel, BorderLayout.EAST);
        add(chartPanel, BorderLayout.CENTER);
    }
}