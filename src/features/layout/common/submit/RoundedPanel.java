package features.layout.common.submit;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable rounded-corner panel used as the content card.
 */
public class RoundedPanel extends JPanel {
    private final int radius;
    private final Color fillColor;

    public RoundedPanel(int radius, Color fillColor) {
        super();
        this.radius = radius;
        this.fillColor = fillColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}