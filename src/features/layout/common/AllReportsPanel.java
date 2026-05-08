package features.layout.common;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

import app.E_Report;
import features.components.FilterBarPanel;
import features.core.RecentReportsPanel;
import models.ComplaintDetail;
import models.UserSession;
import services.fetcher.AllReportsFetcher;

public class AllReportsPanel extends JPanel {

    private static final String[] REPORT_TABLE_COLUMNS = {
            "Report ID", "Type", "Purok", "Date Submitted",
            "Last Update", "Status", "Action"
    };
    private static final int ACTION_COLUMN_INDEX = 6;
    private static final String ACTION_BUTTON_TEXT = "View";
    private static final Color ACTION_BUTTON_COLOR = new Color(120, 100, 200);
    private static final int SECTION_GAP = 20;
    private static final int PANEL_PADDING = 20;

    private final E_Report app;
    private final UserSession us;
    private FilterBarPanel filterBarPanel;
    private RecentReportsPanel reportsPanel;
    private List<ComplaintDetail> allComplaints;
    private List<Object[]> filteredDataList;
    private AllReportsFetcher fetcher;

    public AllReportsPanel(E_Report app) {
        this.app = app;
        this.us = app.getUserSession();
        this.allComplaints = new ArrayList<>();
        this.filteredDataList = new ArrayList<>();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
                PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));

        initializeUI();

        this.fetcher = new AllReportsFetcher(app);
        this.fetcher.addDataChangeListener(this::onDataChanged);

        // Initial load – no date filter, all categories/puroks/statuses
        this.fetcher.applyFilters(null, null, "All Category", "All Purok", "All Statuses");
    }

    private void onDataChanged() {
        filteredDataList.clear();
        filteredDataList.addAll(fetcher.getFilteredReports());

        Map<Long, Color> highlights = computeRowHighlights();
        reportsPanel.setRowHighlights(highlights);

        refreshTable();
    }

    private Map<Long, Color> computeRowHighlights() {
        Map<Long, Color> highlights = new HashMap<>();
        Set<Integer> followUpIds = fetchFollowUpComplaintIds();
        int rowsPerPage = reportsPanel.getRowsPerPage();

        for (int i = 0; i < filteredDataList.size(); i++) {
            Object[] row = filteredDataList.get(i);
            if (row == null || row.length == 0 || row[0] == null)
                continue;

            int complaintId;
            try {
                complaintId = Integer.parseInt(row[0].toString());
            } catch (NumberFormatException e) {
                continue;
            }

            String status = row.length > 5 && row[5] != null ? row[5].toString() : "";

            int page = i / rowsPerPage;
            int rowIdx = i % rowsPerPage;
            long pos = ((long) page << 32) | (rowIdx & 0xffffffffL);

            // FIXED: Status colors now take priority over follow-ups
            if ("Resolved".equalsIgnoreCase(status)) {
                highlights.put(pos, new Color(232, 245, 233)); // light green
            } else if ("Rejected".equalsIgnoreCase(status)) {
                highlights.put(pos, new Color(255, 235, 238)); // light red
            } else if (followUpIds.contains(complaintId)) {
                highlights.put(pos, new Color(255, 248, 225)); // light orange
            }
        }
        return highlights;
    }

    private void initializeUI() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // Filter bar row
        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        filterBarPanel = new FilterBarPanel(new FilterBarPanel.FilterListener() {
            @Override
            public void onApply(Date fromDate, Date toDate, String category,
                    String purok, String status) {
                fetcher.applyFilters(fromDate, toDate, category, purok, status);
            }

            @Override
            public void onReset() {
                fetcher.applyFilters(null, null, null, null, null);
            }
        });
        filterRow.add(filterBarPanel, BorderLayout.CENTER);

        wrapper.add(filterRow);
        wrapper.add(Box.createRigidArea(new Dimension(0, SECTION_GAP)));

        // Reports table row
        JPanel contentRow = new JPanel(new GridBagLayout());
        contentRow.setOpaque(false);
        contentRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        reportsPanel = new RecentReportsPanel("All Reports", REPORT_TABLE_COLUMNS, 6);
        reportsPanel.setButtonColumn(ACTION_COLUMN_INDEX, ACTION_BUTTON_TEXT, ACTION_BUTTON_COLOR);
        reportsPanel.setOnViewClicked(row -> handleReportAction(row));

        gbc.gridx = 0;
        contentRow.add(reportsPanel, gbc);

        wrapper.add(contentRow);
        wrapper.add(Box.createVerticalGlue());

        add(wrapper, BorderLayout.CENTER);
    }

    // ============================================================
    // TABLE REFRESH & EVENTS
    // ============================================================

    private void refreshTable() {
        reportsPanel.clearReports();
        for (Object[] row : filteredDataList) {
            reportsPanel.addReport(row);
        }
        reportsPanel.revalidate();
        reportsPanel.repaint();
    }

    private void handleReportAction(int row) {
        if (row < 0 || row >= filteredDataList.size())
            return;

        Object idObj = filteredDataList.get(row)[0];
        int reportId = (idObj instanceof Integer) ? (Integer) idObj : Integer.parseInt(idObj.toString());
        ComplaintDetail cd = fetchComplaintFromDb(reportId);

        if (cd != null) {
            app.setCurrentComplaint(cd);
            app.setReturnRoute("reports");
            app.navigate("complaintdetail", "reports");
        }
    }

    private ComplaintDetail fetchComplaintFromDb(int id) {
        var all = new services.controller.ComplaintServiceController().getAllComplaints();
        if (all == null)
            return null;
        for (ComplaintDetail cd : all) {
            if (cd.getComplaintId() == id)
                return cd;
        }
        return null;
    }

    /**
     * Fetches distinct complaint IDs that currently have a Pending follow-up
     * request.
     */
    private Set<Integer> fetchFollowUpComplaintIds() {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT DISTINCT CD_ID FROM Follow_Up_Request WHERE status = 'Pending'";
        try (Connection con = config.database.DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("CD_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    public void clearReports() {
        allComplaints.clear();
        filteredDataList.clear();
        reportsPanel.clearReports();
    }

    public List<Object[]> getFilteredDataList() {
        return new ArrayList<>(filteredDataList);
    }

    public RecentReportsPanel getReportsPanel() {
        return reportsPanel;
    }

    public FilterBarPanel getFilterBarPanel() {
        return filterBarPanel;
    }
}