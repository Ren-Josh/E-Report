package services.fetcher;

import app.E_Report;
import features.components.filter.TimeFilter;
import features.core.dashboardpanel.captain.panels.ActivityItem;
import services.controller.RecentActivityController;
import services.controller.ReportStatisticsController;

import java.util.Arrays;
import java.util.List;

/**
 * Background fetcher for the Captain role dashboard.
 * Gathers statistics (totals, trends, category/status/source breakdowns) and
 * recent activity items from the service layer, then caches the results so
 * the UI panel can read them safely on the Event Dispatch Thread.
 */
public class CaptainDashboardFetcher extends AbstractDashboardFetcher {
    /** Controller that provides report/statistics queries. */
    private final ReportStatisticsController rsc;
    /** Controller that provides recent activity feed data. */
    private final RecentActivityController rac;

    // -- Cached data --
    /** Total number of reports matching the active filters. */
    private int total;
    /** Number of reports with "Pending" status. */
    private int pending;
    /** Number of reports with "In Progress" status. */
    private int inProgress;
    /** Number of reports with "Resolved" status. */
    private int resolved;
    /** Numeric trend values for the line graph (Y-axis). */
    private double[] lineValues;
    /** Trend labels for the line graph (X-axis). */
    private String[] lineLabels;
    /** Detailed descriptions for each line graph point. */
    private String[] lineDetails;
    /** Values for the category distribution chart. */
    private int[] categoryValues;
    /** Labels for the category distribution chart. */
    private String[] categoryLabels;
    /** Values for the status breakdown chart. */
    private int[] statusValues;
    /** Labels for the status breakdown chart. */
    private String[] statusLabels;
    /** Background bar totals used to render proportional status bars. */
    private int[] backgroundTotals;
    /** Values for the report source chart. */
    private int[] sourceValues;
    /** Labels for the report source chart. */
    private String[] sourceLabels;
    /** Recent activity items displayed in the activity feed panel. */
    private List<ActivityItem> activities;

    // -- Current filter state (auto-refresh uses these) --
    /** Start date string for filtering (YYYY-MM-DD or similar). */
    private String currentStart;
    /** End date string for filtering. */
    private String currentEnd;
    /** Selected category filter; null or "All Categories" disables filtering. */
    private String currentCategory;
    /** Selected purok filter; null or "All Puroks" disables filtering. */
    private String currentPurok;
    /** Selected status filter; null or "All Statuses" disables filtering. */
    private String currentStatus;
    /** Time granularity selector that drives how trend data is grouped. */
    private TimeFilter currentTimeFilter;

    /**
     * Constructs the fetcher and wires it to the main application frame.
     * 
     * @param app the main E_Report frame used for session/context access
     */
    public CaptainDashboardFetcher(E_Report app) {
        super(app);
        this.rsc = new ReportStatisticsController();
        this.rac = new RecentActivityController();
    }

    /**
     * Applies new filter criteria and immediately triggers a background refresh.
     * Called from the dashboard panel whenever the user changes any filter control.
     * 
     * @param start      start date string, or null for no lower bound
     * @param end        end date string, or null for no upper bound
     * @param category   category name, or "All Categories" to disable filtering
     * @param purok      purok name, or "All Puroks" to disable filtering
     * @param status     status name, or "All Statuses" to disable filtering
     * @param timeFilter time granularity that determines trend grouping
     */
    public void applyFilters(String start, String end, String category,
            String purok, String status, TimeFilter timeFilter) {
        this.currentStart = start;
        this.currentEnd = end;
        this.currentCategory = category;
        this.currentPurok = purok;
        this.currentStatus = status;
        this.currentTimeFilter = timeFilter;
        // Kick off the background fetch cycle defined in AbstractDashboardFetcher.
        refreshNow();
    }

