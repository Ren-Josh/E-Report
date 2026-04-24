package features.layout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import app.E_Report;
import features.core.RecentReportsPanel;
import features.core.dashboardpanel.DashboardInfoCardsPanel;
import models.UserSession;
import services.fetcher.ResidentDashboardFetcher;

public class ResidentDashboardPanel extends JPanel {

    private final E_Report app;
    private final UserSession us;
    private DashboardInfoCardsPanel infoCardsPanel;
    private RecentReportsPanel reportsPanel;
    private ResidentDashboardFetcher fetcher;

    private static final String[] REPORT_TABLE_COLUMNS = {
            "Report ID", "Type", "Purok", "Date Submitted", "Last Update", "Status", "Action"
    };
    private static final int ACTION_COLUMN_INDEX = 6;
    private static final String ACTION_BUTTON_TEXT = "View";
    private static final Color ACTION_BUTTON_COLOR = new Color(120, 100, 200);

    public ResidentDashboardPanel(E_Report app) {
        this.app = app;
        this.us = app.getUserSession();
        setLayout(new BorderLayout());
        setOpaque(false);

        // Build the static UI first (info cards with default zeros, empty reports
        // table)
        initializeUI();

        // Create fetcher and attach listener – it will update the UI asynchronously
        fetcher = new ResidentDashboardFetcher(app);
        fetcher.addDataChangeListener(this::onDataChanged);
        // The fetcher automatically starts loading in its constructor
    }

    private void initializeUI() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Info cards – initially empty, will be filled by onDataChanged()
        String[] iconPaths = {
                "src/assets/icons/reports_icon.png",
                "src/assets/icons/report_pending_icon.png",
                "src/assets/icons/report_in_progress_icon.png",
                "src/assets/icons/resolved_icon.png"
        };
        infoCardsPanel = new DashboardInfoCardsPanel(12, 12, 12, 12, iconPaths);
        infoCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Show zeros while data is loading
        infoCardsPanel.updateValues(0, 0, 0, 0);
        contentPanel.add(infoCardsPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Recent reports table – initially empty
        reportsPanel = new RecentReportsPanel("My Reports", REPORT_TABLE_COLUMNS, 6);
        reportsPanel.setButtonColumn(ACTION_COLUMN_INDEX, ACTION_BUTTON_TEXT, ACTION_BUTTON_COLOR);
        reportsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(reportsPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void onDataChanged() {
        // Called on EDT when fetcher finishes loading
        int[] stats = fetcher.getStatValues();
        if (stats != null && stats.length == 4) {
            infoCardsPanel.updateValues(stats[0], stats[1], stats[2], stats[3]);
        }

        List<Object[]> reports = fetcher.getReports();
        reportsPanel.clearReports();
        if (reports != null) {
            for (Object[] row : reports) {
                reportsPanel.addReport(row);
            }
        }
        reportsPanel.revalidate();
        reportsPanel.repaint();
    }
}