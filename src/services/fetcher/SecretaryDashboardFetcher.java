package services.fetcher;

import app.E_Report;
import models.ComplaintDetail;
import services.controller.ComplaintServiceController;
import services.controller.ReportStatisticsController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Background fetcher for the Secretary role dashboard.
 * Gathers system-wide statistics (total and by status), the 7 most recent
 * complaints across all users, and placeholder lists for activities and tasks.
 */
public class SecretaryDashboardFetcher extends AbstractDashboardFetcher {
    /** Controller that provides report/statistics queries. */
    private final ReportStatisticsController rsc;
    /** Controller that provides complaint CRUD and query operations. */
    private final ComplaintServiceController csc;

    /**
     * Cached stat counters for the Secretary dashboard.
     * Index 0 = total reports across the entire system.
     * Index 1 = Pending count, 2 = In Progress count, 3 = Resolved count.
     */
    private int[] statValues = new int[4];
    /** Cached table rows for the recent complaints list (newest 7). */
    private List<Object[]> reports = new ArrayList<>();
    /** Placeholder for future activity feed data. */
    private List<String> activities = new ArrayList<>();
    /** Placeholder for future task list data. */
    private List<String> tasks = new ArrayList<>();

    /**
     * Constructs the fetcher and wires it to the main application frame.
     * 
     * @param app the main E_Report frame used for session/context access
     */
    public SecretaryDashboardFetcher(E_Report app) {
        super(app);
        this.rsc = new ReportStatisticsController();
        this.csc = new ComplaintServiceController();
    }

    /**
     * Core fetch logic executed on a background thread.
     * 1. Queries system-wide totals and per-status counts (no user filter).
     * 2. Fetches all complaints, sorts by creation date descending,
     * limits to the 7 newest, and maps them to table rows.
     * 3. Initializes empty placeholder lists for activities and tasks.
     */
    @Override
    protected void performFetch() {
        // Stats — reuse dynamic status counting (same pattern as Resident)
        // but with no filters applied, giving system-wide numbers.
        String[] statuses = { "Pending", "In Progress", "Resolved" };
        // Index 0: grand total of all reports in the system.
        statValues[0] = rsc.countTotalReportWithFilters(us, null, null, null, null, null);
        // Indices 1-3: counts for each status with no filter constraints.
        for (int i = 0; i < statuses.length; i++) {
            statValues[i + 1] = rsc.countTotalReportByStatusWithFilters(
                    us, statuses[i], null, null, null, null, null);
        }

        // Reports — fetch every complaint, then keep only the newest 7.
        List<ComplaintDetail> complaints = csc.getAllComplaints();
        // Sort so the most recently created complaints appear first.
        List<ComplaintDetail> sorted = sortByDateTimeDesc(complaints);
        // Truncate to the top 7 for the dashboard preview.
        List<ComplaintDetail> recent = limit(sorted, 7);

        // Convert the selected complaints into flat table rows.
        this.reports = mapToRows(recent);

        // TODO: Wire up real Activity & Task controllers when available.
        // For now, keep empty lists so the UI has something non-null to render.
        this.activities = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    /**
     * Sorts a complaint list by dateTime descending, with null-safe handling.
     * Null timestamps are pushed to the end so they do not obscure real data.
     * Creates a defensive copy so the original list is not modified.
     * 
     * @param list the source list; may be null
     * @return a new list sorted newest-first, or an empty list if input was null
     */
    private List<ComplaintDetail> sortByDateTimeDesc(List<ComplaintDetail> list) {
        if (list == null)
            return new ArrayList<>();
        // Defensive copy to avoid mutating the caller's list.
        List<ComplaintDetail> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(ComplaintDetail::getDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return copy;
    }

    /**
     * Limits a list to its first N elements.
     * If the list is shorter than the limit, returns it unchanged.
     * If the list is null, returns an empty list.
     * 
     * @param list the source list; may be null
     * @param max  the maximum number of elements to retain
     * @return the original list (if short enough), a subList, or an empty list
     */
    private List<ComplaintDetail> limit(List<ComplaintDetail> list, int max) {
        if (list == null || list.size() <= max)
            return list != null ? list : new ArrayList<>();
        // subList creates a view; no new objects are allocated for the elements.
        return list.subList(0, max);
    }

    /**
     * Converts a list of ComplaintDetail objects into table-ready Object[] rows.
     * Delegates the actual field mapping to ComplaintRowMapper.toRow().
     * 
     * @param complaints the source list; may be null
     * @return a list of Object arrays suitable for JTable model population
     */
    private List<Object[]> mapToRows(List<ComplaintDetail> complaints) {
        List<Object[]> rows = new ArrayList<>();
        if (complaints == null)
            return rows;
        for (ComplaintDetail cd : complaints) {
            rows.add(ComplaintRowMapper.toRow(cd));
        }
        return rows;
    }

    /**
     * Returns a clone of the cached stat values array.
     * 
     * @return int[4] = {total, pending, inProgress, resolved}
     */
    public int[] getStatValues() {
        return statValues.clone();
    }

    /**
     * Returns a defensive copy of the cached report rows.
     * 
     * @return list of Object arrays for JTable model population
     */
    public List<Object[]> getReports() {
        return new ArrayList<>(reports);
    }

    /**
     * Returns a defensive copy of the cached activities list.
     * Currently returns an empty list until real activity data is wired in.
     * 
     * @return list of activity strings
     */
    public List<String> getActivities() {
        return new ArrayList<>(activities);
    }

    /**
     * Returns a defensive copy of the cached tasks list.
     * Currently returns an empty list until real task data is wired in.
     * 
     * @return list of task strings
     */
    public List<String> getTasks() {
        return new ArrayList<>(tasks);
    }
}