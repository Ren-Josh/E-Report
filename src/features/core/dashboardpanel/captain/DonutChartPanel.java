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

    // -------------------------------------------------------------------------
    // UI COMPONENTS (stored for dynamic updates)
    // -------------------------------------------------------------------------

    /** Wrapper holding donutPanel and legendPanel. */
    private JPanel chartPanel;

    /** The donut rendering panel. */
    private JPanel donutPanel;

    /** The legend listing categories. */
    private JPanel legendPanel;

    /** The title label at the top. */
    private JLabel titleLabel;

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
        this.labels = labels != null ? labels : new String[0];
        this.values = values != null ? values : new int[0];
        this.colors = colors != null ? colors : new Color[0];

        setLayout(new BorderLayout(10, 10));

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));
        add(titleLabel, BorderLayout.NORTH);

        chartPanel = new JPanel(new BorderLayout(10, 0));
        chartPanel.setOpaque(false);

        donutPanel = createDonutPanel();
        donutPanel.setOpaque(false);
        donutPanel.setPreferredSize(new Dimension(150, 150));
        chartPanel.add(donutPanel, BorderLayout.CENTER);

        legendPanel = createLegendPanel();
        chartPanel.add(legendPanel, BorderLayout.EAST);

        add(chartPanel, BorderLayout.CENTER);
    }

    // =========================================================================
    // PUBLIC API — Data Updates
    // =========================================================================

    /**
     * Updates the donut chart data, colors, and legend, then repaints.
     *
     * @param title     New chart title (can be null to keep existing).
     * @param newLabels New category labels.
     * @param newValues New segment values.
     * @param newColors New segment colors.
     */
    public void updateData(String title, String[] newLabels, int[] newValues, Color[] newColors) {
        if (title != null) {
            titleLabel.setText(title);
        }
        this.labels = newLabels != null ? newLabels : new String[0];
        this.values = newValues != null ? newValues : new int[0];
        this.colors = newColors != null ? newColors : new Color[0];

        // Rebuild legend since label text and values changed
        chartPanel.remove(legendPanel);
        legendPanel = createLegendPanel();
        chartPanel.add(legendPanel, BorderLayout.EAST);

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    // =========================================================================
    // PRIVATE — Component Builders
    // =========================================================================

    private JPanel createDonutPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (values == null || values.length == 0 || labels == null || labels.length == 0) {
                    drawEmptyState(g);
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                int total = 0;
                for (int v : values)
                    total += v;

                if (total == 0) {
                    drawEmptyState(g);
                    g2.dispose();
                    return;
                }

                float startAngle = 90;
                for (int i = 0; i < values.length; i++) {
                    float arcAngle = -(float) (values[i] * 360.0 / total);
                    g2.setColor(safeColor(i));
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

            private void drawEmptyState(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 180, 180));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                String msg = "No data available";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g2.drawString(msg, x, y);
                g2.dispose();
            }
        };
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new GridLayout(Math.max(labels.length, 1), 1, 2, 2));
        panel.setOpaque(false);

        if (labels == null || labels.length == 0) {
            JLabel empty = new JLabel("No data");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            empty.setForeground(new Color(180, 180, 180));
            panel.add(empty);
            return panel;
        }

        for (int i = 0; i < labels.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            item.setOpaque(false);

            JPanel colorBox = new JPanel();
            colorBox.setBackground(safeColor(i));
            colorBox.setPreferredSize(new Dimension(12, 12));

            int val = (values != null && i < values.length) ? values[i] : 0;
            JLabel label = new JLabel(labels[i] + " (" + val + ")");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            label.setForeground(new Color(108, 117, 125));

            item.add(colorBox);
            item.add(label);
            panel.add(item);
        }
        return panel;
    }

    private Color safeColor(int index) {
        if (colors != null && index >= 0 && index < colors.length) {
            return colors[index];
        }
        return Color.GRAY;
    }
}