package features.core.dashboardpanel.captain;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * BarChartPanel
 *
 * A custom Swing panel that displays a bar chart with a corresponding legend.
 * Extends BaseCardPanel to inherit the base card layout and title rendering.
 *
 * The chart renders rounded bars with dashed horizontal grid lines and Y-axis
 * labels. The legend is displayed to the right of the chart, showing a color
 * swatch, category label, and its corresponding value for each bar.
 *
 * Layout:
 * - CENTER : chartPanel — draws bars, grid lines, and Y-axis labels
 * - EAST : legendPanel — lists each category with its color swatch and value
 */
public class BarChartPanel extends BaseCardPanel {

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /**
     * Array of category label names displayed in the legend and mapped to each bar.
     */
    private String[] labels;

    /**
     * Array of integer values corresponding to each bar, used to compute bar
     * height.
     */
    private int[] values;

    /**
     * Array of colors assigned to each bar and its matching legend color swatch.
     */
    private Color[] colors;

    // -------------------------------------------------------------------------
    // CONSTANTS / DEFAULTS
    // -------------------------------------------------------------------------

    /**
     * The maximum value used to scale bar heights relative to the chart area.
     * Acts as the Y-axis ceiling. Defaults to 100.
     */
    private int maxValue = 100;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    // -------------------------------------------------------------------------
    // CONSTRUCTOR-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * contentPanel : JPanel
     * Root wrapper panel using BorderLayout with a 15px horizontal gap.
     * Holds the chartPanel (CENTER) and legendPanel (EAST) side by side.
     *
     * chartPanel : JPanel (anonymous subclass)
     * Overrides paintComponent to perform all custom bar chart rendering.
     * Fixed preferred size of 240x180 pixels.
     *
     * legendPanel : JPanel
     * Uses GridLayout to display one legend row per label entry.
     * Padded with 10px top/bottom and 5px left/right empty border.
     *
     * -- Per legend entry (inside for-loop) --
     *
     * item : JPanel
     * A single legend row using FlowLayout (LEFT-aligned, 8px gap),
     * containing one swatch and one label per entry.
     *
     * swatch : JPanel
     * A small 14x14px colored square representing the bar color
     * for its corresponding category entry.
     *
     * text : JLabel
     * Displays the category name and value in the format:
     * "Label (value)" using Segoe UI plain 12pt font.
     */

