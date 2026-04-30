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
 */
public class MyProfileView extends JPanel {
    private final E_Report app;
    private final HeaderPanel header;
    private final NavPanel nav;
    private final ProfilePanel profilePanel;

    public MyProfileView(E_Report app) {
        this.app = app;

        header = new HeaderPanel(app);
        nav = new NavPanel();

        profilePanel = new ProfilePanel(app, () -> app.navigate("securitypassword"));

        initializeLayout();
        setupNavigation();
    }

    private void initializeLayout() {
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(profilePanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

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

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNavPanel() {
        return nav;
    }

    public ProfilePanel getProfilePanel() {
        return profilePanel;
    }

    public void refreshProfile() {
        profilePanel.loadFromApp();
        header.refresh();
    }
}