package features.components;

import config.UIConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;

public class UIComboBox<E> extends JComboBox<E> {
    private boolean forcePlainBackground = false;
    private int radius = UIConfig.FIELD_RADIUS;
    private boolean isHovered = false;
    private Color idleBorderColor = UIConfig.FIELD_BORDER_IDLE;

    private Font baseFont;

    public enum SizePreset {
        SMALL, DEFAULT, LARGE
    }

    public enum ValidationState {
        IDLE, INVALID, VALID
    }

    private ValidationState state = ValidationState.IDLE;

    public UIComboBox(E[] items) {
        super(items);
        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setForeground(UIConfig.TEXT_PRIMARY);
        setFocusable(false);
        setOpaque(false);
        setBorder(new EmptyBorder(6, 12, 6, 10));

        this.baseFont = getFont();

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(4, 8, 4, 8));
                label.setFont(UIConfig.BODY);
                label.setForeground(UIConfig.TEXT_PRIMARY);
                label.setBackground(isSelected ? new Color(240, 240, 240) : Color.WHITE);
                return label;
            }
        });

        addActionListener(e -> {
            if (!isInvalidSelection() && state == ValidationState.INVALID) {
                setValid();
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                showPopup();
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                isHovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                isHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }
        });

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setContentAreaFilled(false);
                btn.setBorder(new EmptyBorder(0, 0, 0, 10));
                btn.setFocusPainted(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
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
        revalidate(); // ADDED: layout recalculates when font metrics change
    }

    public static <T> void applyPreset(UIComboBox<T> combo, int width) {
        combo.setPreferredSize(new Dimension(width, UIConfig.COMBOBOX_HEIGHT));
        combo.setFont(UIConfig.COMBOBOX_FONT);
        combo.setForeground(UIConfig.COMBOBOX_FG);
        combo.setForcePlainBackground(true);
        combo.setBorder(UIConfig.COMBOBOX_BORDER);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 4));

                // FIXED: use combo's live font so applySizePreset() is visible
                label.setFont(combo.getFont());

                String str = value != null ? value.toString() : "";
                boolean isPlaceholder = str.startsWith("All ") || str.startsWith("Select");
                if (isPlaceholder) {
                    label.setForeground(UIConfig.DISABLED_TEXT);
                    label.setFont(combo.getFont().deriveFont(Font.ITALIC));
                } else {
                    label.setForeground(UIConfig.COMBOBOX_FG);
                }
                return label;
            }
        });
        combo.baseFont = combo.getFont();
    }

    public void setIdleBorderColor(Color color) {
        this.idleBorderColor = color;
        repaint();
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
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

    public boolean isInvalidSelection() {
        Object value = getSelectedItem();
        return value == null ||
                value.toString().trim().isEmpty() ||
                value.toString().toLowerCase().startsWith("select");
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
            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            int r = Math.min(radius, Math.min(w / 2, h / 2));

            g2.setColor(isHovered ? UIConfig.FIELD_BG_HOVER : getBackground());
            g2.fillRoundRect(0, 0, w, h, r, r);

            Color borderColor = switch (state) {
                case INVALID -> UIConfig.FIELD_INVALID;
                case VALID -> UIConfig.FIELD_VALID;
                default -> isHovered ? UIConfig.FIELD_BORDER_HOVER : idleBorderColor;
            };

            float sw = UIConfig.FIELD_STROKE_WIDTH;
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(sw));

            float inset = sw / 2.0f;
            int bx = (int) Math.ceil(inset);
            int by = (int) Math.ceil(inset);
            int bw = Math.max(1, (int) Math.floor(w - inset * 2));
            int bh = Math.max(1, (int) Math.floor(h - inset * 2));
            int br = Math.min(r, Math.min(bw / 2, bh / 2));

            g2.drawRoundRect(bx, by, bw, bh, br, br);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    public void setForcePlainBackground(boolean force) {
        this.forcePlainBackground = force;
        if (force) {
            setOpaque(true);
            setBackground(Color.WHITE);
        }
        repaint();
    }
}