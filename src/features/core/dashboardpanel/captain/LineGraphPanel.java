package features.core.dashboardpanel.captain;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * LineGraphPanel
 *
 * A custom dashboard panel that renders a line graph with an area fill using
 * Java Swing's 2D Graphics API. This panel extends BaseCardPanel and displays
 * a titled card containing a visual representation of numerical data over
 * labeled intervals.
 *
 * The graph includes:
 * - A background grid with horizontal guide lines
 * - A gradient-filled area under the line
 * - A smooth line connecting data points
 * - Circular markers for each data point
 * - X-axis labels (provided)
 * - Y-axis scale labels (fixed percentage-based values)
 *
 * The graph dynamically scales based on the provided data and panel size.
 */
public class LineGraphPanel extends BaseCardPanel {

    // -------------------------------------------------------------------------
    // DATA VARIABLES
    // -------------------------------------------------------------------------

    /**
     * Array of numerical values to be plotted on the graph.
     * Each value corresponds to a specific label on the X-axis.
     */
    private double[] data;

    /**
     * Labels corresponding to each data point.
     * Displayed along the X-axis.
     */
    private String[] labels;

    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    /**
     * The maximum value used to normalize data scaling on the Y-axis.
     * Default is 100, assuming percentage-based data.
     */
    private double maxValue = 100;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Constructs a LineGraphPanel with a specified title, data set,
     * and corresponding labels.
     *
     * Initializes the base card panel, assigns the provided data and labels,
     * and creates a custom chart panel responsible for rendering the graph.
     *
     * @param title  The title displayed at the top of the card panel.
     * @param data   Array of numerical values to be plotted.
     * @param labels Array of labels corresponding to each data point.
     */
    public LineGraphPanel(String title, double[] data, String[] labels) {
        super(title);

        this.data = data;
        this.labels = labels;

        JPanel chartPanel = new JPanel() {

            // -----------------------------------------------------------------
            // paintComponent — METHOD-LEVEL VARIABLES
            // -----------------------------------------------------------------

            /*
             * g2 : Graphics2D
             * A copy of the Graphics object used for advanced 2D rendering.
             *
             * width : int
             * Current width of the panel.
             *
             * height : int
             * Current height of the panel.
             *
             * padding : int
             * Margin space around the chart area.
             *
             * chartWidth : int
             * Drawable width of the chart excluding padding.
             *
             * chartHeight : int
             * Drawable height of the chart excluding padding.
             *
             * gradient : GradientPaint
             * Paint used to fill the area under the line graph.
             *
             * areaPath : Path2D
             * Shape representing the filled area under the line graph.
             *
             * xPoints : int[]
             * Computed X coordinates for each data point.
             *
             * yPoints : int[]
             * Computed Y coordinates for each data point.
             *
             * fm : FontMetrics
             * Used to calculate text width for label alignment.
             */

            /**
             * Paints the line graph, including grid lines, area fill,
             * data lines, markers, and axis labels.
             *
             * Rendering Steps:
             * 1. Enable anti-aliasing for smoother visuals
             * 2. Draw horizontal grid lines
             * 3. Compute data point coordinates
             * 4. Create and fill gradient area under the line
             * 5. Draw connecting lines between data points
             * 6. Draw circular markers on each data point
             * 7. Render X-axis labels
             * 8. Render Y-axis scale labels
             *
             * @param g The Graphics context used for painting
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 40;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // Draw horizontal grid lines
                g2.setColor(new Color(224, 224, 224));
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5 }, 0));
                for (int i = 0; i <= 4; i++) {
                    int y = padding + (chartHeight * i / 4);
                    g2.drawLine(padding, y, width - padding, y);
                }

                // Create gradient for area fill
                GradientPaint gradient = new GradientPaint(
                        0, padding, new Color(66, 133, 244, 180),
                        0, height - padding, new Color(66, 133, 244, 20));

                Path2D areaPath = new Path2D.Double();
                areaPath.moveTo(padding, height - padding);

                int[] xPoints = new int[data.length];
                int[] yPoints = new int[data.length];

                // Calculate coordinates for each data point
                for (int i = 0; i < data.length; i++) {
                    xPoints[i] = padding + (chartWidth * i / (data.length - 1));
                    yPoints[i] = height - padding - (int) ((data[i] / maxValue) * chartHeight);
                    areaPath.lineTo(xPoints[i], yPoints[i]);
                }

                // Close area path
                areaPath.lineTo(width - padding, height - padding);
                areaPath.closePath();

                // Fill area under the line
                g2.setPaint(gradient);
                g2.fill(areaPath);

                // Draw line graph
                g2.setColor(new Color(66, 133, 244));
                g2.setStroke(new BasicStroke(3));
                for (int i = 0; i < data.length - 1; i++) {
                    g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
                }

                // Draw data point markers
                g2.setColor(Color.WHITE);
                for (int i = 0; i < data.length; i++) {
                    g2.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                    g2.setColor(new Color(66, 133, 244));
                    g2.drawOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                    g2.setColor(Color.WHITE);
                }

                // Draw X-axis labels
                g2.setColor(new Color(108, 117, 125));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                FontMetrics fm = g2.getFontMetrics();
                for (int i = 0; i < labels.length; i++) {
                    int x = xPoints[i] - fm.stringWidth(labels[i]) / 2;
                    g2.drawString(labels[i], x, height - padding + 20);
                }

                // Draw Y-axis labels
                for (int i = 0; i <= 4; i++) {
                    int value = 100 - (i * 25);
                    String label = String.valueOf(value);
                    int x = padding - 30;
                    int y = padding + (chartHeight * i / 4) + 4;
                    g2.drawString(label, x, y);
                }

                g2.dispose();
            }
        };

        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(300, 200));

        add(chartPanel, BorderLayout.CENTER);
    }
}