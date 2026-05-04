package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.layout.common.ComplaintStatusUpdatePanel;
import models.ComplaintDetail;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Full-screen view for updating complaint status.
 * Wraps ComplaintStatusUpdatePanel with header, navigation, and background.
 */
public class ComplaintStatusUpdateView extends JPanel {

    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /** Top header bar displaying user info and system branding. */
    private HeaderPanel header;
    /** Left-side navigation panel with role-specific menu items. */
    private NavPanel nav;
    /** Central panel containing the status update form and controls. */
    private ComplaintStatusUpdatePanel statusPanel;

    /**
     * Constructs the Complaint Status Update view, assembling header,
     * role-based navigation, and the status update content panel.
     * 
     * @param app the main E_Report application frame
     */
    public ComplaintStatusUpdateView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // Create the background panel and configure its layout with gaps.
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Instantiate sub-components.
        header = new HeaderPanel(app);
        nav = new NavPanel();
        statusPanel = new ComplaintStatusUpdatePanel(app);

        // Setup navigation based on role.
        UserSession us = app.getUserSession();
        if (us != null) {
            String role = us.getRole();
            if (role.equalsIgnoreCase("captain")) {
                nav.setCaptainMenus(route -> app.navigate(route));
            } else if (role.equalsIgnoreCase("secretary")) {
                nav.setSecretaryMenus(route -> app.navigate(route));
            } else {
                nav.setResidentMenus(route -> app.navigate(route));
            }
        }

        // Assemble the layout: header on top, nav on the left, content in center.
        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(statusPanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    /**
     * Loads a complaint into the status update panel.
     * Call this before navigating to this view so the panel has data to display.
     * 
     * @param complaint the ComplaintDetail to load into the form
     */
    public void setComplaint(ComplaintDetail complaint) {
        statusPanel.loadComplaint(complaint);
    }

    /** @return the status update content panel instance */
    public ComplaintStatusUpdatePanel getStatusPanel() {
        return statusPanel;
    }

    /** @return the header panel instance */
    public HeaderPanel getHeader() {
        return header;
    }

    /** @return the navigation panel instance */
    public NavPanel getNavPanel() {
        return nav;
    }
}