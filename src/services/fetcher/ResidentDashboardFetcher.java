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

        String[] statuses = { "Pending", "In Progress", "Resolved" };
        statValues[0] = rsc.countTotalReportByUser(session);
        for (int i = 0; i < statuses.length; i++) {
            statValues[i + 1] = rsc.countTotalReportByUserAndStatus(session, statuses[i]);
        }

        List<ComplaintDetail> complaints = csc.getRecentComplaintByUser(session, 7);
        this.rawComplaints = complaints != null ? new ArrayList<>(complaints) : new ArrayList<>();
        this.reports = mapToRows(rawComplaints);
    }

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

    public List<ComplaintDetail> getRawComplaints() {
        return new ArrayList<>(rawComplaints);
    }

    public List<ComplaintDetail> getComplaintDetails() {
        return getRawComplaints();
    }
}