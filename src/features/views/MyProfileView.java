package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.layout.common.ProfilePanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * MyProfileView - A dedicated view for user profile management.
 * Features a fixed header and navigation panel with the ProfilePanel as main
 * content.
 */
public class MyProfileView extends JPanel {
    private final E_Report app;
    private final HeaderPanel header;
    private final NavPanel nav;
    private final ProfilePanel profilePanel;

    public MyProfileView(E_Report app) {
        this.app = app;

        // Initialize components
        header = new HeaderPanel(app);
        nav = new NavPanel();
        profilePanel = new ProfilePanel(app);

        // Setup layout and navigation
        initializeLayout();
        setupNavigation();
    }

    /**
     * Initializes the main layout structure with background panel
     */
    private void initializeLayout() {
        setLayout(new BorderLayout());

        // Create background panel with consistent styling from DashboardView
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add components to background panel
        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(profilePanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    /**
     * Setup role-based navigation menus
     */
    private void setupNavigation() {
        UserSession session = app.getUserSession();
        if (session == null)
            return;

        String role = session.getRole();
        if (role.equalsIgnoreCase("captain")) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (role.equalsIgnoreCase("secretary")) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else if (role.equalsIgnoreCase("resident")) {
            nav.setResidentMenus(route -> app.navigate(route));
        }
    }

    // ==================== Getters ====================

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNav() {
        return nav;
    }

    public ProfilePanel getProfilePanel() {
        return profilePanel;
    }

    // ==================== Public API ====================

    /**
     * Refreshes the profile data from the application model
     * Call this when navigating to the view to ensure data is current
     */
    public void refreshProfile() {
        profilePanel.loadFromApp();
    }
}
