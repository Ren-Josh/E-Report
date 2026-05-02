package services.fetcher;

import app.E_Report;
import models.ComplaintDetail;
import services.controller.ComplaintServiceController;
import services.controller.ReportStatisticsController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SecretaryDashboardFetcher extends AbstractDashboardFetcher {
    private final ReportStatisticsController rsc;
    private final ComplaintServiceController csc;

    private int[] statValues = new int[4];
    private List<Object[]> reports = new ArrayList<>();
    private List<String> activities = new ArrayList<>();
    private List<String> tasks = new ArrayList<>();

    public SecretaryDashboardFetcher(E_Report app) {
        super(app);
        this.rsc = new ReportStatisticsController();
        this.csc = new ComplaintServiceController();
    }

    @Override
    protected void performFetch() {
        // Stats — reuse dynamic status counting (same pattern as Resident)
        String[] statuses = { "Pending", "In Progress", "Resolved" };
        statValues[0] = rsc.countTotalReportWithFilters(us, null, null, null, null, null);
        for (int i = 0; i < statuses.length; i++) {
            statValues[i + 1] = rsc.countTotalReportByStatusWithFilters(
                    us, statuses[i], null, null, null, null, null);
        }

        // Reports — newest 7, sorted by creation date
        List<ComplaintDetail> complaints = csc.getAllComplaints();
        List<ComplaintDetail> sorted = sortByDateTimeDesc(complaints);
        List<ComplaintDetail> recent = limit(sorted, 7);

        this.reports = mapToRows(recent);

        // TODO: Wire up real Activity & Task controllers when available
        this.activities = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    /** Sort by dateTime descending, null-safe */
    private List<ComplaintDetail> sortByDateTimeDesc(List<ComplaintDetail> list) {
        if (list == null)
            return new ArrayList<>();
        List<ComplaintDetail> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(ComplaintDetail::getDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return copy;
    }

    /** Limit list to first N elements */
    private List<ComplaintDetail> limit(List<ComplaintDetail> list, int max) {
        if (list == null || list.size() <= max)
            return list != null ? list : new ArrayList<>();
        return list.subList(0, max);
    }

    /** Centralized row mapping */
    private List<Object[]> mapToRows(List<ComplaintDetail> complaints) {
        List<Object[]> rows = new ArrayList<>();
        if (complaints == null)
            return rows;
        for (ComplaintDetail cd : complaints) {
            rows.add(ComplaintRowMapper.toRow(cd));
        }
        return rows;
    }

    public int[] getStatValues() {
        return statValues.clone();
    }

    public List<Object[]> getReports() {
        return new ArrayList<>(reports);
    }

    public List<String> getActivities() {
        return new ArrayList<>(activities);
    }

    public List<String> getTasks() {
        return new ArrayList<>(tasks);
    }
}