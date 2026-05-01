package features.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import config.UIConfig;
import services.validation.UIValidator;

public class UIInput extends JTextField {
    private String placeholder;
    private ImageIcon icon;
    private int radius = UIConfig.FIELD_RADIUS;
    private UIValidator.FieldType fieldType = UIValidator.FieldType.TEXT;
    private Color idleBorderColor = UIConfig.FIELD_BORDER_IDLE;
    private Color readonlyBackground = UIConfig.FIELD_BG_READONLY;
    private boolean isHovered = false;
    private boolean forcePlainBackground = false;

    private Font baseFont;
    private float placeholderFontSize = 12f; // ADJUSTABLE: default 12pt

    public enum SizePreset {
        SMALL, DEFAULT, LARGE
    }

    public enum ValidationState {
        IDLE, INVALID, VALID
    }

    private ValidationState state = ValidationState.IDLE;

    public UIInput(int columns) {
        super(columns);
        setOpaque(false);
        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setForeground(UIConfig.TEXT_PRIMARY);
        setCaretColor(UIConfig.PRIMARY);
        applyPadding(12);

        this.baseFont = getFont();

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

    public UIInput(int columns, String iconPath) {
        this(columns);
        if (iconPath != null) {
            this.icon = new ImageIcon(new ImageIcon(iconPath).getImage()
                    .getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            applyPadding(36);
        }
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

    public UIValidator.FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(UIValidator.FieldType type) {
        this.fieldType = type;
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

    public void setLimit(int max, boolean numericOnly) {
        ((AbstractDocument) getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (numericOnly && !text.matches("\\d*"))
                    return;
                int currentLength = fb.getDocument().getLength();
                if ((currentLength + text.length() - length) <= max) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
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
        if (UIValidator.isValidField(this.fieldType, value))
            setValid();
        else
            setError();
    }

    public String getValue() {
        return getText() == null ? "" : getText().trim();
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

    public void setPlaceholder(String p) {
        this.placeholder = p;
        repaint();
    }

    public ValidationState getState() {
        return state;
    }

    private void applyPadding(int left) {
        setBorder(new EmptyBorder(6, left, 6, 10));
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

            if (icon != null) {
                Composite oldComp = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                int iconY = Math.max(0, (h - 18) / 2);
                g2.drawImage(icon.getImage(), 10, iconY, 18, 18, null);
                g2.setComposite(oldComp);
            }

            super.paintComponent(g);

            // Smaller, lighter placeholder (default 12pt italic)
            if (placeholder != null && getText().isEmpty()) {
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