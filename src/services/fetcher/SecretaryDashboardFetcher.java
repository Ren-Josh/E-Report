package services.fetcher;

import app.E_Report;
import models.ComplaintDetail;
import services.controller.ComplaintServiceController;
import services.controller.ReportStatisticsController;

import java.util.ArrayList;
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
        // Stats — global counts (null filters = all records)
        int total = rsc.countTotalReportWithFilters(us, null, null, null, null, null);
        int pending = rsc.countTotalReportByStatusWithFilters(us, "Pending", null, null, null, null, null);
        int inProgress = rsc.countTotalReportByStatusWithFilters(us, "In Progress", null, null, null, null, null);
        int resolved = rsc.countTotalReportByStatusWithFilters(us, "Resolved", null, null, null, null, null);

        // Recent reports (all complaints, newest 7)
        List<ComplaintDetail> complaints = csc.getAllComplaints();
        List<Object[]> rows = new ArrayList<>();

        if (complaints != null) {
            complaints.sort((a, b) -> {
                var t1 = a.getDateTime();
                var t2 = b.getDateTime();
                if (t1 == null || t2 == null)
                    return 0;
                return t2.compareTo(t1);
            });
            int limit = Math.min(complaints.size(), 7);
            for (int i = 0; i < limit; i++) {
                ComplaintDetail cd = complaints.get(i);
                Object[] row = new Object[7];
                row[0] = cd.getComplaintId();
                row[1] = cd.getType();
                row[2] = cd.getPurok();
                row[3] = cd.getDateTime();
                row[4] = cd.getLastUpdateTimestamp() != null
                        ? cd.getLastUpdateTimestamp()
                        : cd.getDateTime();
                row[5] = cd.getCurrentStatus();
                row[6] = "View";
                rows.add(row);
            }
        }

        // TODO: Wire up real Activity & Task controllers when available
        List<String> acts = new ArrayList<>();
        List<String> tks = new ArrayList<>();

        this.statValues = new int[] { total, pending, inProgress, resolved };
        this.reports = rows;
        this.activities = acts;
        this.tasks = tks;
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