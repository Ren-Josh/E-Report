package features.components;

import javax.swing.*;
import java.awt.*;

public class GlassPanel extends JPanel {
    protected float glassOpacity = 0.90f;
    protected int cornerRadius = 15;
    protected Color glassColor = new Color(255, 255, 255);
    
    public GlassPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }
    
    public GlassPanel() {
        this(new BorderLayout());
    }
    
    public void setGlassOpacity(float opacity) {
        this.glassOpacity = opacity;
        repaint();
    }
    
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
    
    public void setGlassColor(Color color) {
        this.glassColor = color;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        // Shadow
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(2, 2, w - 2, h - 2, cornerRadius, cornerRadius);
        
        // Glass background
        g2.setColor(new Color(glassColor.getRed(), glassColor.getGreen(), 
                             glassColor.getBlue(), (int)(255 * glassOpacity)));
        g2.fillRoundRect(0, 0, w - 2, h - 2, cornerRadius, cornerRadius);
        
        // Border
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 3, h - 3, cornerRadius, cornerRadius);
        
        g2.dispose();
    }
}
