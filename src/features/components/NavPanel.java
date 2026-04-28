package features.components;

import javax.swing.*;
import config.UIConfig;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NavPanel extends JPanel {
    private JPanel menuContainer;
    private static int selectedIndex = 0;

    // Holds all current navigation items (icon, label, action)
    private List<NavItem> navItems = new ArrayList<>();

    // MODIFY THIS: Icon size
    private int iconSize = 22;

    // Glass settings
    private float glassOpacity = 0.85f;
    private int cornerRadius = 20;

    public NavPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        setPreferredSize(new Dimension(200, 0));

        menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        add(menuContainer, BorderLayout.NORTH);
    }

    // ========== NAV ITEM DATA CLASS ==========

    public static class NavItem {
        private final String iconPath;
        private final String label;
        private final String navigateTo; // e.g., "dashboard", "reports"
        private final Consumer<String> action; // Optional custom action override

        public NavItem(String iconPath, String label, String navigateTo) {
            this(iconPath, label, navigateTo, null);
        }

        public NavItem(String iconPath, String label, String navigateTo, Consumer<String> action) {
            this.iconPath = iconPath;
            this.label = label;
            this.navigateTo = navigateTo;
            this.action = action;
        }

        public String getIconPath() {
            return iconPath;
        }

        public String getLabel() {
            return label;
        }

        public String getNavigateTo() {
            return navigateTo;
        }

        public boolean hasCustomAction() {
            return action != null;
        }

        public void execute(String navigateTo) {
            if (action != null)
                action.accept(navigateTo);
        }
    }

    // ========== ROLE-BASED MENU SETTERS ==========

    public void setResidentMenus(Consumer<String> navigator) {
        String[] paths = UIConfig.NAV_RESIDENT_ICON_PATHS;
        String[] labels = UIConfig.NAV_RESIDENT_ICON_LABELS;
        String[] targets = UIConfig.NAV_RESIDENT_TARGET;

        navItems = new ArrayList<>();
        for (int i = 0; i < paths.length; i++) {
            navItems.add(new NavItem(paths[i], labels[i], targets[i]));
        }
        buildMenus(navigator);
    }

    public void setCaptainMenus(Consumer<String> navigator) {
        String[] paths = UIConfig.NAV_CAPTAIN_ICON_PATHS;
        String[] labels = UIConfig.NAV_CAPTAIN_ICON_LABELS;
        String[] targets = UIConfig.NAV_CAPTAIN_TARGET;

        navItems = new ArrayList<>();
        for (int i = 0; i < paths.length; i++) {
            navItems.add(new NavItem(paths[i], labels[i], targets[i]));
        }
        buildMenus(navigator);
    }

    public void setSecretaryMenus(Consumer<String> navigator) {
        String[] paths = UIConfig.NAV_SECRETARY_ICON_PATHS;
        String[] labels = UIConfig.NAV_SECRETARY_ICON_LABELS;
        String[] targets = UIConfig.NAV_SECRETARY_TARGET;

        navItems = new ArrayList<>();
        for (int i = 0; i < paths.length; i++) {
            navItems.add(new NavItem(paths[i], labels[i], targets[i]));
        }
        buildMenus(navigator);
    }

    // ========== CUSTOM MENU SETTER (most flexible) ==========

    /**
     * Set completely custom navigation items with their own actions.
     * Example:
     * navPanel.setCustomMenus(navigator, List.of(
     * new NavItem("icon.png", "Dashboard", "dashboard"),
     * new NavItem("icon.png", "Reports", "reports", route -> {
     * // custom logic before navigate
     * navigator.accept(route);
     * })
     * ));
     */
    public void setCustomMenus(Consumer<String> navigator, List<NavItem> items) {
        this.navItems = new ArrayList<>(items);
        buildMenus(navigator);
    }

    // ============================================

    private Consumer<String> currentNavigator;

    private void buildMenus(Consumer<String> navigator) {
        this.currentNavigator = navigator;
        refreshMenu();
    }

    private JPanel createMenuItem(int index) {
        boolean isSelected = (index == selectedIndex);
        NavItem item = navItems.get(index);

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

        JLabel iconLabel = loadIcon(item.getIconPath(), iconSize, iconSize);
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(iconLabel, gbc);

        JLabel textLabel = new JLabel(item.getLabel());
        textLabel.setFont(new Font("Arial", Font.BOLD, 14));
        textLabel.setForeground(isSelected ? Color.WHITE : new Color(50, 50, 50));
        textLabel.setHorizontalAlignment(SwingConstants.LEFT);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 8, 0, 10);
        panel.add(textLabel, gbc);

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

                NavItem clickedItem = navItems.get(idx);
                if (clickedItem.hasCustomAction()) {
                    clickedItem.execute(clickedItem.getNavigateTo());
                } else if (currentNavigator != null) {
                    currentNavigator.accept(clickedItem.getNavigateTo());
                }
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

        g2.setColor(new Color(0, 0, 0, 25));
        g2.fillRoundRect(x + 2, y + 2, w - 2, h - 2, cornerRadius, cornerRadius);

        g2.setColor(new Color(255, 255, 255, (int) (255 * glassOpacity)));
        g2.fillRoundRect(x, y, w - 2, h - 2, cornerRadius, cornerRadius);

        GradientPaint shine = new GradientPaint(
                x, y, new Color(255, 255, 255, 100),
                x, y + h / 4, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fillRoundRect(x, y, w - 2, h / 3, cornerRadius, cornerRadius);

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
        for (int i = 0; i < navItems.size(); i++) {
            menuContainer.add(createMenuItem(i));
            menuContainer.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        revalidate();
        repaint();
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < navItems.size()) {
            selectedIndex = index;
            refreshMenu();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedRoute() {
        if (selectedIndex >= 0 && selectedIndex < navItems.size()) {
            return navItems.get(selectedIndex).getNavigateTo();
        }
        return null;
    }
}