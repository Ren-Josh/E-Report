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

public class MyReportsView extends JPanel {
    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private MyReportPanel myReportsPanel;

    public MyReportsView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();
        myReportsPanel = new MyReportPanel(app);

        UserSession us = app.getUserSession();
        if (us != null) { // ← null guard added
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
        bgPanel.add(myReportsPanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNavPanel() {
        return nav;
    }

    public MyReportPanel getMyReportsPanel() {
        return myReportsPanel;
    }
}