package features.components;

import javax.swing.*;
import java.awt.*;

public class UICard extends JPanel {
    private int radius;
    private Color backgroundColor;
    private boolean shadowEnabled = true;
    private Color shadowColor = new Color(0, 0, 0, 30);
    private int shadowOffsetX = 5;
    private int shadowOffsetY = 5;
    private boolean showBorder = false;
    private Color borderColor = new Color(200, 200, 200);
    private int borderThickness = 1;

    public UICard(int radius, Color backgroundColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void setShadowEnabled(boolean enabled) {
        this.shadowEnabled = enabled;
        repaint();
    }

    public void setShadowColor(Color color) {
        this.shadowColor = color;
        repaint();
    }

    public void setShadowOffset(int x, int y) {
        this.shadowOffsetX = x;
        this.shadowOffsetY = y;
        repaint();
    }

    public void setShowBorder(boolean show) {
        this.showBorder = show;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setBorderThickness(int thickness) {
        this.borderThickness = thickness;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (shadowEnabled) {
            g2.setColor(shadowColor);
            g2.fillRoundRect(shadowOffsetX, shadowOffsetY,
                    getWidth() - shadowOffsetX * 2, getHeight() - shadowOffsetY * 2,
                    radius, radius);
        }

        g2.setColor(backgroundColor);
        int offset = shadowEnabled ? Math.max(shadowOffsetX, shadowOffsetY) : 0;
        g2.fillRoundRect(0, 0, getWidth() - offset, getHeight() - offset, radius, radius);

        if (showBorder) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderThickness));
            g2.drawRoundRect(0, 0, getWidth() - offset - 1, getHeight() - offset - 1, radius, radius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}