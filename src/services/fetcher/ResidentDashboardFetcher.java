package services.fetcher;

import app.E_Report;
import models.ComplaintDetail;
import models.UserSession;
import services.controller.ComplaintServiceController;
import services.controller.ReportStatisticsController;

import java.util.ArrayList;
import java.util.List;

/**
 * Background fetcher for the Resident role dashboard.
 * Gathers per-user statistics (total and by status) and the most recent
 * complaints submitted by the currently logged-in resident.
 */
public class ResidentDashboardFetcher extends AbstractDashboardFetcher {
    /** Controller that provides report/statistics queries. */
    private final ReportStatisticsController rsc;
    /** Controller that provides complaint CRUD and query operations. */
    private final ComplaintServiceController csc;

    /**
     * Cached stat counters for the Resident dashboard.
     * Index 0 = total reports by this user.
     * Index 1 = Pending count, 2 = In Progress count, 3 = Resolved count.
     */
    private int[] statValues = new int[4];
    /**
     * Cached table rows derived from the raw complaints (via ComplaintRowMapper).
     */
    private List<Object[]> reports = new ArrayList<>();
    /** Cached raw ComplaintDetail objects fetched from the service layer. */
    private List<ComplaintDetail> rawComplaints = new ArrayList<>();

    /**
     * Constructs the fetcher and wires it to the main application frame.
     * 
     * @param app the main E_Report frame used for session/context access
     */
    public ResidentDashboardFetcher(E_Report app) {
        super(app);
        this.rsc = new ReportStatisticsController();
        this.csc = new ComplaintServiceController();
    }

    /**
     * Core fetch logic executed on a background thread.
     * Pulls the current user's session from the app, queries the total report
     * count and per-status breakdowns, then fetches the 7 most recent
     * complaints for that user and converts them into table rows.
     */
    @Override
    protected void performFetch() {
        // Retrieve the active session so all queries are scoped to this resident.
        UserSession session = app.getUserSession();

        // Status strings used to populate indices 1-3 of statValues.
        String[] statuses = { "Pending", "In Progress", "Resolved" };
        // Index 0: grand total of all reports submitted by this user.
        statValues[0] = rsc.countTotalReportByUser(session);
        // Indices 1-3: counts for each status in the same order as the statuses array.
        for (int i = 0; i < statuses.length; i++) {
            statValues[i + 1] = rsc.countTotalReportByUserAndStatus(session, statuses[i]);
        }

        // Fetch the 7 most recent complaints for this user from the service layer.
        List<ComplaintDetail> complaints = csc.getRecentComplaintByUser(session, 7);
        // Defensive copy to prevent external modification of the cached list.
        this.rawComplaints = complaints != null ? new ArrayList<>(complaints) : new ArrayList<>();
        // Convert the domain objects into flat Object[] rows for JTable display.
        this.reports = mapToRows(rawComplaints);
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
     * Returns a defensive copy of the cached raw complaint objects.
     * Useful when the UI needs the full domain object rather than flat rows.
     * 
     * @return list of ComplaintDetail instances
     */
    public List<ComplaintDetail> getRawComplaints() {
        return new ArrayList<>(rawComplaints);
    }

    /**
     * Alias for getRawComplaints() to support callers expecting a generic
     * "getComplaintDetails" method name.
     * 
     * @return list of ComplaintDetail instances
     */
    public List<ComplaintDetail> getComplaintDetails() {
        return getRawComplaints();
    }
}