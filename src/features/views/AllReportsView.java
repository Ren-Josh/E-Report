package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.layout.common.AllReportsPanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Composite view panel for the "All Reports" screen.
 * Assembles a header, role-based navigation sidebar, and the main reports
 * content area on a background panel.
 */
public class AllReportsView extends JPanel {
    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /** Top header bar displaying user info and system branding. */
    private HeaderPanel header;
    /** Left-side navigation panel with role-specific menu items. */
    private NavPanel nav;
    /** Central content panel containing the reports table and filter controls. */
    private AllReportsPanel allReportsPanel;

    /**
     * Constructs the All Reports view, wiring all sub-components together.
     * Reads the current user's role from the session to configure the
     * correct navigation menu set.
     * 
     * @param app the main E_Report application frame
     */
    public AllReportsView(E_Report app) {
        this.app = app;
        app.setReturnRoute("reports");
        setLayout(new BorderLayout());

        // Create the background panel and configure its layout with gaps.
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Instantiate sub-components.
        header = new HeaderPanel(app);
        nav = new NavPanel();
        allReportsPanel = new AllReportsPanel(app);

        // Configure navigation menus based on the authenticated user's role.
        UserSession us = app.getUserSession();
        if (us.getRole().equalsIgnoreCase("captain")) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (us.getRole().equalsIgnoreCase("secretary")) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else {
            nav.setResidentMenus(route -> app.navigate(route));
        }

        // Assemble the layout: header on top, nav on the left, content in center.
        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(allReportsPanel, BorderLayout.CENTER);

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

    /** @return the main reports content panel instance */
    public AllReportsPanel getAllReportsPanel() {
        return allReportsPanel;
    }
}