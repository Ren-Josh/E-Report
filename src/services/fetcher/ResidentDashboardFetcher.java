package services.fetcher;

import app.E_Report;
import models.ComplaintDetail;
import models.UserSession;
import services.controller.ComplaintServiceController;
import services.controller.ReportStatisticsController;

import java.util.ArrayList;
import java.util.List;

public class ResidentDashboardFetcher extends AbstractDashboardFetcher {
    private final ReportStatisticsController rsc;
    private final ComplaintServiceController csc;

    private int[] statValues = new int[4];
    private List<Object[]> reports = new ArrayList<>();
    private List<ComplaintDetail> rawComplaints = new ArrayList<>();

    public ResidentDashboardFetcher(E_Report app) {
        super(app);
        this.rsc = new ReportStatisticsController();
        this.csc = new ComplaintServiceController();
    }

    @Override
    protected void performFetch() {
        UserSession session = app.getUserSession();

        int total = rsc.countTotalReportByUser(session);
        int pending = rsc.countTotalReportByUserAndStatus(session, "Pending");
        int inProgress = rsc.countTotalReportByUserAndStatus(session, "In Progress");
        int resolved = rsc.countTotalReportByUserAndStatus(session, "Resolved");

        List<ComplaintDetail> complaints = csc.getRecentComplaintByUser(session, 7);
        List<Object[]> rows = new ArrayList<>();

        if (complaints != null) {
            for (ComplaintDetail cd : complaints) {
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

        this.statValues = new int[] { total, pending, inProgress, resolved };
        this.rawComplaints = complaints != null ? new ArrayList<>(complaints) : new ArrayList<>();
        this.reports = rows;
    }

    public int[] getStatValues() {
        return statValues.clone();
    }

    public List<Object[]> getReports() {
        return new ArrayList<>(reports);
    }

    public List<ComplaintDetail> getRawComplaints() {
        return new ArrayList<>(rawComplaints);
    }
}