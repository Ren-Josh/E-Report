package features.components;

import javax.swing.border.LineBorder;
import java.awt.*;

public class RoundedLineBorder extends LineBorder {
    private final int radius;

    public RoundedLineBorder(Color color, int radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(6, 10, 6, 10);
    }
}