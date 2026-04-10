package features.components;

import javax.swing.*;
import java.awt.*;

public class UICard extends JPanel {
    private int radius;
    private Color backgroundColor;

    public UICard(int radius, Color backgroundColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
        // Use BoxLayout but ensure children are centered
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, radius, radius);

        // Draw Card
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, radius, radius);
        
        g2.dispose();
    }
}