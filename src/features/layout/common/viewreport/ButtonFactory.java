package features.layout.common.viewreport;

import javax.swing.*;
import java.awt.*;

/**
 * Factory for creating consistently styled buttons used across the View Report
 * module.
 */
public final class ButtonFactory {
    private ButtonFactory() {
    }

    public static JButton createPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BOLD_12);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FIXED: force text to dead-center regardless of L&F
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setMargin(new Insets(0, 0, 0, 0));

        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BOLD_12);
        btn.setBackground(Color.WHITE);
        btn.setForeground(UIConstants.C_TEXT_MUTED);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FIXED: force text to dead-center regardless of L&F
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setMargin(new Insets(0, 0, 0, 0));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        return btn;
    }

    public static JButton createGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BOLD_12);
        btn.setForeground(UIConstants.C_TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FIXED: force text to dead-center regardless of L&F
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setMargin(new Insets(0, 0, 0, 0));

        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BOLD_12);
        btn.setBackground(UIConstants.C_REJECTED);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FIXED: force text to dead-center regardless of L&F
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setMargin(new Insets(0, 0, 0, 0));

        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }
}