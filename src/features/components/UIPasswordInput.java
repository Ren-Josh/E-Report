package features.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import config.UIConfig;

public class UIPasswordInput extends JPasswordField {
    private String placeholder;
    private ImageIcon lockIcon;
    private ImageIcon eyeOnIcon;
    private ImageIcon eyeOffIcon;
    private boolean isPasswordVisible = false;
    private int cornerRadius = 15;

    // Simplified constructor: No icon parameters needed
    public UIPasswordInput(int columns) {
        super(columns);
        setOpaque(false);
        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setCaretColor(UIConfig.PRIMARY);
        
        // Load Icons directly from UIConfig
        // Note: Ensure these constants exist in your UIConfig.java
        try {
            this.lockIcon = scaleIcon(UIConfig.LOCK_ICON_PATH);
            this.eyeOnIcon = scaleIcon(UIConfig.EYE_ICON_PATH);
            this.eyeOffIcon = scaleIcon(UIConfig.EYE_OFF_ICON_PATH);
        } catch (Exception e) {
            System.err.println("Error loading password icons: " + e.getMessage());
        }

        updateBorder();

        // Toggle Functionality - Switched to mousePressed for better responsiveness
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if click is within the eye icon area (right side)
                if (e.getX() > getWidth() - 40) {
                    togglePasswordVisibility();
                }
            }
        });
        
        // Change cursor to HAND when hovering over the eye
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getX() > getWidth() - 40) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.TEXT_CURSOR));
                }
            }
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        // (char) 0 makes text visible, '\u2022' is the standard bullet dot
        setEchoChar(isPasswordVisible ? (char) 0 : '\u2022');
        repaint();
        revalidate();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private void updateBorder() {
        int left = (lockIcon != null) ? 40 : 15;
        int right = (eyeOnIcon != null) ? 40 : 15;
        setBorder(new EmptyBorder(10, left, 10, right));
    }

    private ImageIcon scaleIcon(String path) {
        if (path == null) return null;
        return new ImageIcon(new ImageIcon(path).getImage()
                .getScaledInstance(20, 20, Image.SCALE_SMOOTH));
    }

    public void setPlaceholder(String placeholder) { 
        this.placeholder = placeholder; 
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. Draw Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // 2. Draw Border
        g2.setColor(new Color(220, 220, 220));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        super.paintComponent(g2);

        int iconY = (getHeight() - 20) / 2;

        // 3. Draw Lock Icon
        if (lockIcon != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2.drawImage(lockIcon.getImage(), 12, iconY, 20, 20, null);
        }

        // 4. Draw Eye Icon (Toggles image based on state)
        if (eyeOnIcon != null && eyeOffIcon != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            Image currentEye = !isPasswordVisible ? eyeOnIcon.getImage() : eyeOffIcon.getImage();
            g2.drawImage(currentEye, getWidth() - 32, iconY, 20, 20, null);
        }

        // 5. Placeholder
        if (placeholder != null && getPassword().length == 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(new Color(170, 170, 170));
            FontMetrics fm = g2.getFontMetrics();
            int yPos = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, lockIcon != null ? 40 : 15, yPos);
        }

        g2.dispose();
    }
}