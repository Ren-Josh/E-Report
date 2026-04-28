package features.core.dashboardpanel.captain;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;

public class LineGraphPanel extends BaseCardPanel {

    private double[] data;
    private String[] labels;
    private String[] pointDetails;
    private JPanel chartPanel;
    private JPanel legendPanel;
    private JPanel contentPanel;
    private JLabel titleLabel; // direct reference to BaseCardPanel's title
    private String cardTitle;
    private double maxValue = 100;

    public LineGraphPanel(String title, double[] data, String[] labels) {
        this(title, data, labels, null);
    }

    public LineGraphPanel(String title, double[] data, String[] labels, String[] pointDetails) {
        super(title);
        this.cardTitle = title;
        this.data = data != null ? data : new double[0];
        this.labels = labels != null ? labels : new String[0];
        this.pointDetails = pointDetails != null ? pointDetails : new String[0];

        // Grab a direct reference to the bold title label that BaseCardPanel created
        this.titleLabel = findTitleLabel(this, title);

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

    /**
     * Searches the hierarchy for the bold JLabel whose text matches the initial
     * title.
     */
    private JLabel findTitleLabel(Container container, String expectedText) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (expectedText.equals(lbl.getText())
                        && lbl.getFont() != null
                        && lbl.getFont().isBold()) {
                    return lbl;
                }
            }
            if (c instanceof Container) {
                JLabel found = findTitleLabel((Container) c, expectedText);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    /** Updates the card title safely. */
    public void setCardTitle(String newTitle) {
        if (titleLabel != null && newTitle != null) {
            titleLabel.setText(newTitle);
            this.cardTitle = newTitle;
        }
    }

    public void updateData(double[] newData, String[] newLabels) {
        updateData(newData, newLabels, null, null);
    }

    public void updateData(double[] newData, String[] newLabels, String[] newPointDetails) {
        updateData(newData, newLabels, newPointDetails, null);
    }

    /** Full update — data, legend, and optional new card title. */
    public void updateData(double[] newData, String[] newLabels, String[] newPointDetails, String newTitle) {
        this.data = newData != null ? newData : new double[0];
        this.labels = newLabels != null ? newLabels : new String[0];
        this.pointDetails = newPointDetails != null ? newPointDetails : new String[0];

        if (newTitle != null) {
            setCardTitle(newTitle);
        }

        if (data.length > 0) {
            this.maxValue = Arrays.stream(data).max().orElse(100);
            if (this.maxValue <= 0)
                this.maxValue = 100;
        } else {
            this.maxValue = 100;
        }

        contentPanel.remove(legendPanel);
        legendPanel = createLegendPanel();
        contentPanel.add(legendPanel, BorderLayout.EAST);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (data == null || data.length == 0 || labels == null || labels.length == 0
                        || data.length != labels.length) {
                    drawEmptyState(g);
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 30;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // ---- dashed horizontal grid lines ----
                g2.setColor(new Color(224, 224, 224));
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5 }, 0));
                for (int i = 0; i <= 4; i++) {
                    int y = padding + (chartHeight * i / 4);
                    g2.drawLine(padding, y, width - padding, y);
                }

                // ---- compute point positions ----
                int[] xPoints = new int[data.length];
                int[] yPoints = new int[data.length];
                if (data.length == 1) {
                    xPoints[0] = padding + chartWidth / 2;
                    yPoints[0] = height - padding - (int) ((data[0] / maxValue) * chartHeight);
                } else {
                    for (int i = 0; i < data.length; i++) {
                        xPoints[i] = padding + (chartWidth * i / (data.length - 1));
                        yPoints[i] = height - padding - (int) ((data[i] / maxValue) * chartHeight);
                    }
                }

                // ---- filled area (only when more than one point) ----
                if (data.length > 1) {
                    GradientPaint gradient = new GradientPaint(
                            0, padding, new Color(66, 133, 244, 180),
                            0, height - padding, new Color(66, 133, 244, 20));

                    Path2D areaPath = new Path2D.Double();
                    areaPath.moveTo(xPoints[0], height - padding);
                    for (int i = 0; i < data.length; i++) {
                        areaPath.lineTo(xPoints[i], yPoints[i]);
                    }
                    areaPath.lineTo(xPoints[data.length - 1], height - padding);
                    areaPath.closePath();

                    g2.setPaint(gradient);
                    g2.fill(areaPath);
                }

                // ---- draw connecting lines ----
                g2.setColor(new Color(66, 133, 244));
                g2.setStroke(new BasicStroke(3));
                for (int i = 0; i < data.length - 1; i++) {
                    g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
                }

                // ---- draw data points ----
                g2.setColor(Color.WHITE);
                for (int i = 0; i < data.length; i++) {
                    g2.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                    g2.setColor(new Color(66, 133, 244));
                    g2.drawOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                    g2.setColor(Color.WHITE);
                }

                // ---- X-axis labels ----
                g2.setColor(new Color(108, 117, 125));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics fm = g2.getFontMetrics();
                for (int i = 0; i < labels.length; i++) {
                    int x = xPoints[i] - fm.stringWidth(labels[i]) / 2;
                    g2.drawString(labels[i], x, height - padding + 18);
                }

                // ---- Y-axis value labels ----
                g2.setColor(new Color(108, 117, 125));
                for (int i = 0; i <= 4; i++) {
                    double value = maxValue - (i * (maxValue / 4.0));
                    String label = String.format("%.0f", value);
                    int x = padding - 28;
                    int y = padding + (chartHeight * i / 4) + 4;
                    g2.drawString(label, x, y);
                }

                g2.dispose();
            }

            private void drawEmptyState(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 180, 180));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                String msg = "No data";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g2.drawString(msg, x, y);
                g2.dispose();
            }
        };
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new GridLayout(Math.max(pointDetails.length, 1), 1, 4, 4));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        if (pointDetails == null || pointDetails.length == 0) {
            JLabel empty = new JLabel("No data");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            empty.setForeground(new Color(180, 180, 180));
            panel.add(empty);
            return panel;
        }

        for (int i = 0; i < pointDetails.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            item.setOpaque(false);

            JPanel colorBox = new JPanel();
            colorBox.setBackground(new Color(66, 133, 244));
            colorBox.setPreferredSize(new Dimension(10, 10));

            JLabel label = new JLabel(pointDetails[i]);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            label.setForeground(new Color(108, 117, 125));

            item.add(colorBox);
            item.add(label);
            panel.add(item);
        }
        return panel;
    }
}