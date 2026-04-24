package features.components;

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private Color hoverBg;
    private Color pressedBg;
    private int radius;
    private ButtonType type;
    private Color borderColor;
    private boolean isHovered = false;

    public UIButton(String text, Color bg, Dimension size, Font font, int radius, ButtonType type) {
        super(text);
        this.customSize = size;
        this.bg = bg != null ? bg : Color.WHITE;
        // FIX: darken this.bg (already null-safe), not the raw bg parameter
        this.hoverBg = darken(this.bg, 0.85f);
        this.pressedBg = darken(this.bg, 0.70f);
        this.radius = radius;
        this.type = type;
        this.borderColor = UIConfig.OUTLINE_PRIMARY;

        setPreferredSize(size);
        setMinimumSize(size);
        setFont(font);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        applyTextStyle();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    /** Darken a color by a factor (0.0 = black, 1.0 = original) */
    private static Color darken(Color c, float factor) {
        if (c == null)
            return Color.WHITE; // safety net
        return new Color(
                Math.max((int) (c.getRed() * factor), 0),
                Math.max((int) (c.getGreen() * factor), 0),
                Math.max((int) (c.getBlue() * factor), 0),
                c.getAlpha());
    }

    public void setHoverBg(Color hoverBg) {
        this.hoverBg = hoverBg;
        repaint();
    }

    public void setPressedBg(Color pressedBg) {
        this.pressedBg = pressedBg;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
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

        Color currentBg = bg;
        if (type == ButtonType.PRIMARY || type == ButtonType.ELEVATED) {
            if (getModel().isPressed()) {
                currentBg = pressedBg;
            } else if (isHovered || getModel().isRollover()) {
                currentBg = hoverBg;
            }
        }

        if (!isEnabled() || type == ButtonType.DISABLED) {
            g2.setColor(UIConfig.DISABLED_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            setForeground(UIConfig.DISABLED_TEXT);
        } else if (type == ButtonType.ELEVATED) {
            g2.setColor(UIConfig.SHADOW_COLOR);
            g2.fillRoundRect(
                    UIConfig.SHADOW_OFFSET_X,
                    UIConfig.SHADOW_OFFSET_Y,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius);
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        } else if (type == ButtonType.OUTLINED) {
            g2.setColor(new Color(0, 0, 0, 0));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(borderColor != null ? borderColor : UIConfig.OUTLINE_PRIMARY);
            g2.setStroke(new BasicStroke(UIConfig.OUTLINE_THICKNESS));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        } else {
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }

        super.paintComponent(g);
        g2.dispose();
    }
}