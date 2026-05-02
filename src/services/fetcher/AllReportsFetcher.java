package services.fetcher;

import app.E_Report;
import models.ComplaintDetail;
import services.controller.ComplaintServiceController;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AllReportsFetcher extends AbstractDashboardFetcher {
    private final ComplaintServiceController csc;

    private List<ComplaintDetail> allComplaints = new ArrayList<>();
    private List<Object[]> filteredReports = new ArrayList<>();

    private Date currentFrom, currentTo;
    private String currentCategory, currentPurok, currentStatus;

    public AllReportsFetcher(E_Report app) {
        super(app);
        this.csc = new ComplaintServiceController();
    }

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
        this.allComplaints = sortByDateTimeDesc(complaints != null ? complaints : new ArrayList<>());

        List<Object[]> rows = new ArrayList<>();
        for (ComplaintDetail cd : allComplaints) {
            if (matchesFilters(cd)) {
                rows.add(ComplaintRowMapper.toRow(cd));
            }
        }
        this.filteredReports = rows;
    }

    private List<ComplaintDetail> sortByDateTimeDesc(List<ComplaintDetail> list) {
        list.sort(Comparator.comparing(ComplaintDetail::getDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private boolean matchesFilters(ComplaintDetail cd) {
        Timestamp filterTimestamp = cd.getLastUpdateTimestamp() != null
                ? cd.getLastUpdateTimestamp()
                : cd.getDateTime();

        if (!matchesDateRange(filterTimestamp))
            return false;
        if (!matchesStringField(cd.getType(), currentCategory, "All Categories"))
            return false;
        if (!matchesStringField(cd.getPurok(), currentPurok, "All Puroks"))
            return false;
        if (!matchesStringField(cd.getCurrentStatus(), currentStatus, "All Statuses"))
            return false;

        return true;
    }

    private boolean matchesDateRange(Timestamp ts) {
        if (ts == null)
            return currentFrom == null && currentTo == null;
        Date rd = stripTime(ts);
        if (currentFrom != null && rd.before(stripTime(currentFrom)))
            return false;
        if (currentTo != null && rd.after(stripTime(currentTo)))
            return false;
        return true;
    }

    private boolean matchesStringField(String value, String filter, String allMarker) {
        if (filter == null || allMarker.equalsIgnoreCase(filter))
            return true;
        return value != null && value.equalsIgnoreCase(filter);
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