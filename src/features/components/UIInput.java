package features.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import config.UIConfig;

public class UIInput extends JTextField {
    private String placeholder;
    private ImageIcon icon;
    private int cornerRadius = 15; // Default roundness

    public UIInput(int columns, String iconPath) {
        this(columns);
        if (iconPath != null) {
            this.icon = new ImageIcon(new ImageIcon(iconPath).getImage()
                    .getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            updateBorder(40);
        }
    }

    public UIInput(int columns) {
        super(columns);
        setFont(UIConfig.BODY);
        setOpaque(false); // Required to prevent the square background from painting
        setBackground(Color.WHITE);
        setForeground(UIConfig.TEXT_PRIMARY);
        setCaretColor(UIConfig.PRIMARY);
        updateBorder(15);
    }

    // New method to adjust roundness dynamically
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private void updateBorder(int leftPadding) {
        // Use a null border for the outer so we can draw the rounded line ourselves
        setBorder(new EmptyBorder(10, leftPadding, 10, 15));
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Rounded Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // 2. Draw Rounded Border (matches the light gray in reference)
        g2.setColor(new Color(220, 220, 220));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // 3. Draw Icon
        int iconSize = 20;
        int yLocation = (getHeight() - iconSize) / 2;
        if (icon != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2.drawImage(icon.getImage(), 12, yLocation, iconSize, iconSize, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Call super AFTER drawing background but BEFORE placeholder
        super.paintComponent(g2);

        // 4. Draw Placeholder
        if (placeholder != null && getText().isEmpty()) {
            g2.setColor(new Color(180, 180, 180));
            FontMetrics fm = g2.getFontMetrics();
            int yPos = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            int xPos = (icon != null) ? 40 : 15;
            g2.drawString(placeholder, xPos, yPos);
        }

        g2.dispose();
    }
}