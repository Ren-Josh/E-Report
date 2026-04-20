package features.core.dashboardpanel.captain;

import javax.swing.*;
import java.awt.*;

/**
 * StackedBarChartPanel
 *
 * A custom Swing panel that renders a simple stacked-style bar chart with a
 * legend. Each bar represents a category label with a filled value against
 * a fixed maximum scale.
 *
 * The panel consists of:
 * - A custom-drawn chart area (bars + grid + axis labels)
 * - A vertical legend showing color indicators and values
 *
 * This class extends BaseCardPanel and is intended for dashboard visualization
 * of categorized quantitative data.
 */
public class StackedBarChartPanel extends BaseCardPanel {

    // ============================================================
    // INSTANCE VARIABLES (DATA SET)
    // ============================================================

    /** Array of category labels displayed in the chart and legend. */
    private String[] labels;

    /** Maximum possible values per category (reserved for scaling logic). */
    private int[] maxValues;

    /** Actual filled values used to render bar heights. */
    private int[] filledValues;

    /** Color palette assigned per category bar and legend swatch. */
    private Color[] colors;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs a StackedBarChartPanel with dataset and rendering configuration.
     *
     * Initializes chart data arrays and builds the UI consisting of a custom
     * bar chart panel and a legend panel aligned horizontally.
     *
     * @param title        Title displayed in the BaseCardPanel header.
     * @param labels       Array of category labels.
     * @param maxValues    Array of maximum reference values per category.
     * @param filledValues Array of actual values used for bar height rendering.
     * @param colors       Array of colors assigned to each category.
     */
    public StackedBarChartPanel(
            String title,
            String[] labels,
            int[] maxValues,
            int[] filledValues,
            Color[] colors) {
        super(title);

        this.labels = labels;
        this.maxValues = maxValues;
        this.filledValues = filledValues;
        this.colors = colors;

        // ------------------------------------------------------------
        // MAIN CONTENT WRAPPER
        // ------------------------------------------------------------
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        // ------------------------------------------------------------
        // CHART PANEL (CUSTOM DRAWING SURFACE)
        // ------------------------------------------------------------

        JPanel chartPanel = new JPanel() {

            // ============================================================
            // METHOD: paintComponent (CUSTOM GRAPH RENDERING)
            // ============================================================

            // ------------------------------------------------------------
            // METHOD-LEVEL VARIABLES
            // ------------------------------------------------------------

            /*
             * width : int
             * Total width of the chart panel.
             *
             * height : int
             * Total height of the chart panel.
             *
             * padLeft : int
             * Left padding for Y-axis labels and spacing.
             *
             * padRight : int
             * Right padding for chart margin.
             *
             * padTop : int
             * Top padding for chart spacing.
             *
             * padBottom : int
             * Bottom padding for chart spacing.
             *
             * chartW : int
             * Computed drawable width of chart area.
             *
             * chartH : int
             * Computed drawable height of chart area.
             *
             * barW : int
             * Width of each bar based on available space.
             *
             * maxVal : int
             * Fixed maximum scale used for bar height normalization.
             *
             * sectionW : int
             * Horizontal segment width allocated per label.
             *
             * x : int
             * X-position of each bar.
             *
             * y : int
             * Y-position of each bar based on value scaling.
             *
             * barH : int
             * Computed height of each bar.
             *
             * val : int
             * Value used for rendering Y-axis labels.
             */

            /**
             * Custom paint method that renders:
             * - Grid lines
             * - Y-axis labels
             * - Data bars with background and filled portions
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                int padLeft = 35;
                int padRight = 10;
                int padTop = 15;
                int padBottom = 15;

                int chartW = width - padLeft - padRight;
                int chartH = height - padTop - padBottom;

                int barW = Math.max(18, (chartW / labels.length) - 12);
                int maxVal = 200;

                // ------------------------------------------------------------
                // GRID LINES
                // ------------------------------------------------------------
                g2.setColor(new Color(224, 224, 224));
                g2.setStroke(new BasicStroke(1));

                for (int i = 0; i <= 4; i++) {
                    int y = padTop + (chartH * i / 4);
                    g2.drawLine(padLeft, y, width - padRight, y);
                }

                // ------------------------------------------------------------
                // Y-AXIS LABELS
                // ------------------------------------------------------------
                g2.setColor(new Color(108, 117, 125));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                for (int i = 0; i <= 4; i++) {
                    int val = (4 - i) * 50;
                    int y = padTop + (chartH * i / 4) + 4;
                    g2.drawString(String.valueOf(val), padLeft - 32, y);
                }

                // ------------------------------------------------------------
                // BARS RENDERING
                // ------------------------------------------------------------
                for (int i = 0; i < labels.length; i++) {

                    int sectionW = chartW / labels.length;
                    int x = padLeft + (sectionW * i) + (sectionW - barW) / 2;

                    int barH = (int) ((filledValues[i] / (double) maxVal) * chartH);
                    int y = padTop + chartH - barH;

                    // Background bar (light tint)
                    g2.setColor(new Color(
                            colors[i].getRed(),
                            colors[i].getGreen(),
                            colors[i].getBlue(),
                            50));
                    g2.fillRoundRect(x, padTop, barW, chartH, 10, 10);

                    // Filled bar
                    g2.setColor(colors[i]);
                    g2.fillRoundRect(x, y, barW, barH, 10, 10);
                }

                g2.dispose();
            }
        };

        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(220, 180));

        contentPanel.add(chartPanel, BorderLayout.CENTER);

        // ------------------------------------------------------------
        // LEGEND PANEL
        // ------------------------------------------------------------

        JPanel legendPanel = new JPanel(new GridLayout(labels.length, 1, 8, 8));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        for (int i = 0; i < labels.length; i++) {

            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            item.setOpaque(false);

            JPanel swatch = new JPanel();
            swatch.setBackground(colors[i]);
            swatch.setPreferredSize(new Dimension(12, 12));
            swatch.setOpaque(true);

            JLabel text = new JLabel(labels[i] + " (" + filledValues[i] + ")");
            text.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            text.setForeground(new Color(90, 90, 90));

            item.add(swatch);
            item.add(text);

            legendPanel.add(item);
        }

        contentPanel.add(legendPanel, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
    }
}