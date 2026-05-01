package services.fetcher;

import app.E_Report;
import models.ComplaintDetail;
import services.controller.ComplaintServiceController;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AllReportsFetcher extends AbstractDashboardFetcher {
    private final ComplaintServiceController csc;

    private List<ComplaintDetail> allComplaints = new ArrayList<>();
    private List<Object[]> filteredReports = new ArrayList<>();

    // Current filter state
    private Date currentFrom, currentTo;
    private String currentCategory, currentPurok, currentStatus;

    public AllReportsFetcher(E_Report app) {
        super(app);
        this.csc = new ComplaintServiceController();
    }

    /** Called by AllReportsPanel when filters are applied or reset. */
    public void applyFilters(Date fromDate, Date toDate, String category,
            String purok, String status) {
        this.currentFrom = fromDate;
        this.currentTo = toDate;
        this.currentCategory = category;
        this.currentPurok = purok;
        this.currentStatus = status;
        refreshNow();
    }

    @Override
    protected void performFetch() {
        List<ComplaintDetail> complaints = csc.getAllComplaints();
        List<ComplaintDetail> raw = new ArrayList<>();

        if (complaints != null) {
            // Sort by creation date for consistent display
            complaints.sort((a, b) -> {
                Timestamp t1 = a.getDateTime();
                Timestamp t2 = b.getDateTime();
                if (t1 == null && t2 == null)
                    return 0;
                if (t1 == null)
                    return 1;
                if (t2 == null)
                    return -1;
                return t2.compareTo(t1);
            });
            raw.addAll(complaints);
        }

        this.allComplaints = raw;

        List<Object[]> rows = new ArrayList<>();
        for (ComplaintDetail cd : raw) {
            if (matchesFilters(cd)) {
                rows.add(complaintToRow(cd));
            }
        }
        this.filteredReports = rows;
    }

    private boolean matchesFilters(ComplaintDetail cd) {
        // ===== DATE RANGE FILTER — now uses LAST UPDATE timestamp =====
        // Use the last update timestamp if available, otherwise fall back to creation
        // date
        Timestamp filterTimestamp = cd.getLastUpdateTimestamp() != null
                ? cd.getLastUpdateTimestamp()
                : cd.getDateTime();

        if (currentFrom != null) {
            Date rd = stripTime(filterTimestamp);
            if (rd == null || rd.before(stripTime(currentFrom))) {
                return false;
            }
        }
        if (currentTo != null) {
            Date rd = stripTime(filterTimestamp);
            if (rd == null || rd.after(stripTime(currentTo))) {
                return false;
            }
        }

        // Category filter
        if (currentCategory != null && !"All Categories".equalsIgnoreCase(currentCategory)) {
            String t = cd.getType();
            if (t == null || !t.equalsIgnoreCase(currentCategory)) {
                return false;
            }
        }

        // Purok filter
        if (currentPurok != null && !"All Puroks".equalsIgnoreCase(currentPurok)) {
            String p = cd.getPurok();
            if (p == null || !p.equalsIgnoreCase(currentPurok)) {
                return false;
            }
        }

        // Status filter
        if (currentStatus != null && !"All Statuses".equalsIgnoreCase(currentStatus)) {
            String s = cd.getCurrentStatus();
            if (s == null || !s.equalsIgnoreCase(currentStatus)) {
                return false;
            }
        }

        return true;
    }

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

    private Object[] complaintToRow(ComplaintDetail cd) {
        Object[] row = new Object[7];
        row[0] = String.valueOf(cd.getComplaintId());
        row[1] = cd.getType() != null ? cd.getType() : "";
        row[2] = cd.getPurok() != null ? cd.getPurok() : "";
        row[3] = formatTs(cd.getDateTime()); // Date Submitted (creation)
        row[4] = formatTs(cd.getLastUpdateTimestamp() != null
                ? cd.getLastUpdateTimestamp()
                : cd.getDateTime()); // Last Update
        row[5] = cd.getCurrentStatus() != null ? cd.getCurrentStatus() : "";
        row[6] = "View";
        return row;
    }

    private String formatTs(Timestamp ts) {
        if (ts == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(ts.getTime()));
    }

    public List<Object[]> getFilteredReports() {
        return new ArrayList<>(filteredReports);
    }

    public List<ComplaintDetail> getAllComplaints() {
        return new ArrayList<>(allComplaints);
    }
}