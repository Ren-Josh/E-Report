package features.core.dashboardpanel.resident;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import app.E_Report;
import features.core.RecentReportsPanel;
import features.core.dashboardpanel.DashboardInfoCardsPanel;
import models.ComplaintDetail;
import services.fetcher.ResidentDashboardFetcher;

public class ResidentDashboardPanel extends JPanel {

    private final E_Report app;
    private DashboardInfoCardsPanel infoCardsPanel;
    private RecentReportsPanel reportsPanel;
    private ResidentDashboardFetcher fetcher;

    // Stores the complaint data so we can map row clicks to actual ComplaintDetail
    // objects
    private List<ComplaintDetail> recentComplaints;

    private static final String[] REPORT_TABLE_COLUMNS = {
            "Report ID", "Type", "Purok", "Date Submitted", "Last Update", "Status", "Action"
    };
    private static final int ACTION_COLUMN_INDEX = 6;
    private static final String ACTION_BUTTON_TEXT = "View";
    private static final Color ACTION_BUTTON_COLOR = new Color(120, 100, 200);

    public ResidentDashboardPanel(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());
        setOpaque(false);

        initializeUI();

        fetcher = new ResidentDashboardFetcher(app);
        fetcher.addDataChangeListener(this::onDataChanged);
    }

    private void initializeUI() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] iconPaths = {
                "src/assets/icons/reports_icon.png",
                "src/assets/icons/report_pending_icon.png",
                "src/assets/icons/report_in_progress_icon.png",
                "src/assets/icons/resolved_icon.png"
        };
        infoCardsPanel = new DashboardInfoCardsPanel(12, 12, 12, 12, iconPaths);
        infoCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCardsPanel.updateValues(0, 0, 0, 0);
        contentPanel.add(infoCardsPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        reportsPanel = new RecentReportsPanel("My Reports", REPORT_TABLE_COLUMNS, 6);
        reportsPanel.setButtonColumn(ACTION_COLUMN_INDEX, ACTION_BUTTON_TEXT, ACTION_BUTTON_COLOR);
        reportsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ========== VIEW CLICK HANDLER ==========
        reportsPanel.setOnViewClicked(row -> {
            if (recentComplaints == null || row < 0 || row >= recentComplaints.size()) {
                return;
            }
            ComplaintDetail cd = recentComplaints.get(row);
            if (cd != null) {
                app.setCurrentComplaint(cd);
                app.setReturnRoute("myreport");
                app.navigate("complaintdetail");
            }
        });
        // ========================================

        contentPanel.add(reportsPanel);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void onDataChanged() {
        // Update stat cards
        int[] stats = fetcher.getStatValues();
        if (stats != null && stats.length == 4) {
            infoCardsPanel.updateValues(stats[0], stats[1], stats[2], stats[3]);
            app.setResidentDashboardStats(stats[0], stats[1], stats[2], stats[3]);
        }

        // Update reports table
        List<Object[]> reports = fetcher.getReports();
        reportsPanel.clearReports();

        if (reports != null) {
            for (Object[] row : reports) {
                reportsPanel.addReport(row);
            }
            app.setResidentDashboardReports(reports);
        }

        // Sync the ComplaintDetail list for row-click mapping
        this.recentComplaints = fetcher.getComplaintDetails();

        reportsPanel.revalidate();
        reportsPanel.repaint();
    }
}