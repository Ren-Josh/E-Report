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

public class DashboardView extends JPanel {
    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private CaptainDashboardPanel cdp;
    private SecretaryDashboardPanel sdp;
    private ResidentDashboardPanel rdp;

    public DashboardView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);

        UserSession us = app.getUserSession(); // local var only — not stored
        if (us == null) {
            throw new IllegalStateException("No active session when loading Dashboard");
        }

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

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNavPanel() {
        return nav;
    }

    public SecretaryDashboardPanel getContent() {
        return sdp;
    }
}