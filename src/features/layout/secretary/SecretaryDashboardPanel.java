package features.layout.secretary;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import app.E_Report;
import config.UIConfig;
import features.core.RecentReportsPanel;
import features.core.dashboardpanel.DashboardInfoCardsPanel;
import features.core.dashboardpanel.captain.ActivityItem;
import features.core.dashboardpanel.captain.RecentActivitiesPanel;
import features.core.dashboardpanel.secretary.TaskNotesPanel;
import models.UserSession;
import services.controller.RecentActivityController;
import services.fetcher.SecretaryDashboardFetcher;

public class SecretaryDashboardPanel extends JPanel {
    protected E_Report app;
    private DashboardInfoCardsPanel statsCards;
    private RecentReportsPanel reportsPanel;
    private RecentActivitiesPanel activitiesPanel;
    private TaskNotesPanel taskNotesPanel;
    private SecretaryDashboardFetcher fetcher;

    private String[] statIconPaths;
    private int[] statValues;
    private List<Object[]> reportDataList;
    private List<ActivityItem> activityList;

    private static final String[] REPORT_TABLE_COLUMNS = {
            "Report ID", "Category", "Purok", "Date Submitted",
            "Last Update", "Status", "Action"
    };
    private static final int ACTION_COLUMN_INDEX = 6;
    private static final String ACTION_BUTTON_TEXT = "View";
    private static final Color ACTION_BUTTON_COLOR = new Color(120, 100, 200);
    private static final int SECTION_GAP = 20;
    private static final int DASHBOARD_PADDING = 20;

    public SecretaryDashboardPanel(E_Report app) {
        this.app = app;
        this.statIconPaths = UIConfig.STAT_ICON_PATHS;
        this.statValues = new int[4];
        this.reportDataList = new ArrayList<>();
        this.activityList = new ArrayList<>();

        initializeUI();

        this.fetcher = new SecretaryDashboardFetcher(app);
        this.fetcher.addDataChangeListener(this::onDataChanged);
    }

