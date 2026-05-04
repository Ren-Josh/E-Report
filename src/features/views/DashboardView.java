package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.core.dashboardpanel.captain.CaptainDashboardPanel;
import features.core.dashboardpanel.resident.ResidentDashboardPanel;
import features.core.dashboardpanel.secretary.SecretaryDashboardPanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Composite view panel for the Dashboard screen.
 * Dynamically instantiates the correct dashboard content panel based on the
 * current user's role (Captain, Secretary, or Resident).
 */
public class DashboardView extends JPanel {
    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /** Top header bar displaying user info and system branding. */
    private HeaderPanel header;
    /** Left-side navigation panel with role-specific menu items. */
    private NavPanel nav;
    /** Captain-specific dashboard content panel; null if user is not a Captain. */
    private CaptainDashboardPanel cdp;
    /**
     * Secretary-specific dashboard content panel; null if user is not a Secretary.
     */
    private SecretaryDashboardPanel sdp;
    /**
     * Resident-specific dashboard content panel; null if user is not a Resident.
     */
    private ResidentDashboardPanel rdp;

    /**
     * Constructs the Dashboard view, assembling header, role-based navigation,
     * and the appropriate dashboard content panel for the current user.
     * Throws if no active session exists, since the dashboard requires
     * authentication.
     * 
     * @param app the main E_Report application frame
     */
    public DashboardView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // Create the background panel and configure its layout with gaps.
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Instantiate shared chrome components.
        header = new HeaderPanel(app);
        nav = new NavPanel();

        // Attach header and nav before adding role-specific content.
        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);

        // Read the session locally; do not store it as a field to avoid stale data.
        UserSession us = app.getUserSession();
        if (us == null) {
            // Dashboard requires an authenticated user; fail fast if missing.
            throw new IllegalStateException("No active session when loading Dashboard");
        }

        // Route to the correct dashboard implementation based on role.
        String role = us.getRole();
        if (role.equalsIgnoreCase("captain")) {
            cdp = new CaptainDashboardPanel(app);
            nav.setCaptainMenus(route -> app.navigate(route));
            bgPanel.add(cdp, BorderLayout.CENTER);
        } else if (role.equalsIgnoreCase("secretary")) {
            sdp = new SecretaryDashboardPanel(app);
            nav.setSecretaryMenus(route -> app.navigate(route));
            bgPanel.add(sdp, BorderLayout.CENTER);
        } else if (role.equalsIgnoreCase("resident")) {
            rdp = new ResidentDashboardPanel(app);
            nav.setResidentMenus(route -> app.navigate(route));
            bgPanel.add(rdp, BorderLayout.CENTER);
        }

        add(bgPanel, BorderLayout.CENTER);
    }

    /** @return the header panel instance */
    public HeaderPanel getHeader() {
        return header;
    }

    /** @return the navigation panel instance */
    public NavPanel getNavPanel() {
        return nav;
    }

    /**
     * Returns the Secretary dashboard panel.
     * Note: returns null if the current user is not a Secretary.
     * 
     * @return the SecretaryDashboardPanel instance, or null
     */
    public SecretaryDashboardPanel getContent() {
        return sdp;
    }
}