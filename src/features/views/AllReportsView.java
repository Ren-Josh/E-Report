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

public class AllReportsView extends JPanel {
    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private AllReportsPanel allReportsPanel;

    public AllReportsView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();
        allReportsPanel = new AllReportsPanel(app);

        UserSession us = app.getUserSession();
        if (us.getRole().equalsIgnoreCase("captain")) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (us.getRole().equalsIgnoreCase("secretary")) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else {
            nav.setResidentMenus(route -> app.navigate(route));
        }

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(allReportsPanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNavPanel() {
        return nav;
    }

    public AllReportsPanel getAllReportsPanel() {
        return allReportsPanel;
    }
}