    /**
     * Constructs a BarChartPanel with the given title, data labels, values,
     * and colors.
     *
     * Initializes the chart drawing area and legend panel, then assembles
     * them into a BorderLayout content wrapper that is added to the card.
     *
     * @param title  The title text passed to BaseCardPanel for the card header.
     * @param labels Category names for each bar in the chart.
     * @param values Numeric values for each bar used to calculate bar height
     *               relative to maxValue.
     * @param colors Colors used to fill each bar and its matching legend swatch.
     */
    public BarChartPanel(String title, String[] labels, int[] values, Color[] colors) {
        super(title);
        this.labels = labels;
        this.values = values;
        this.colors = colors;

        // Content wrapper: chart in center, legend on right
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setOpaque(false);

        // Chart area — bars + dashed grid lines only
        JPanel chartPanel = new JPanel() {

            // -----------------------------------------------------------------
            // PAINT-LEVEL VARIABLES (paintComponent)
            // -----------------------------------------------------------------

            /*
             * g2 : Graphics2D
             * Derived 2D graphics context from the base Graphics object.
             * Antialiasing is enabled on this context. Disposed at the
             * end of each paint cycle.
             *
             * width : int
             * Current pixel width of the chartPanel at paint time.
             *
             * height : int
             * Current pixel height of the chartPanel at paint time.
             *
             * padLeft : int = 35
             * Left padding in pixels; reserves space for Y-axis labels.
             *
             * padRight : int = 10
             * Right padding in pixels.
             *
             * padTop : int = 15
             * Top padding in pixels.
             *
             * padBottom : int = 15
             * Bottom padding in pixels.
             *
             * chartW : int
             * Effective drawable width after subtracting left and right padding.
             *
             * chartH : int
             * Effective drawable height after subtracting top and bottom padding.
             *
             * barW : int
             * Width of each individual bar in pixels. Computed as
             * (chartW / number of labels) - 20, with a minimum of 24px enforced.
             *
             * intervals : int = 5
             * Number of Y-axis divisions, producing grid lines and labels
             * at 0, 20, 40, 60, 80, and 100 by default.
             *
             * -- Inside grid line loop --
             *
             * y (grid) : int
             * Y pixel coordinate for each horizontal dashed grid line.
             *
             * -- Inside Y-axis label loop --
             *
             * val : int
             * Numeric label printed at each Y-axis interval, stepping
             * down from maxValue to 0.
             *
             * y (label) : int
             * Y pixel coordinate for each Y-axis label, offset +4px
             * downward for vertical text alignment.
             *
             * -- Inside bar drawing loop --
             *
             * sectionW : int
             * Pixel width of each bar's allocated column section,
             * computed as chartW divided by number of labels.
             *
             * x : int
             * X pixel coordinate for the left edge of the current bar,
             * centered within its section.
             *
             * barH : int
             * Pixel height of the current bar, proportionally calculated
             * as (value / maxValue) * chartH.
             *
             * y (bar) : int
             * Y pixel coordinate for the top of the current bar.
             * Grows upward from the bottom edge of the chart area.
             *
             * bar : RoundRectangle2D
             * Rounded rectangle shape used to render each bar with
             * 8px arc rounding on all corners.
             */

            /**
             * Renders the bar chart onto this panel.
             *
             * Draws dashed horizontal grid lines across the chart area, numeric
             * Y-axis labels on the left side, and filled rounded rectangular bars
             * for each data entry. Antialiasing is applied for smoother output.
             *
             * @param g The base Graphics context provided by the Swing paint system.
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padLeft = 35;
                int padRight = 10;
                int padTop = 15;
                int padBottom = 15;

                int chartW = width - padLeft - padRight;
                int chartH = height - padTop - padBottom;
                int barW = Math.max(24, (chartW / labels.length) - 20);
                int intervals = 5; // 0, 20, 40, 60, 80, 100

                // Dashed horizontal grid lines
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(
                        1f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL,
                        0f,
                        new float[] { 4f, 4f },
                        0f));

                for (int i = 0; i <= intervals; i++) {
                    int y = padTop + (chartH * i / intervals);
                    g2.drawLine(padLeft, y, width - padRight, y);
                }

                // Y-axis labels
                g2.setStroke(new BasicStroke(1)); // reset stroke
                g2.setColor(new Color(108, 117, 125));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                for (int i = 0; i <= intervals; i++) {
                    int val = (intervals - i) * (maxValue / intervals);
                    int y = padTop + (chartH * i / intervals) + 4;
                    g2.drawString(String.valueOf(val), padLeft - 28, y);
                }

                // Bars
                for (int i = 0; i < labels.length; i++) {
                    int sectionW = chartW / labels.length;
                    int x = padLeft + (sectionW * i) + (sectionW - barW) / 2;
                    int barH = (int) ((values[i] / (double) maxValue) * chartH);
                    int y = padTop + chartH - barH;

                    g2.setColor(colors[i]);
                    RoundRectangle2D bar = new RoundRectangle2D.Float(x, y, barW, barH, 8, 8);
                    g2.fill(bar);
                }

                g2.dispose();
            }
        };
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(240, 180));
        contentPanel.add(chartPanel, BorderLayout.CENTER);

        // Legend panel — vertical list with color swatch + label + value
        JPanel legendPanel = new JPanel(new GridLayout(labels.length, 1, 10, 10));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        for (int i = 0; i < labels.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            item.setOpaque(false);

            // Color swatch
            JPanel swatch = new JPanel();
            swatch.setBackground(colors[i]);
            swatch.setPreferredSize(new Dimension(14, 14));
            swatch.setOpaque(true);

            // Label with value in parentheses
            JLabel text = new JLabel(labels[i] + " (" + values[i] + ")");
            text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            text.setForeground(new Color(80, 80, 80));

            item.add(swatch);
            item.add(text);
            legendPanel.add(item);
        }

        contentPanel.add(legendPanel, BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);
    }
}