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

/**
 * Fetches and filters all complaints/reports for the "All Reports" view.
 * Extends AbstractDashboardFetcher to hook into the background fetch
 * lifecycle. Holds the full unfiltered complaint list plus the currently
 * filtered subset based on date range, category, purok, and status criteria.
 */
public class AllReportsFetcher extends AbstractDashboardFetcher {
    /** Controller that handles database access for complaint records. */
    private final ComplaintServiceController csc;

    /** Complete list of all complaints, sorted newest-first after each fetch. */
    private List<ComplaintDetail> allComplaints = new ArrayList<>();
    /** Subset of allComplaints that satisfy the active filter criteria. */
    private List<Object[]> filteredReports = new ArrayList<>();

    /** Lower bound of the date range filter (inclusive, time stripped). */
    private Date currentFrom;
    /** Upper bound of the date range filter (inclusive, time stripped). */
    private Date currentTo;
    /** Selected category filter; null or "All Categories" means no filtering. */
    private String currentCategory;
    /** Selected purok filter; null or "All Puroks" means no filtering. */
    private String currentPurok;
    /** Selected status filter; null or "All Statuses" means no filtering. */
    private String currentStatus;

    /**
     * Constructs the fetcher and wires it to the main application frame.
     * 
     * @param app the main E_Report frame used for session/context access
     */
    public AllReportsFetcher(E_Report app) {
        super(app);
        this.csc = new ComplaintServiceController();
    }

    /**
     * Applies new filter criteria and immediately triggers a background refresh.
     * The fetcher stores the criteria locally; performFetch() uses them
     * to rebuild filteredReports from the already-fetched allComplaints.
     * 
     * @param fromDate start date (inclusive), or null for no lower bound
     * @param toDate   end date (inclusive), or null for no upper bound
     * @param category category name, or "All Categories" to disable filtering
     * @param purok    purok name, or "All Puroks" to disable filtering
     * @param status   status name, or "All Statuses" to disable filtering
     */
    public void applyFilters(Date fromDate, Date toDate, String category,
            String purok, String status) {
        this.currentFrom = fromDate;
        this.currentTo = toDate;
        this.currentCategory = category;
        this.currentPurok = purok;
        this.currentStatus = status;
        // Kick off the background fetch cycle defined in AbstractDashboardFetcher.
        refreshNow();
    }

    /**
     * Core fetch logic executed on a background thread.
     * 1. Loads every complaint from the database.
     * 2. Sorts them by date/time descending (most recent first).
     * 3. Re-applies the currently stored filters to produce filteredReports.
     */
    @Override
    protected void performFetch() {
        // Pull the full raw list from the service layer.
        List<ComplaintDetail> complaints = csc.getAllComplaints();
        // Defensive copy + sort so the UI always sees newest entries first.
        this.allComplaints = sortByDateTimeDesc(complaints != null ? complaints : new ArrayList<>());

        // Re-evaluate every complaint against the active filters.
        List<Object[]> rows = new ArrayList<>();
        for (ComplaintDetail cd : allComplaints) {
            if (matchesFilters(cd)) {
                // Convert the domain object into a flat Object[] row for JTable display.
                rows.add(ComplaintRowMapper.toRow(cd));
            }
        }
        this.filteredReports = rows;
    }

    /**
     * Sorts a complaint list by dateTime in descending order.
     * Null timestamps are pushed to the end so they do not obscure real data.
     * 
     * @param list the list to sort (modified in place)
     * @return the same list instance, now sorted
     */
    private List<ComplaintDetail> sortByDateTimeDesc(List<ComplaintDetail> list) {
        list.sort(Comparator.comparing(ComplaintDetail::getDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    /**
     * Evaluates a single complaint against all active filter dimensions.
     * 
     * @param cd the complaint to test
     * @return true only if the complaint passes every active filter
     */
    private boolean matchesFilters(ComplaintDetail cd) {
        // Prefer the last-update timestamp for date filtering; fall back to creation
        // time.
        Timestamp filterTimestamp = cd.getLastUpdateTimestamp() != null
                ? cd.getLastUpdateTimestamp()
                : cd.getDateTime();

        // Short-circuit: fail as soon as any filter rejects the record.
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

    /**
     * Checks whether a timestamp falls inside the current date range.
     * Both bounds are inclusive. Time-of-day is stripped from all dates so that
     * the comparison is purely calendar-day based.
     * 
     * @param ts the timestamp to evaluate; may be null
     * @return true if inside (or equal to) the range, or if no bounds are set
     */
    private boolean matchesDateRange(Timestamp ts) {
        // If the record has no date and no filters are set, allow it through.
        if (ts == null)
            return currentFrom == null && currentTo == null;

        // Normalize to midnight so HH:mm:ss does not affect the comparison.
        Date rd = stripTime(ts);

        // Reject if before the from-date or after the to-date.
        if (currentFrom != null && rd.before(stripTime(currentFrom)))
            return false;
        if (currentTo != null && rd.after(stripTime(currentTo)))
            return false;

        return true;
    }

    /**
     * Generic string filter matcher used for category, purok, and status.
     * A null filter or a filter equal to the "all" sentinel value disables
     * filtering
     * for that dimension and always returns true.
     * 
     * @param value     the complaint's field value
     * @param filter    the selected filter criterion
     * @param allMarker the sentinel string that means "show all"
     * @return true if the field matches the filter or if the filter is disabled
     */
    private boolean matchesStringField(String value, String filter, String allMarker) {
        if (filter == null || allMarker.equalsIgnoreCase(filter))
            return true;
        // Case-insensitive exact match; null values never match an active filter.
        return value != null && value.equalsIgnoreCase(filter);
    }

    /**
     * Strips the time component from a Date, leaving only year/month/day.
     * Used so that date-range comparisons behave intuitively (e.g., selecting
     * 2024-01-01 as the "to" date includes the entire day, not just 00:00:00).
     * 
     * @param date the source date; may be null
     * @return a new Date set to midnight of the same calendar day, or null if input
     *         was null
     */
    private Date stripTime(Date date) {
        if (date == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // Zero out all time fields so only the date portion remains.
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Formats a SQL Timestamp into a human-readable string.
     * Pattern: yyyy-MM-dd HH:mm:ss
     * 
     * @param ts the timestamp to format; may be null
     * @return the formatted string, or an empty string if the input was null
     */
    private String formatTs(Timestamp ts) {
        if (ts == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(ts.getTime()));
    }

    /**
     * Returns a defensive copy of the filtered report rows.
     * 
     * @return list of Object arrays suitable for JTable model population
     */
    public List<Object[]> getFilteredReports() {
        return new ArrayList<>(filteredReports);
    }

    /**
     * Returns a defensive copy of the full unfiltered complaint list.
     * 
     * @return all complaints sorted newest-first
     */
    public List<ComplaintDetail> getAllComplaints() {
        return new ArrayList<>(allComplaints);
    }
}