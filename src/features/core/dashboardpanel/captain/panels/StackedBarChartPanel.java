package features.core.dashboardpanel.captain.panels;

import javax.swing.*;
import java.awt.*;

public class StackedBarChartPanel extends BaseCardPanel {

    private String[] labels;
    private int[] maxValues;
    private int[] filledValues;
    private int maxVal;
    private Color[] colors;
    private JPanel contentPanel;
    private JPanel chartPanel;
    private JPanel legendPanel;
    private boolean hasData = false;

    public StackedBarChartPanel(
            String title,
            String[] labels,
            int[] maxValues,
            int[] filledValues,
            Color[] colors,
            int maxVal) {
        super(title);
        updateInternalData(labels, maxValues, filledValues, colors, maxVal, false);
        buildUI();
    }

    private void buildUI() {
        contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        chartPanel = createChartPanel();
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(220, 180));
        contentPanel.add(chartPanel, BorderLayout.CENTER);

        legendPanel = createLegendPanel();
        contentPanel.add(legendPanel, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void updateData(String title, String[] newLabels, int[] newMaxValues, int[] newFilled, Color[] newColors,
            int newMaxVal) {
        if (title != null) {
            // setTitle(title);
        }
        updateInternalData(newLabels, newMaxValues, newFilled, newColors, newMaxVal, true);
    }

    private void updateInternalData(String[] newLabels, int[] newMaxValues, int[] newFilled, Color[] newColors,
            int newMaxVal, boolean rebuild) {
        this.labels = newLabels != null ? newLabels : new String[0];
        this.maxValues = newMaxValues != null ? newMaxValues : new int[0];
        this.filledValues = newFilled != null ? newFilled : new int[0];
        this.colors = newColors != null ? newColors : new Color[0];

        // Check if we have actual data to display
        boolean hasNonZeroValues = false;
        for (int v : this.filledValues) {
            if (v > 0) {
                hasNonZeroValues = true;
                break;
            }
        }
        this.hasData = this.labels.length > 0 && this.filledValues.length > 0 && hasNonZeroValues;

        // CRITICAL FIX: Actually update the ceiling value!
        this.maxVal = newMaxVal > 0 ? newMaxVal : 100;

        if (rebuild) {
            // Rebuild legend since text/values changed
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

                int barW = Math.max(18, (chartW / Math.max(labels.length, 1)) - 12);

                // GRID LINES
                g2.setColor(new Color(224, 224, 224));
                g2.setStroke(new BasicStroke(1));

                for (int i = 0; i <= 4; i++) {
                    int y = padTop + (chartH * i / 4);
                    g2.drawLine(padLeft, y, width - padRight, y);
                }

                // Y-AXIS LABELS — use floating point division
                g2.setColor(new Color(108, 117, 125));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                for (int i = 0; i <= 4; i++) {
                    double rawVal = (4 - i) * (maxVal / 4.0);
                    int val = (int) Math.round(rawVal);
                    int y = padTop + (chartH * i / 4) + 4;
                    g2.drawString(String.valueOf(val), padLeft - 32, y);
                }

                // BARS RENDERING
                for (int i = 0; i < labels.length; i++) {

                    int sectionW = chartW / Math.max(labels.length, 1);
                    int x = padLeft + (sectionW * i) + (sectionW - barW) / 2;

                    int fill = (filledValues != null && i < filledValues.length) ? filledValues[i] : 0;
                    int barH = (int) ((fill / (double) maxVal) * chartH);
                    int y = padTop + chartH - barH;

                    Color c = safeColor(i);
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
                    g2.fillRoundRect(x, padTop, barW, chartH, 10, 10);

                    g2.setColor(c);
                    g2.fillRoundRect(x, y, barW, barH, 10, 10);
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
        JPanel panel = new JPanel(new GridLayout(Math.max(labels.length, 1), 1, 8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        if (!hasData) {
            JLabel empty = new JLabel("No data");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            empty.setForeground(new Color(180, 180, 180));
            panel.add(empty);
            return panel;
        }

        for (int i = 0; i < labels.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            item.setOpaque(false);

            JPanel swatch = new JPanel();
            swatch.setBackground(safeColor(i));
            swatch.setPreferredSize(new Dimension(12, 12));
            swatch.setOpaque(true);

            int val = (filledValues != null && i < filledValues.length) ? filledValues[i] : 0;
            JLabel text = new JLabel(labels[i] + " (" + val + ")");
            text.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            text.setForeground(new Color(90, 90, 90));

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