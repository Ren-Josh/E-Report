package services.fetcher;

import app.E_Report;
import features.components.filter.TimeFilter;
import features.core.dashboardpanel.captain.ActivityItem;
import services.controller.RecentActivityController;
import services.controller.ReportStatisticsController;

import java.util.Arrays;
import java.util.List;

public class CaptainDashboardFetcher extends AbstractDashboardFetcher {
    private final ReportStatisticsController rsc;
    private final RecentActivityController rac;

    // -- Cached data --
    private int total, pending, inProgress, resolved;
    private double[] lineValues;
    private String[] lineLabels;
    private String[] lineDetails;
    private int[] categoryValues;
    private String[] categoryLabels;
    private int[] statusValues;
    private String[] statusLabels;
    private int[] backgroundTotals;
    private int[] sourceValues;
    private String[] sourceLabels;
    private List<ActivityItem> activities;

    // -- Current filter state (auto-refresh uses these) --
    private String currentStart, currentEnd, currentCategory, currentPurok, currentStatus;
    private TimeFilter currentTimeFilter;

    public CaptainDashboardFetcher(E_Report app) {
        super(app);
        this.rsc = new ReportStatisticsController();
        this.rac = new RecentActivityController();
    }

    /** Call this from the panel when the user applies filters. */
    public void applyFilters(String start, String end, String category,
            String purok, String status, TimeFilter timeFilter) {
        this.currentStart = start;
        this.currentEnd = end;
        this.currentCategory = category;
        this.currentPurok = purok;
        this.currentStatus = status;
        this.currentTimeFilter = timeFilter;
        refreshNow();
    }

    @Override
    protected void performFetch() {
        String start = currentStart;
        String end = currentEnd;
        String category = currentCategory;
        String purok = currentPurok;
        String status = currentStatus;
        TimeFilter tf = currentTimeFilter;

        String groupBy = determineGroupBy(tf);

        int t = rsc.countTotalReportWithFilters(us, start, end, category, purok, status);
        int p = rsc.countTotalReportByStatusWithFilters(us, "Pending", start, end, category, purok, status);
        int ip = rsc.countTotalReportByStatusWithFilters(us, "In Progress", start, end, category, purok, status);
        int r = rsc.countTotalReportByStatusWithFilters(us, "Resolved", start, end, category, purok, status);

        double[] lv = rsc.getTrendValues(us, groupBy, start, end, category, purok, status);
        String[] ll = rsc.getTrendLabels(us, groupBy, start, end, category, purok, status);
        String[] ld = rsc.getTrendDetails(us, groupBy, start, end, category, purok, status);

        int[] cv = rsc.getCategoryValues(us, start, end, category, purok, status);
        String[] cl = rsc.getCategoryLabels(us, start, end, category, purok, status);

        int[] sv = rsc.getCaseStatusCounts(us, start, end, category, purok, status);
        String[] sl = rsc.getCaseStatusLabels(us, start, end, category, purok, status);
        int[] bt = new int[Math.max(sv.length, 1)];
        Arrays.fill(bt, t > 0 ? t : 1);

        int[] rv = rsc.getReportSourceCounts(us, start, end, category, purok, status);
        String[] rl = rsc.getReportSourceLabels(us, start, end, category, purok, status);

        List<ActivityItem> act = rac.getRecentActivities(us, 7);

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
     * Dynamic title based on the exact filter type the user selected.
     *
     * All Time → "All Case Graph"
     * Span of Years → "Yearly Case Graph"
     * Single Year → "Monthly Case Graph"
     * Span of Months → "Monthly Case Graph"
     * Single Month → "Weekly Case Graph"
     * Single Week → "Daily Case Graph"
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

    // -- Getters (called on EDT after fireDataChanged) --
    public int getTotal() {
        return total;
    }

    public int getPending() {
        return pending;
    }

    public int getInProgress() {
        return inProgress;
    }

    public int getResolved() {
        return resolved;
    }

    public double[] getLineValues() {
        return lineValues;
    }

    public String[] getLineLabels() {
        return lineLabels;
    }

    public String[] getLineDetails() {
        return lineDetails;
    }

    public int[] getCategoryValues() {
        return categoryValues;
    }

    public String[] getCategoryLabels() {
        return categoryLabels;
    }

    public int[] getStatusValues() {
        return statusValues;
    }

    public String[] getStatusLabels() {
        return statusLabels;
    }

    public int[] getBackgroundTotals() {
        return backgroundTotals;
    }

    public int[] getSourceValues() {
        return sourceValues;
    }

    public String[] getSourceLabels() {
        return sourceLabels;
    }

    public List<ActivityItem> getActivities() {
        return activities;
    }
}