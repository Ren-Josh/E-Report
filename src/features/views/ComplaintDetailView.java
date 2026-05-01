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

public class ComplaintDetailView extends JPanel {

    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private ComplaintContentPanel contentPanel;

    public ComplaintDetailView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        bgPanel.add(header, BorderLayout.NORTH);

        nav = new NavPanel();
        setupNavigation();
        bgPanel.add(nav, BorderLayout.WEST);

        contentPanel = new ComplaintContentPanel(app);
        // ── READ return route from app state ──
        String route = app.getReturnRoute();
        contentPanel.setReturnRoute(route != null ? route : "dashboard");

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

    public NavPanel getNavPanel() {
        return nav;
    }
}