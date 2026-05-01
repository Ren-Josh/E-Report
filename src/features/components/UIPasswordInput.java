package features.components;

import config.UIConfig;
import services.validation.UIValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class UIPasswordInput extends JPasswordField {

    private String placeholder;
    private ImageIcon lockIcon, eyeOnIcon, eyeOffIcon;
    private UIPasswordInput referenceField;
    private boolean isPasswordVisible = false;
    private int radius = UIConfig.FIELD_RADIUS;
    private int paddingLeft = 12, paddingRight = 10, paddingTop = 6, paddingBottom = 6;
    private boolean showEyeIcon = true;
    private boolean isHovered = false;
    private boolean forcePlainBackground = false;

    private Font baseFont;
    private float placeholderFontSize = 12f; // ADJUSTABLE: default 12pt

    public enum SizePreset {
        SMALL, DEFAULT, LARGE
    }

    private UIValidator.FieldType fieldType = UIValidator.FieldType.TEXT;
    private Color idleBorderColor = UIConfig.FIELD_BORDER_IDLE;
    private Color readonlyBackground = UIConfig.FIELD_BG_READONLY;

    public enum ValidationState {
        IDLE, INVALID, VALID
    }

    private ValidationState state = ValidationState.IDLE;

    public UIPasswordInput(int columns) {
        super(columns);
        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setForeground(UIConfig.TEXT_PRIMARY);
        setCaretColor(UIConfig.PRIMARY);
        setOpaque(false);
        applyPadding();

        this.baseFont = getFont();

        try {
            this.lockIcon = scaleIcon(UIConfig.LOCK_ICON_PATH);
            this.eyeOnIcon = scaleIcon(UIConfig.EYE_ICON_PATH);
            this.eyeOffIcon = scaleIcon(UIConfig.EYE_OFF_ICON_PATH);
            paddingLeft = 36;
            paddingRight = 36;
            applyPadding();
        } catch (Exception e) {
        }

        getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateLive();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateLive();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateLive();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(isOverEyeIcon(e.getX()) && showEyeIcon
                        ? new Cursor(Cursor.HAND_CURSOR)
                        : new Cursor(Cursor.TEXT_CURSOR));
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (showEyeIcon && isOverEyeIcon(e.getX()))
                    togglePasswordVisibility();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }
        });
    }

    public void applySizePreset() {
        applySizePreset(SizePreset.DEFAULT);
    }

    public void applySizePreset(SizePreset preset) {
        if (preset == null)
            preset = SizePreset.DEFAULT;
        switch (preset) {
            case SMALL -> setFont(baseFont.deriveFont(baseFont.getSize2D() - 2f));
            case LARGE -> setFont(baseFont.deriveFont(baseFont.getSize2D() + 2f));
            case DEFAULT -> setFont(baseFont);
        }
        repaint();
        revalidate();
    }

    public void setShowEyeIcon(boolean show) {
        this.showEyeIcon = show;
        paddingRight = show ? 36 : 10;
        applyPadding();
        repaint();
    }

    public void setIdleBorderColor(Color color) {
        this.idleBorderColor = color;
        repaint();
    }

    public void setReadonlyBackground(Color color) {
        this.readonlyBackground = color;
        repaint();
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setForcePlainBackground(boolean force) {
        this.forcePlainBackground = force;
        if (force) {
            setOpaque(true);
            setBackground(Color.WHITE);
        }
        repaint();
    }

    @Override
    public void setEditable(boolean b) {
        super.setEditable(b);
        setBackground(b ? Color.WHITE : readonlyBackground);
        repaint();
    }

    private boolean isOverEyeIcon(int mouseX) {
        return mouseX > getWidth() - 40;
    }

    public void setMatchTarget(UIPasswordInput other) {
        this.referenceField = other;
    }

    public void setPlaceholderFontSize(float size) {
        this.placeholderFontSize = size;
        repaint();
    }

    private void validateLive() {
        String value = getValue();
        if (value.isEmpty()) {
            clearError();
            return;
        }
        boolean isValid = UIValidator.isValidField(this.fieldType, value);
        if (isValid)
            setValid();
        else
            setError();

        if (referenceField != null) {
            if (value.equals(referenceField.getValue()))
                setValid();
            else
                setError();
        }
    }

    private void applyPadding() {
        setBorder(new EmptyBorder(paddingTop, paddingLeft, paddingBottom, paddingRight));
    }

    private ImageIcon scaleIcon(String path) {
        if (path == null)
            return null;
        return new ImageIcon(new ImageIcon(path).getImage()
                .getScaledInstance(18, 18, Image.SCALE_SMOOTH));
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        setEchoChar(isPasswordVisible ? (char) 0 : '\u2022');
        repaint();
    }

    public void setFieldType(UIValidator.FieldType type) {
        this.fieldType = type;
    }

    public void setError() {
        state = ValidationState.INVALID;
        repaint();
    }

    public void setValid() {
        state = ValidationState.VALID;
        repaint();
    }

    public void clearError() {
        state = ValidationState.IDLE;
        repaint();
    }

    public ValidationState getState() {
        return state;
    }

    public String getValue() {
        return new String(getPassword()).trim();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (forcePlainBackground) {
            g.setColor(getBackground());
            g.fillRect(0, 0, Math.max(1, getWidth()), Math.max(1, getHeight()));
            super.paintComponent(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            int r = Math.min(radius, Math.min(w / 2, h / 2));

            g2.setColor(isHovered ? UIConfig.FIELD_BG_HOVER : getBackground());
            g2.fillRoundRect(0, 0, w, h, r, r);

            Color border = switch (state) {
                case INVALID -> UIConfig.FIELD_INVALID;
                case VALID -> UIConfig.FIELD_VALID;
                default -> isHovered ? UIConfig.FIELD_BORDER_HOVER : idleBorderColor;
            };

            float sw = UIConfig.FIELD_STROKE_WIDTH;
            g2.setColor(border);
            g2.setStroke(new BasicStroke(sw));

            float inset = sw / 2.0f;
            int bx = (int) Math.ceil(inset);
            int by = (int) Math.ceil(inset);
            int bw = Math.max(1, (int) Math.floor(w - inset * 2));
            int bh = Math.max(1, (int) Math.floor(h - inset * 2));
            int br = Math.min(r, Math.min(bw / 2, bh / 2));

            g2.drawRoundRect(bx, by, bw, bh, br, br);

            if (lockIcon != null) {
                Composite oldComp = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                int iconY = Math.max(0, (h - 18) / 2);
                g2.drawImage(lockIcon.getImage(), 10, iconY, 18, 18, null);
                g2.setComposite(oldComp);
            }

            if (eyeOnIcon != null && showEyeIcon) {
                Composite oldComp = g2.getComposite();
                Image eye = isPasswordVisible ? eyeOffIcon.getImage() : eyeOnIcon.getImage();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                int iconY = Math.max(0, (h - 18) / 2);
                g2.drawImage(eye, w - 28, iconY, 18, 18, null);
                g2.setComposite(oldComp);
            }

            super.paintComponent(g);

            // Smaller, lighter placeholder (default 12pt italic)
            if (placeholder != null && getPassword().length == 0) {
                g2.setColor(UIConfig.FIELD_PLACEHOLDER);
                Font origFont = g2.getFont();
                g2.setFont(origFont.deriveFont(Font.ITALIC, placeholderFontSize));
                FontMetrics fm = g2.getFontMetrics();
                int y = Math.max(fm.getAscent(), (h - fm.getHeight()) / 2 + fm.getAscent());
                g2.drawString(placeholder, getInsets().left, y);
                g2.setFont(origFont);
            }
        } finally {
            g2.dispose();
        }
    }
}