package features.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import config.UIConfig;

public class UIComboBox<E> extends JComboBox<E> {
    private int radius = 10;

    public UIComboBox(E[] items) {
        super(items);
        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        setFocusable(false);
        
        // Remove default clunky styling
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = super.createArrowButton();
                button.setContentAreaFilled(false);
                button.setBorder(new EmptyBorder(0, 0, 0, 5));
                return button;
            }
        });
        
        setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Paint Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        
        // Paint Border (Matches UIInput style)
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        
        super.paintComponent(g);
        g2.dispose();
    }
}