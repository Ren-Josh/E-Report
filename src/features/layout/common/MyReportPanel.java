package features.layout.common;

import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.*;

import app.E_Report;
import features.components.FilterBarPanel;
import features.core.RecentReportsPanel;
import models.ComplaintDetail;
import models.UserSession;
import services.controller.ComplaintServiceController;

public class MyReportPanel extends JPanel {

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

    public MyReportPanel(E_Report app) {
        this.app = app;
        this.us = app.getUserSession();
        this.allComplaints = new ArrayList<>();
        this.filteredDataList = new ArrayList<>();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
                PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));

        initializeUI();

        // Defer DB load until UI is fully constructed
        SwingUtilities.invokeLater(this::loadAllReports);
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
            public void onApply(Date fromDate, Date toDate, String category, String purok, String status) {
                applyFilters(fromDate, toDate, category, purok, status);
            }

            @Override
            public void onReset() {
                clearFilters();
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

        reportsPanel = new RecentReportsPanel("My Reports", REPORT_TABLE_COLUMNS, 6);
        reportsPanel.setButtonColumn(ACTION_COLUMN_INDEX, ACTION_BUTTON_TEXT, ACTION_BUTTON_COLOR);
        reportsPanel.setOnViewClicked(row -> handleReportAction(row));

        gbc.gridx = 0;
        contentRow.add(reportsPanel, gbc);

        wrapper.add(contentRow);
        wrapper.add(Box.createVerticalGlue());

        add(wrapper, BorderLayout.CENTER);
    }

    // ============================================================
    // DATA LOADING
    // ============================================================

    private void loadAllReports() {

        ComplaintServiceController csc = new ComplaintServiceController();
        List<ComplaintDetail> complaints = csc.getAllComplaintByUser(us);

        allComplaints.clear();

        if (complaints != null && !complaints.isEmpty()) {
            complaints.sort((a, b) -> {
                Timestamp ta = a.getLastUpdateTimestamp() != null ? a.getLastUpdateTimestamp() : a.getDateTime();
                Timestamp tb = b.getLastUpdateTimestamp() != null ? b.getLastUpdateTimestamp() : b.getDateTime();
                if (ta == null && tb == null)
                    return 0;
                if (ta == null)
                    return 1;
                if (tb == null)
                    return -1;
                return tb.compareTo(ta);
            });

            allComplaints.addAll(complaints);
        } else {
            System.out.println("[MyReportPanel] No complaints returned (null or empty)");
        }

        clearFilters();
    }

    public void refreshDataFromDatabase() {
        loadAllReports();
    }

    // ============================================================
    // FILTERING LOGIC
    // ============================================================

    private void applyFilters(Date fromDate, Date toDate, String category, String purok, String status) {
        filteredDataList.clear();

        for (ComplaintDetail cd : allComplaints) {
            boolean matches = true;

            // --- Date From filter ---
            if (fromDate != null) {
                Date rowDate = stripTime(cd.getDateTime());
                Date fromOnly = stripTime(fromDate);
                if (rowDate == null || rowDate.before(fromOnly)) {
                    matches = false;
                }
            }

            // --- Date To filter (inclusive) ---
            if (toDate != null && matches) {
                Date rowDate = stripTime(cd.getDateTime());
                Date toOnly = stripTime(toDate);
                if (rowDate == null || rowDate.after(toOnly)) {
                    matches = false;
                }
            }

            // --- Category filter ---
            if (!"All Categories".equals(category) && matches) {
                String rowType = cd.getType();
                if (rowType == null || !rowType.equalsIgnoreCase(category)) {
                    matches = false;
                }
            }

            // --- Purok filter ---
            if (!"All Puroks".equals(purok) && matches) {
                String rowPurok = cd.getPurok();
                if (rowPurok == null || !rowPurok.equalsIgnoreCase(purok)) {
                    matches = false;
                }
            }

            // --- Status filter ---
            if (!"All Statuses".equals(status) && matches) {
                String rowStatus = cd.getCurrentStatus();
                if (rowStatus == null || !rowStatus.equalsIgnoreCase(status)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredDataList.add(complaintToRow(cd));
            }
        }

        refreshTable();
    }

    private void clearFilters() {
        filteredDataList.clear();
        for (ComplaintDetail cd : allComplaints) {
            filteredDataList.add(complaintToRow(cd));
        }
        refreshTable();
    }

    // ============================================================
    // DATE HELPERS
    // ============================================================

    private Date stripTime(Date date) {
        if (date == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // ============================================================
    // CONVERSION
    // ============================================================

    private Object[] complaintToRow(ComplaintDetail cd) {
        Object[] row = new Object[ACTION_COLUMN_INDEX + 1];

        row[0] = String.valueOf(cd.getComplaintId());
        row[1] = cd.getType() != null ? cd.getType() : "";
        row[2] = cd.getPurok() != null ? cd.getPurok() : "";
        row[3] = formatTimestamp(cd.getDateTime());
        row[4] = formatTimestamp(cd.getLastUpdateTimestamp() != null
                ? cd.getLastUpdateTimestamp()
                : cd.getDateTime());
        row[5] = cd.getCurrentStatus() != null ? cd.getCurrentStatus() : "";
        row[6] = ACTION_BUTTON_TEXT;

        return row;
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(ts.getTime()));
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

        int reportId = Integer.parseInt((String) filteredDataList.get(row)[0]);
        ComplaintDetail cd = findComplaintById(reportId);

        if (cd != null) {
            app.setCurrentComplaint(cd);
            app.setReturnRoute("myreport");
            app.navigate("complaintdetail");
        }
    }

    private ComplaintDetail findComplaintById(int id) {
        for (ComplaintDetail cd : allComplaints) {
            if (cd.getComplaintId() == id)
                return cd;
        }
        return null;
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    public void addReport(Object[] reportData) {
        refreshDataFromDatabase();
    }

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