    /**
     * Core fetch logic executed on a background thread.
     * Pulls all statistics from the service layer using the currently stored
     * filter values, then writes the results into the cached fields so the UI
     * can read them safely after the fetch completes.
     */
    @Override
    protected void performFetch() {
        // Capture the current filter state locally so it cannot change mid-fetch.
        String start = currentStart;
        String end = currentEnd;
        String category = currentCategory;
        String purok = currentPurok;
        String status = currentStatus;
        TimeFilter tf = currentTimeFilter;

        // Determine SQL GROUP BY clause based on the selected time granularity.
        String groupBy = determineGroupBy(tf);

        // Fetch the four primary stat counters (filtered).
        int t = rsc.countTotalReportWithFilters(us, start, end, category, purok, status);
        int p = rsc.countTotalReportByStatusWithFilters(us, "Pending", start, end, category, purok, status);
        int ip = rsc.countTotalReportByStatusWithFilters(us, "In Progress", start, end, category, purok, status);
        int r = rsc.countTotalReportByStatusWithFilters(us, "Resolved", start, end, category, purok, status);

        // Fetch trend data for the line graph.
        double[] lv = rsc.getTrendValues(us, groupBy, start, end, category, purok, status);
        String[] ll = rsc.getTrendLabels(us, groupBy, start, end, category, purok, status);
        String[] ld = rsc.getTrendDetails(us, groupBy, start, end, category, purok, status);

        // Fetch category distribution data.
        int[] cv = rsc.getCategoryValues(us, start, end, category, purok, status);
        String[] cl = rsc.getCategoryLabels(us, start, end, category, purok, status);

        // Fetch status breakdown data.
        int[] sv = rsc.getCaseStatusCounts(us, start, end, category, purok, status);
        String[] sl = rsc.getCaseStatusLabels(us, start, end, category, purok, status);
        // Build background totals array: every bar uses the grand total so
        // the status bars render as proportions of the whole.
        int[] bt = new int[Math.max(sv.length, 1)];
        Arrays.fill(bt, t > 0 ? t : 1);

        // Fetch report source distribution data.
        int[] rv = rsc.getReportSourceCounts(us, start, end, category, purok, status);
        String[] rl = rsc.getReportSourceLabels(us, start, end, category, purok, status);

        // Fetch the recent activity feed (last 7 items).
        List<ActivityItem> act = rac.getRecentActivities(us, 7);

        // Write all fetched data into the cached fields.
        this.total = t;
        this.pending = p;
        this.inProgress = ip;
        this.resolved = r;
        this.lineValues = lv;
        this.lineLabels = ll;
        this.lineDetails = ld;
        this.categoryValues = cv;
        this.categoryLabels = cl;
        this.statusValues = sv;
        this.statusLabels = sl;
        this.backgroundTotals = bt;
        this.sourceValues = rv;
        this.sourceLabels = rl;
        this.activities = act;
    }

    /**
     * Maps a TimeFilter granularity to the corresponding SQL GROUP BY keyword.
     * Finer granularities (single month, single week) group by DAY;
     * medium granularities (single year, span of months) group by MONTH;
     * coarse or open-ended ranges group by YEAR.
     * 
     * @param timeFilter the selected time filter; null defaults to YEAR
     * @return the SQL GROUP BY string ("YEAR", "MONTH", or "DAY")
     */
    private String determineGroupBy(TimeFilter timeFilter) {
        if (timeFilter == null)
            return "YEAR";
        switch (timeFilter.getFilterType()) {
            case ALL_TIME:
                return "YEAR";
            case SPAN_OF_YEARS:
                return "YEAR";
            case SINGLE_YEAR:
                return "MONTH";
            case SPAN_OF_MONTHS:
                return "MONTH";
            case SINGLE_MONTH:
                return "DAY";
            case SINGLE_WEEK:
                return "DAY";
            default:
                return "YEAR";
        }
    }

    /**
     * Generates a human-readable title for the trend line graph based on the
     * current time filter granularity.
     * 
     * @return a descriptive title string such as "Yearly Case Graph" or "Daily Case
     *         Graph"
     */
    public String getLineGraphTitle() {
        if (currentTimeFilter == null) {
            return "All Case Graph";
        }
        switch (currentTimeFilter.getFilterType()) {
            case ALL_TIME:
                return "All Case Graph";
            case SPAN_OF_YEARS:
                return "Yearly Case Graph";
            case SINGLE_YEAR:
                return "Monthly Case Graph";
            case SPAN_OF_MONTHS:
                return "Monthly Case Graph";
            case SINGLE_MONTH:
                return "Weekly Case Graph";
            case SINGLE_WEEK:
                return "Daily Case Graph";
            default:
                return "Case Graph";
        }
    }

    /** @return total number of reports matching the active filters */
    public int getTotal() {
        return total;
    }

    /** @return number of reports with "Pending" status */
    public int getPending() {
        return pending;
    }

    /** @return number of reports with "In Progress" status */
    public int getInProgress() {
        return inProgress;
    }

    /** @return number of reports with "Resolved" status */
    public int getResolved() {
        return resolved;
    }

    /** @return trend values for the line graph */
    public double[] getLineValues() {
        return lineValues;
    }

    /** @return trend labels for the line graph X-axis */
    public String[] getLineLabels() {
        return lineLabels;
    }

    /** @return detailed descriptions for each line graph data point */
    public String[] getLineDetails() {
        return lineDetails;
    }

    /** @return values for the category distribution chart */
    public int[] getCategoryValues() {
        return categoryValues;
    }

    /** @return labels for the category distribution chart */
    public String[] getCategoryLabels() {
        return categoryLabels;
    }

    /** @return values for the status breakdown chart */
    public int[] getStatusValues() {
        return statusValues;
    }

    /** @return labels for the status breakdown chart */
    public String[] getStatusLabels() {
        return statusLabels;
    }

    /** @return background bar totals used for proportional status rendering */
    public int[] getBackgroundTotals() {
        return backgroundTotals;
    }

    /** @return values for the report source chart */
    public int[] getSourceValues() {
        return sourceValues;
    }

    /** @return labels for the report source chart */
    public String[] getSourceLabels() {
        return sourceLabels;
    }

    /** @return the recent activity items for the activity feed panel */
    public List<ActivityItem> getActivities() {
        return activities;
    }
}