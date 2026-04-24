package features.core.dashboardpanel.captain;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;

public class BarChartPanel extends BaseCardPanel {

    private String[] labels;
    private int[] values;
    private Color[] colors;
    private JPanel contentPanel;
    private JPanel chartPanel;
    private JPanel legendPanel;
    private int maxValue = 100;
    private int total;
    private boolean hasData = false;

    public BarChartPanel(String title, String[] labels, int[] values, Color[] colors, int total) {
        super(title);
        updateInternalData(labels, values, colors, total, false);
        buildUI();
    }

    private void buildUI() {
        contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setOpaque(false);

        chartPanel = createChartPanel();
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(240, 180));
        contentPanel.add(chartPanel, BorderLayout.CENTER);

        legendPanel = createLegendPanel();
        contentPanel.add(legendPanel, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void updateData(String title, String[] newLabels, int[] newValues, Color[] newColors, int newTotal) {
        if (title != null) {
            // setTitle(title);
        }
        updateInternalData(newLabels, newValues, newColors, newTotal, true);
    }

    private void updateInternalData(String[] newLabels, int[] newValues, Color[] newColors, int newTotal,
            boolean rebuild) {
        this.labels = newLabels != null ? newLabels : new String[0];
        this.values = newValues != null ? newValues : new int[0];
        this.colors = newColors != null ? newColors : new Color[0];
        this.total = newTotal;

        // Check if we have actual data to display
        boolean hasNonZeroValues = false;
        for (int v : this.values) {
            if (v > 0) {
                hasNonZeroValues = true;
                break;
            }
        }
        this.hasData = this.labels.length > 0 && this.values.length > 0 && hasNonZeroValues;

        // CEILING = total filtered complaints
        if (this.total > 0) {
            this.maxValue = this.total;
        } else {
            this.maxValue = 100;
        }
        // Ensure maxValue is at least the largest individual value so bars don't
        // overflow
        if (values.length > 0) {
            int maxIndividual = Arrays.stream(values).max().orElse(0);
            if (maxIndividual > this.maxValue) {
                this.maxValue = maxIndividual;
            }
        }
        // Ensure minimum of 5 for readable Y-axis labels
        if (this.maxValue < 5) {
            this.maxValue = 5;
        }

        if (rebuild) {
            // Rebuild legend with new data
            contentPanel.remove(legendPanel);
            legendPanel = createLegendPanel();
            contentPanel.add(legendPanel, BorderLayout.EAST);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private JPanel createChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (!hasData) {
                    drawEmptyState(g);
                    return;
                }

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
                int barW = Math.max(24, (chartW / Math.max(labels.length, 1)) - 20);
                int intervals = 5;

                // Dashed horizontal grid lines
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(
                        1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0f, new float[] { 4f, 4f }, 0f));

                for (int i = 0; i <= intervals; i++) {
                    int y = padTop + (chartH * i / intervals);
                    g2.drawLine(padLeft, y, width - padRight, y);
                }

                // Y-axis labels — use floating point division
                g2.setStroke(new BasicStroke(1));
                g2.setColor(new Color(108, 117, 125));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                for (int i = 0; i <= intervals; i++) {
                    double rawVal = (intervals - i) * (maxValue / (double) intervals);
                    int val = (int) Math.round(rawVal);
                    int y = padTop + (chartH * i / intervals) + 4;
                    g2.drawString(String.valueOf(val), padLeft - 28, y);
                }

                // Bars
                for (int i = 0; i < labels.length; i++) {
                    int sectionW = chartW / Math.max(labels.length, 1);
                    int x = padLeft + (sectionW * i) + (sectionW - barW) / 2;
                    int val = (values != null && i < values.length) ? values[i] : 0;
                    int barH = (int) ((val / (double) maxValue) * chartH);
                    int y = padTop + chartH - barH;

                    g2.setColor(safeColor(i));
                    RoundRectangle2D bar = new RoundRectangle2D.Float(x, y, barW, barH, 8, 8);
                    g2.fill(bar);
                }

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
        JPanel panel = new JPanel(new GridLayout(Math.max(labels.length, 1), 1, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        if (!hasData) {
            JLabel empty = new JLabel("No data");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(new Color(180, 180, 180));
            panel.add(empty);
            return panel;
        }

        for (int i = 0; i < labels.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            item.setOpaque(false);

            JPanel swatch = new JPanel();
            swatch.setBackground(safeColor(i));
            swatch.setPreferredSize(new Dimension(14, 14));
            swatch.setOpaque(true);

            int val = (values != null && i < values.length) ? values[i] : 0;
            JLabel text = new JLabel(labels[i] + " (" + val + ")");
            text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            text.setForeground(new Color(80, 80, 80));

            item.add(swatch);
            item.add(text);
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