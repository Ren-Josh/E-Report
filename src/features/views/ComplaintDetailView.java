package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.layout.common.viewreport.ComplaintContentPanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Composite view panel for the "Complaint Detail" screen.
 * Displays the full details of a single complaint with a header,
 * role-based navigation sidebar, and a scrollable content area.
 * Supports returning to a configurable route after viewing.
 */
public class ComplaintDetailView extends JPanel {

    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /** Top header bar displaying user info and system branding. */
    private HeaderPanel header;
    /** Left-side navigation panel with role-specific menu items. */
    private NavPanel nav;
    /** Central panel that renders the actual complaint details. */
    private ComplaintContentPanel contentPanel;

    /**
     * Constructs the Complaint Detail view, assembling header, navigation,
     * and scrollable content. Reads the return route from application state
     * so the back button can navigate to the correct previous screen.
     * 
     * @param app the main E_Report application frame
     */
    public ComplaintDetailView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // Create the background panel and configure its layout with gaps.
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Build and attach the header bar at the top.
        header = new HeaderPanel(app);
        bgPanel.add(header, BorderLayout.NORTH);

        // Build and attach the navigation sidebar on the left.
        nav = new NavPanel();
        setupNavigation();
        bgPanel.add(nav, BorderLayout.WEST);

        // Build the content panel and set its return route from app state.
        contentPanel = new ComplaintContentPanel(app);
        // Read return route from app state to know where to go when user clicks back.
        String route = app.getReturnRoute();
        contentPanel.setReturnRoute(route != null ? route : "dashboard");

        // Wrap the content in a scroll pane so long complaints remain readable.
        JScrollPane contentScroll = new JScrollPane(contentPanel);
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        contentScroll.setBorder(null);
        // Disable horizontal scrolling; let the content panel handle its own width.
        contentScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // Only show vertical scrollbar when content exceeds viewport height.
        contentScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Increase scroll speed for smoother user experience.
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        bgPanel.add(contentScroll, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    /**
     * Configures the navigation menu based on the current user's role.
     * Guards against null session to prevent NullPointerException.
     */
    private void setupNavigation() {
        UserSession us = app.getUserSession();
        if (us == null)
            return;

        // Use lowercase contains for flexible role matching.
        String role = us.getRole().toLowerCase();
        if (role.contains("captain")) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (role.contains("secretary")) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else {
            nav.setResidentMenus(route -> app.navigate(route));
        }
    }

    /** @return the complaint content panel instance */
    public ComplaintContentPanel getContentPanel() {
        return contentPanel;
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