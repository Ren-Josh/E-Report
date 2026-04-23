package features.components;

import javax.swing.*;

import config.UIConfig;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class NavPanel extends JPanel {
    private JPanel menuContainer;
    private int selectedIndex = 0;

    // Current role-based config
    private String[] currentIconPaths;
    private String[] currentMenuItems;

    // MODIFY THIS: Icon size
    private int iconSize = 22;

    // Glass settings
    private float glassOpacity = 0.85f;
    private int cornerRadius = 20;

    public NavPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        // Outer margin
        setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        setPreferredSize(new Dimension(200, 0));

        // Menu container - REMOVED left padding to align left
        menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        // Only top/bottom padding, no left/right padding
        menuContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Default to resident menus until explicitly set
        setResidentMenus();

        add(menuContainer, BorderLayout.NORTH);
    }

    // ========== ROLE-BASED MENU SETTERS ==========

    /**
     * Load Resident menus: Dashboard, My Reports, Submit Report
     */
    public void setResidentMenus() {
        this.currentIconPaths = UIConfig.NAV_RESIDENT_ICON_PATHS;
        this.currentMenuItems = UIConfig.NAV_RESIDENT_ICON_LABELS;
        this.selectedIndex = 0;
        refreshMenu();
    }

    /**
     * Load Captain menus: Dashboard, Reports, Users, Analytics
     */
    public void setCaptainMenus() {
        this.currentIconPaths = UIConfig.NAV_CAPTAIN_ICON_PATHS;
        this.currentMenuItems = UIConfig.NAV_CAPTAIN_ICON_LABELS;
        this.selectedIndex = 0;
        refreshMenu();
    }

    /**
     * Load Secretary menus: Dashboard, Reports, Users, Case Management
     */
    public void setSecretaryMenus() {
        this.currentIconPaths = UIConfig.NAV_SECRETARY_ICON_PATHS;
        this.currentMenuItems = UIConfig.NAV_SECRETARY_ICON_LABELS;
        this.selectedIndex = 0;
        refreshMenu();
    }

    // ============================================

    private JPanel createMenuItem(int index) {
        boolean isSelected = (index == selectedIndex);
        String text = currentMenuItems[index];
        String iconPath = currentIconPaths[index];

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isSelected) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(100, 150, 255, 230));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                }
            }
        };

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        panel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        // Icon - no weight, fixed size
        JLabel iconLabel = loadIcon(iconPath, iconSize, iconSize);
        gbc.gridx = 0;
        gbc.weightx = 0; // No expansion
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(iconLabel, gbc);

        // Text - expand to fill and align left
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Arial", Font.BOLD, 14));
        textLabel.setForeground(isSelected ? Color.WHITE : new Color(50, 50, 50));
        textLabel.setHorizontalAlignment(SwingConstants.LEFT); // Explicit left align

        gbc.gridx = 1;
        gbc.weightx = 1.0; // Take remaining space
        gbc.fill = GridBagConstraints.HORIZONTAL; // Expand horizontally
        gbc.insets = new Insets(0, 8, 0, 10); // Right margin added
        panel.add(textLabel, gbc);

        // Click handler
        final int idx = index;
        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!isSelected)
                    panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            public void mouseClicked(MouseEvent e) {
                selectedIndex = idx;
                refreshMenu();
            }
        });

        return panel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight() - insets.top - insets.bottom;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 25));
        g2.fillRoundRect(x + 2, y + 2, w - 2, h - 2, cornerRadius, cornerRadius);

        // Glass background
        g2.setColor(new Color(255, 255, 255, (int) (255 * glassOpacity)));
        g2.fillRoundRect(x, y, w - 2, h - 2, cornerRadius, cornerRadius);

        // Top shine
        GradientPaint shine = new GradientPaint(
                x, y, new Color(255, 255, 255, 100),
                x, y + h / 4, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fillRoundRect(x, y, w - 2, h / 3, cornerRadius, cornerRadius);

        // Border
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x, y, w - 3, h - 3, cornerRadius, cornerRadius);

        g2.dispose();
    }

    private JLabel loadIcon(String path, int w, int h) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(w, h));

        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img != null) {
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            label.setOpaque(true);
            label.setBackground(new Color(150, 150, 150));
        }
        return label;
    }

    private void refreshMenu() {
        menuContainer.removeAll();
        for (int i = 0; i < currentMenuItems.length; i++) {
            menuContainer.add(createMenuItem(i));
            menuContainer.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        revalidate();
        repaint();
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < currentMenuItems.length) {
            selectedIndex = index;
            refreshMenu();
        }
    }
}