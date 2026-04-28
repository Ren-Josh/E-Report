package features.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import config.UIConfig;

public class UIButton extends JButton {
    public enum ButtonType {
        PRIMARY,
        OUTLINED,
        DISABLED,
        ELEVATED,
        GHOST
    }

    private Dimension customSize;
    private Color bg;
    private Color hoverBg;
    private Color pressedBg;
    private int radius;
    private ButtonType type;
    private Color borderColor;
    private boolean isHovered = false;

    // Text-color state (decoupled from Swing’s foreground property)
    private Color baseTextColor;
    private Color hoverTextColor;

    public UIButton(String text, Color bg, Dimension size, Font font, int radius, ButtonType type) {
        super(text);
        this.customSize = size;
        this.bg = bg != null ? bg : Color.WHITE;
        this.hoverBg = darken(this.bg, 0.88f);
        this.pressedBg = darken(this.bg, 0.72f);
        this.radius = radius;
        this.type = type;
        this.borderColor = UIConfig.OUTLINE_PRIMARY;

        setPreferredSize(size);
        if (size != null)
            setMinimumSize(size);

        setFont(font);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Default text colours per type
        switch (type) {
            case PRIMARY, ELEVATED -> setTextColor(Color.WHITE);
            case OUTLINED -> setTextColor(UIConfig.OUTLINE_TEXT);
            case GHOST -> setTextColor(UIConfig.TEXT_PRIMARY);
            case DISABLED -> setTextColor(UIConfig.DISABLED_TEXT);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                refreshTextColor();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                refreshTextColor();
                repaint();
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /* Public API */
    /* ------------------------------------------------------------------ */

    public void setHoverBg(Color c) {
        this.hoverBg = c;
        repaint();
    }

    public void setPressedBg(Color c) {
        this.pressedBg = c;
        repaint();
    }

    public void setBorderColor(Color c) {
        this.borderColor = c;
        repaint();
    }

    /** The colour used when the button is idle (not hovered / not disabled). */
    public void setTextColor(Color c) {
        this.baseTextColor = c;
        refreshTextColor();
    }

    /** The colour used while the mouse is over the button. */
    public void setTextHoverColor(Color c) {
        this.hoverTextColor = c;
        refreshTextColor();
    }

    /** Backwards-compat: external setForeground calls update our tracked colour. */
    @Override
    public void setForeground(Color c) {
        setTextColor(c);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refreshTextColor();
    }

    /* ------------------------------------------------------------------ */
    /* Internal helpers */
    /* ------------------------------------------------------------------ */

    /** Applies the correct foreground colour based on enabled / hover state. */
    private void refreshTextColor() {
        if (!isEnabled() || type == ButtonType.DISABLED) {
            super.setForeground(UIConfig.DISABLED_TEXT);
            return;
        }
        if (isHovered && hoverTextColor != null) {
            super.setForeground(hoverTextColor);
            return;
        }
        if (baseTextColor != null) {
            super.setForeground(baseTextColor);
            return;
        }
        super.setForeground(UIConfig.TEXT_PRIMARY);
    }

    private static Color darken(Color c, float factor) {
        if (c == null)
            return Color.WHITE;
        return new Color(
                Math.max((int) (c.getRed() * factor), 0),
                Math.max((int) (c.getGreen() * factor), 0),
                Math.max((int) (c.getBlue() * factor), 0),
                c.getAlpha());
    }

    @Override
    public Dimension getPreferredSize() {
        return customSize != null ? customSize : super.getPreferredSize();
    }

    /* ------------------------------------------------------------------ */
    /* Paint */
    /* ------------------------------------------------------------------ */

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            int r = Math.min(radius, Math.min(w / 2, h / 2));

            if (!isEnabled() || type == ButtonType.DISABLED) {
                g2.setColor(UIConfig.DISABLED_BG);
                g2.fillRoundRect(0, 0, w, h, r, r);
            } else if (type == ButtonType.PRIMARY || type == ButtonType.ELEVATED) {
                Color currentBg = bg;
                if (getModel().isPressed()) {
                    currentBg = pressedBg;
                } else if (isHovered || getModel().isRollover()) {
                    currentBg = hoverBg;
                }

                if (type == ButtonType.ELEVATED) {
                    g2.setColor(UIConfig.SHADOW_COLOR);
                    g2.fillRoundRect(
                            UIConfig.SHADOW_OFFSET_X,
                            UIConfig.SHADOW_OFFSET_Y,
                            w, h, r, r);
                }
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, w, h, r, r);
            } else if (type == ButtonType.OUTLINED) {
                Color fill = isHovered ? new Color(245, 247, 250) : getBackground();
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, r, r);

                float sw = 1.0f;
                g2.setColor(borderColor != null ? borderColor : UIConfig.OUTLINE_PRIMARY);
                g2.setStroke(new BasicStroke(sw));

                float inset = sw / 2.0f;
                Shape border = new RoundRectangle2D.Float(
                        inset, inset, w - sw, h - sw, r, r);
                g2.draw(border);
            } else if (type == ButtonType.GHOST) {
                if (isHovered) {
                    g2.setColor(new Color(0, 0, 0, 0.04f));
                    g2.fillRoundRect(0, 0, w, h, r, r);
                }
            }
        } finally {
            g2.dispose();
        }

        super.paintComponent(g);
    }

    /* ------------------------------------------------------------------ */
    /* Factories */
    /* ------------------------------------------------------------------ */

    public static UIButton createPrimaryButton(String text, Color bg, Dimension size, Font font, int radius) {
        return new UIButton(text, bg, size, font, radius, ButtonType.PRIMARY);
    }

    public static UIButton createOutlinedButton(String text, Color borderColor, Color textColor,
            Dimension size, Font font, int radius) {
        UIButton btn = new UIButton(text, null, size, font, radius, ButtonType.OUTLINED);
        btn.setBorderColor(borderColor);
        btn.setTextColor(textColor);
        return btn;
    }

    public static UIButton createTextButton(String text, Color fg, Color fgHover,
            Dimension size, Font font) {
        UIButton btn = new UIButton(text, null, size, font, 0, ButtonType.GHOST);
        btn.setTextColor(fg);
        btn.setTextHoverColor(fgHover);
        return btn;
    }
}