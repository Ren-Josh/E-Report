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

    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private ComplaintStatusUpdatePanel statusPanel;

    public ComplaintStatusUpdateView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();
        statusPanel = new ComplaintStatusUpdatePanel(app);

        // Setup navigation based on role
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

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(statusPanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    /**
     * Loads a complaint into the status update panel.
     * Call this before navigating to this view.
     */
    public void setComplaint(ComplaintDetail complaint) {
        statusPanel.loadComplaint(complaint);
    }

    public ComplaintStatusUpdatePanel getStatusPanel() {
        return statusPanel;
    }

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNavPanel() {
        return nav;
    }
}