    private void onDataChanged() {
        int[] stats = fetcher.getStatValues();
        statsCards.updateValues(stats[0], stats[1], stats[2], stats[3]);

        reportDataList.clear();
        reportDataList.addAll(fetcher.getReports());
        refreshReportsTable();

        RecentActivityController rac = new RecentActivityController();
        activityList.clear();
        activityList.addAll(rac.getRecentActivities(app.getUserSession(), 7));
        refreshActivities();

        // Task notes are self-persisting; no fetcher sync needed
    }

    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
                DASHBOARD_PADDING, DASHBOARD_PADDING, DASHBOARD_PADDING, DASHBOARD_PADDING));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        JPanel statsRow = new JPanel(new BorderLayout());
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsCards = new DashboardInfoCardsPanel(
                statValues[0], statValues[1], statValues[2], statValues[3], statIconPaths);
        statsRow.add(statsCards, BorderLayout.CENTER);
        wrapper.add(statsRow);
        wrapper.add(Box.createRigidArea(new Dimension(0, SECTION_GAP)));

        JPanel contentRow = new JPanel(new GridBagLayout());
        contentRow.setOpaque(false);
        contentRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        reportsPanel = new RecentReportsPanel("Recent Reports", REPORT_TABLE_COLUMNS);
        reportsPanel.setButtonColumn(ACTION_COLUMN_INDEX, ACTION_BUTTON_TEXT, ACTION_BUTTON_COLOR);
        reportsPanel.setOnViewClicked(row -> handleReportAction(row));

        gbc.gridx = 0;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 0, SECTION_GAP);
        contentRow.add(reportsPanel, gbc);

        JPanel rightColumn = new JPanel(new GridBagLayout());
        rightColumn.setOpaque(false);

        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.gridx = 0;
        rgbc.fill = GridBagConstraints.BOTH;
        rgbc.weightx = 1.0;

        RecentActivityController rac = new RecentActivityController();
        UserSession us = app.getUserSession();
        activityList = rac.getRecentActivities(us, 7);
        activitiesPanel = new RecentActivitiesPanel("Recent Activities", activityList);

        rgbc.gridy = 0;
        rgbc.weighty = 0.55;
        rgbc.insets = new Insets(0, 0, SECTION_GAP, 0);
        rightColumn.add(activitiesPanel, rgbc);

        taskNotesPanel = new TaskNotesPanel("Daily Tasks");
        rgbc.gridy = 1;
        rgbc.weighty = 0.45;
        rgbc.insets = new Insets(0, 0, 0, 0);
        rightColumn.add(taskNotesPanel, rgbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentRow.add(rightColumn, gbc);

        wrapper.add(contentRow);
        wrapper.add(Box.createVerticalGlue());
        add(wrapper, BorderLayout.CENTER);
    }

    private void handleReportAction(int row) {
        if (row >= 0 && row < reportDataList.size()) {
            String reportId = (String) reportDataList.get(row)[0];
            JOptionPane.showMessageDialog(this, "Viewing Report: " + reportId);
        }
    }

    public void updateStatValue(int cardIndex, int value) {
        statValues[cardIndex] = value;
        statsCards.updateCardValue(cardIndex, value);
    }

    public void updateAllStatValues(int total, int pending, int inProgress, int resolved) {
        statValues[0] = total;
        statValues[1] = pending;
        statValues[2] = inProgress;
        statValues[3] = resolved;
        statsCards.updateValues(total, pending, inProgress, resolved);
    }

    public void addReport(Object[] reportData) {
        if (reportData == null)
            return;
        Object[] finalData;
        if (reportData.length == ACTION_COLUMN_INDEX) {
            finalData = new Object[ACTION_COLUMN_INDEX + 1];
            System.arraycopy(reportData, 0, finalData, 0, reportData.length);
            finalData[ACTION_COLUMN_INDEX] = ACTION_BUTTON_TEXT;
        } else if (reportData.length == ACTION_COLUMN_INDEX + 1) {
            finalData = reportData.clone();
            finalData[ACTION_COLUMN_INDEX] = ACTION_BUTTON_TEXT;
        } else {
            throw new IllegalArgumentException("Invalid report data length. Expected 6 or 7 columns.");
        }
        reportDataList.add(finalData);
        reportsPanel.addReport(finalData);
    }

    public void removeReport(int index) {
        if (index >= 0 && index < reportDataList.size()) {
            reportDataList.remove(index);
            refreshReportsTable();
        }
    }

    public void setReports(List<Object[]> reports) {
        reportDataList.clear();
        reportDataList.addAll(reports);
        refreshReportsTable();
    }

    public void refreshReportsTable() {
        reportsPanel.clearReports();
        for (Object[] report : reportDataList) {
            reportsPanel.addReport(report);
        }
    }

    public List<Object[]> getReportDataList() {
        return new ArrayList<>(reportDataList);
    }

    public void addActivity(String activity) {
        activityList.add(new ActivityItem("", activity, "", ""));
        refreshActivities();
    }

    public void removeActivity(int index) {
        if (index >= 0 && index < activityList.size()) {
            activityList.remove(index);
            refreshActivities();
        }
    }

    public void setActivities(List<String> activities) {
        activityList.clear();
        for (String desc : activities) {
            activityList.add(new ActivityItem("", desc, "", ""));
        }
        refreshActivities();
    }

    public void refreshActivities() {
        activitiesPanel.updateActivities(activityList);
    }

    public List<String> getActivityList() {
        List<String> descriptions = new ArrayList<>();
        for (ActivityItem item : activityList) {
            descriptions.add(item.getDescription());
        }
        return descriptions;
    }

    // ── Task notes delegation ───────────────────────────────────

    public String getTaskNotesText() {
        return taskNotesPanel.getTasksText();
    }

    public void setTaskNotesText(String text) {
        taskNotesPanel.setTasksText(text);
    }

    public void clearTaskNotes() {
        taskNotesPanel.clearTasks();
    }

    // ── Global clear ────────────────────────────────────────────

    public void clearAllData() {
        reportDataList.clear();
        activityList.clear();
        refreshReportsTable();
        refreshActivities();
        clearTaskNotes();
    }

    public boolean isEmpty() {
        return reportDataList.isEmpty() && activityList.isEmpty()
                && getTaskNotesText().trim().isEmpty();
    }
}