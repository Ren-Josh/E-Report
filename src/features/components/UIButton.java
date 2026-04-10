package features.components;

import javax.swing.JButton;
import java.awt.*;
import config.UIConfig;

public class UIButton extends JButton {
    public enum ButtonType {
        PRIMARY,
        OUTLINED,
        DISABLED,
        ELEVATED
    }
    
    private Dimension customSize;
    private Color bg;
    private int radius;
    private ButtonType type;

    public UIButton(String text, Color bg, Dimension size, Font font, int radius, ButtonType type) {
        super(text);
        this.customSize = size;
        this.bg = bg;
        this.radius = radius;
        this.type = type;

        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setFont(font);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        applyTextStyle();
    }

    private void applyTextStyle() {
        switch (type) {
            case OUTLINED -> setForeground(UIConfig.OUTLINE_TEXT);
            case DISABLED -> setForeground(UIConfig.DISABLED_TEXT);
            default -> setForeground(Color.WHITE);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return customSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // =========================
        // DISABLED STATE
        // =========================
        if (!isEnabled() || type == ButtonType.DISABLED) {
            g2.setColor(UIConfig.DISABLED_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            setForeground(UIConfig.DISABLED_TEXT);
        }

        // =========================
        // ELEVATED BUTTON
        // =========================
        else if (type == ButtonType.ELEVATED) {

            // Shadow
            g2.setColor(UIConfig.SHADOW_COLOR);
            g2.fillRoundRect(
                    UIConfig.SHADOW_OFFSET_X,
                    UIConfig.SHADOW_OFFSET_Y,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius);

            // Button
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }

        // =========================
        // OUTLINED BUTTON
        // =========================
        else if (type == ButtonType.OUTLINED) {

            // Transparent fill (optional)
            g2.setColor(new Color(0, 0, 0, 0));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Border
            g2.setColor(UIConfig.OUTLINE_PRIMARY);
            g2.setStroke(new BasicStroke(UIConfig.OUTLINE_THICKNESS));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }

        // =========================
        // PRIMARY BUTTON
        // =========================
        else {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }

        super.paintComponent(g);
        g2.dispose();
    }
}