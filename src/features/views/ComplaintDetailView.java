package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.viewing.ComplaintContentPanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Full-page complaint detail view with persistent Header and NavPanel.
 * The content panel handles both View and Update modes internally.
 */
public class ComplaintDetailView extends JPanel {

    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private ComplaintContentPanel contentPanel;

    /**
     * Route to navigate back to when Back is clicked (e.g., "dashboard",
     * "myreport", "reports")
     */
    private String returnRoute = "dashboard";

    public ComplaintDetailView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header (always visible)
        header = new HeaderPanel(app);
        bgPanel.add(header, BorderLayout.NORTH);

        // Navigation sidebar (always visible)
        nav = new NavPanel();
        setupNavigation();
        bgPanel.add(nav, BorderLayout.WEST);

        // Main content - wrap in a scroll pane to handle overflow
        contentPanel = new ComplaintContentPanel(app);
        JScrollPane contentScroll = new JScrollPane(contentPanel);
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        contentScroll.setBorder(null);
        contentScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        bgPanel.add(contentScroll, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    /**
     * Set the route to return to when the user clicks Back.
     * Call this BEFORE navigating to this view.
     */
    public void setReturnRoute(String route) {
        this.returnRoute = (route != null && !route.isBlank()) ? route : "dashboard";
        if (contentPanel != null) {
            contentPanel.setReturnRoute(this.returnRoute);
        }
    }

    public String getReturnRoute() {
        return returnRoute;
    }

    private void setupNavigation() {
        UserSession us = app.getUserSession();
        if (us == null)
            return;

        String role = us.getRole().toLowerCase();
        if (role.contains("captain")) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (role.contains("secretary")) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else {
            nav.setResidentMenus(route -> app.navigate(route));
        }
    }

    public ComplaintContentPanel getContentPanel() {
        return contentPanel;
    }

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNav() {
        return nav;
    }
}