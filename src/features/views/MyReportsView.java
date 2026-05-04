package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.layout.common.MyReportPanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Composite view panel for the "My Reports" screen.
 * Displays the currently logged-in user's own complaint reports with a header,
 * role-based navigation sidebar, and the reports content area.
 */
public class MyReportsView extends JPanel {
    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /** Top header bar displaying user info and system branding. */
    private HeaderPanel header;
    /** Left-side navigation panel with role-specific menu items. */
    private NavPanel nav;
    /** Central panel containing the user's personal reports table. */
    private MyReportPanel myReportsPanel;

    /**
     * Constructs the My Reports view, assembling header, role-based navigation,
     * and the personal reports content panel.
     * 
     * @param app the main E_Report application frame
     */
    public MyReportsView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // Create the background panel and configure its layout with gaps.
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Instantiate sub-components.
        header = new HeaderPanel(app);
        nav = new NavPanel();
        myReportsPanel = new MyReportPanel(app);

        // Configure navigation menus based on the authenticated user's role.
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
        bgPanel.add(myReportsPanel, BorderLayout.CENTER);

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

    /** @return the personal reports content panel instance */
    public MyReportPanel getMyReportsPanel() {
        return myReportsPanel;
    }
}