package features.views;

import app.E_Report;
import config.UIConfig;
import features.core.BackgroundPanel;
import features.layout.CaptainDashboardPanel;
import features.layout.SecretaryDashboardPanel;
import features.layout.HeaderPanel;
import features.layout.NavPanel;
import models.UserInfo;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

public class DashboardView extends JPanel {
    private E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private SecretaryDashboardPanel content;
    private CaptainDashboardPanel mdb;
    private UserSession us;
    private UserInfo ui;


    public DashboardView(E_Report app) {
        this.app = app;
        this.us = app.getUserSession();
        this.ui = app.getUserInfo();
        setLayout(new BorderLayout());
        
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();
        content = new SecretaryDashboardPanel(app);
        mdb = new CaptainDashboardPanel();

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        
        if(us.getRole().equalsIgnoreCase("resident")){
            bgPanel.add(mdb, BorderLayout.CENTER);
        }else if(us.getRole().equalsIgnoreCase("secretary")){// || us.getRole().equalsIgnoreCase("secretary")){
            bgPanel.add(content, BorderLayout.CENTER);
        }

        add(bgPanel, BorderLayout.CENTER);
    }

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNav() {
        return nav;
    }

    public SecretaryDashboardPanel getContent() {
        return content;
    }
}