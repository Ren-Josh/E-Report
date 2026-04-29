package features.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import app.E_Report;
import config.UIConfig;

public class HeaderPanel extends JPanel {
    private final E_Report app;

    private JLabel userNameLabel;
    private JLabel userRoleLabel;
    private JLabel userIconLabel;
    private JLabel logoLabel;
    private JLabel titleLabel;
    private JLabel arrowLabel;

    private JWindow disclosurePopup;
    private JPanel disclosureContent;
    private boolean disclosureVisible = false;

    private float glassOpacity = 0.9f;
    private Color glassBorderColor = new Color(255, 255, 255, 180);
    private Color shadowColor = new Color(0, 0, 0, 30);

    public HeaderPanel(E_Report app) {
        this.app = app;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setPreferredSize(new Dimension(720, 70));

        // Left side: Logo and Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        logoLabel = createResizableIconLabel(UIConfig.LOGO_PATH, 50, 50);
        leftPanel.add(logoLabel);

        titleLabel = new JLabel("E-Reporting System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        leftPanel.add(titleLabel);

        add(leftPanel, BorderLayout.WEST);

        // Right side: User info with disclosure trigger
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);

        userNameLabel = new JLabel(app.getCurrentUserFullName());
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userNameLabel.setForeground(new Color(50, 50, 50));
        userNameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        userNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        userRoleLabel = new JLabel(app.getCurrentUserRole());
        userRoleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        userRoleLabel.setForeground(new Color(120, 120, 120));
        userRoleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        userRoleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        userInfoPanel.add(userNameLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        userInfoPanel.add(userRoleLabel);

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        rightPanel.add(userInfoPanel, gbc);

        userIconLabel = createResizableIconLabel(UIConfig.USER_ICON_PATH, 35, 35);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 6);
        rightPanel.add(userIconLabel, gbc);

        arrowLabel = new JLabel("▼");
        arrowLabel.setFont(new Font("Arial", Font.BOLD, 10));
        arrowLabel.setForeground(new Color(100, 100, 100));
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        rightPanel.add(arrowLabel, gbc);

        add(rightPanel, BorderLayout.EAST);

        createDisclosurePopup();

        MouseAdapter toggleListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleDisclosure();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                userNameLabel.setForeground(new Color(100, 150, 255));
                arrowLabel.setForeground(new Color(100, 150, 255));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                userNameLabel.setForeground(new Color(50, 50, 50));
                arrowLabel.setForeground(new Color(100, 100, 100));
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        };

        userNameLabel.addMouseListener(toggleListener);
        userRoleLabel.addMouseListener(toggleListener);
        userIconLabel.addMouseListener(toggleListener);
        arrowLabel.addMouseListener(toggleListener);
        userInfoPanel.addMouseListener(toggleListener);
        rightPanel.addMouseListener(toggleListener);

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent && disclosureVisible) {
                MouseEvent me = (MouseEvent) event;
                if (me.getID() == MouseEvent.MOUSE_CLICKED) {
                    Point popupLoc = disclosurePopup.getLocationOnScreen();
                    Rectangle popupBounds = new Rectangle(popupLoc, disclosurePopup.getSize());

                    Point headerLoc = HeaderPanel.this.getLocationOnScreen();
                    Rectangle headerBounds = new Rectangle(headerLoc, HeaderPanel.this.getSize());

                    Point clickPoint = me.getLocationOnScreen();

                    if (!popupBounds.contains(clickPoint) && !headerBounds.contains(clickPoint)) {
                        hideDisclosure();
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    // ==================== Disclosure Popup ====================

    private void createDisclosurePopup() {
        disclosurePopup = new JWindow(app);
        disclosurePopup.setFocusableWindowState(false);
        disclosurePopup.setAlwaysOnTop(false);

        disclosureContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(3, 3, w - 3, h - 3, 14, 14);

                g2.setColor(new Color(255, 255, 255, 250));
                g2.fillRoundRect(0, 0, w - 3, h - 3, 14, 14);

                g2.setColor(new Color(220, 220, 220, 200));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 4, h - 4, 14, 14);

                g2.dispose();
            }
        };

        disclosureContent.setLayout(new BoxLayout(disclosureContent, BoxLayout.Y_AXIS));
        disclosureContent.setOpaque(false);
        disclosureContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        disclosureContent.setBackground(new Color(0, 0, 0, 0));

        JButton profileBtn = createDisclosureButton("Profile", UIConfig.USER_ICON_PATH);
        profileBtn.addActionListener(e -> {
            onProfileClicked();
            hideDisclosure();
        });

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(230, 230, 230));
        separator.setBackground(new Color(230, 230, 230));

        JButton logoutBtn = createDisclosureButton("Logout", UIConfig.LOGOUT_ICON_PATH);
        logoutBtn.setForeground(new Color(200, 60, 60));
        logoutBtn.addActionListener(e -> {
            onLogoutClicked();
            hideDisclosure();
        });

        disclosureContent.add(profileBtn);
        disclosureContent.add(Box.createRigidArea(new Dimension(0, 6)));
        disclosureContent.add(separator);
        disclosureContent.add(Box.createRigidArea(new Dimension(0, 6)));
        disclosureContent.add(logoutBtn);

        disclosurePopup.add(disclosureContent);
        disclosurePopup.pack();

        app.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                if (disclosurePopup.isVisible()) {
                    updateDisclosurePosition();
                }
            }
        });
    }

    private void toggleDisclosure() {
        if (disclosureVisible) {
            hideDisclosure();
        } else {
            showDisclosure();
        }
    }

    private void showDisclosure() {
        updateDisclosurePosition();
        disclosurePopup.setVisible(true);
        disclosureVisible = true;
        arrowLabel.setText("▲");
    }

    private void hideDisclosure() {
        disclosurePopup.setVisible(false);
        disclosureVisible = false;
        arrowLabel.setText("▼");
    }

    private void updateDisclosurePosition() {
        Point userInfoLoc = userNameLabel.getLocationOnScreen();
        Dimension userInfoSize = userNameLabel.getParent().getSize();
        Dimension popupSize = disclosurePopup.getPreferredSize();

        int x = userInfoLoc.x + userInfoSize.width - (popupSize.width / 2);
        int y = userInfoLoc.y + userInfoSize.height + 5;

        disclosurePopup.setLocation(x, y);
    }

    private JButton createDisclosureButton(String text, String iconPath) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(new Color(240, 245, 255));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                super.paintComponent(g2);
                g2.dispose();
            }
        };

        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setForeground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(150, 38));
        btn.setMinimumSize(new Dimension(150, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        try {
            BufferedImage img = ImageIO.read(new File(iconPath));
            if (img != null) {
                Image scaled = img.getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            // No icon fallback
        }

        return btn;
    }

    protected void onProfileClicked() {
        if (app != null) {
            app.navigate("profile");
        }
    }

    protected void onLogoutClicked() {
        if (app != null) {
            app.logout();
        }
    }

    // ==================== Painting & Layout ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int shadowOffset = 1;

        g2.setColor(shadowColor);
        g2.fillRoundRect(shadowOffset, shadowOffset, width - shadowOffset, height - shadowOffset, 20, 20);

        g2.setColor(glassBorderColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, width - shadowOffset - 1, height - shadowOffset - 1, 20, 20);

        Color glassWhite = new Color(255, 255, 255, (int) (255 * glassOpacity));
        g2.setColor(glassWhite);
        g2.fillRoundRect(0, 0, width - shadowOffset, height - shadowOffset, 20, 20);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 255, 255, 50),
                0, height / 2, new Color(255, 255, 255, 0));
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width - shadowOffset, height / 2, 20, 20);

        g2.dispose();
    }

    /**
     * Refresh labels from the centralized app data. Call after login / profile
     * update.
     */
    public void refresh() {
        userNameLabel.setText(app.getCurrentUserFullName());
        userRoleLabel.setText(app.getCurrentUserRole());
        revalidate();
        repaint();
    }

    /** Direct label override (does NOT touch the app model). */
    public void setUserInfo(String fullName, String role) {
        userNameLabel.setText(fullName);
        userRoleLabel.setText(role);
        revalidate();
        repaint();
    }

    public void setUserRole(String role) {
        userRoleLabel.setText(role);
        revalidate();
        repaint();
    }

    public void setGlassOpacity(float opacity) {
        this.glassOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
        repaint();
    }

    public void setGlassBorderColor(Color color) {
        this.glassBorderColor = color;
        repaint();
    }

    private JLabel createResizableIconLabel(String imagePath, int targetWidth, int targetHeight) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(targetWidth, targetHeight));

        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage != null) {
                Image scaledImage = originalImage.getScaledInstance(
                        targetWidth, targetHeight, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
            } else {
                label.setOpaque(true);
                label.setBackground(new Color(200, 200, 200, 150));
                label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        } catch (IOException e) {
            label.setOpaque(true);
            label.setBackground(new Color(200, 200, 200, 150));
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }

        return label;
    }

    public void updateLogoIcon(String newPath) {
        updateIcon(logoLabel, newPath, 50, 50);
    }

    public void updateUserIcon(String newPath) {
        updateIcon(userIconLabel, newPath, 35, 35);
    }

    private void updateIcon(JLabel label, String imagePath, int w, int h) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage != null) {
                Image scaledImage = originalImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
                label.setOpaque(false);
                label.setBackground(null);
                label.setBorder(null);
            }
        } catch (IOException e) {
            System.err.println("Could not load image: " + imagePath);
        }
        revalidate();
        repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (disclosurePopup != null) {
            disclosurePopup.dispose();
        }
    }
}