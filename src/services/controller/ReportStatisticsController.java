package services.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import config.database.DBConnection;
import daos.ReportStatisticsDao;
import models.UserSession;

public class ReportStatisticsController {

    private final ReportStatisticsDao dao = new ReportStatisticsDao();

    public int countTotalReportByUser(UserSession us) {
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportByUser(con, us.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReport(UserSession us) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return -1;
        }
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReport(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByDate(UserSession us, String start, String end) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return -1;
        }
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportByDate(con, start, end);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByStatus(UserSession us, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return -1;
        }
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportByStatus(con, status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByUserAndStatus(UserSession us, String status) {
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportByUserAndStatus(con, us.getUserId(), status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportWithFilters(UserSession us, String start, String end,
            String category, String purok, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return -1;
        }
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportWithFilters(con, start, end, category, purok, status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByStatusWithFilters(UserSession us, String statusFilter,
            String start, String end, String category, String purok, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return -1;
        }
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportByStatusWithFilters(con, statusFilter,
                    start, end, category, purok, status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Object[]> getTrends(UserSession us, String groupBy) {
        return getTrends(us, groupBy, null, null, null, null, null);
    }

    public List<Object[]> getTrends(UserSession us, String groupBy, String start, String end) {
        return getTrends(us, groupBy, start, end, null, null, null);
    }

    public List<Object[]> getTrends(UserSession us, String groupBy, String start, String end,
            String category, String purok, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return Arrays.asList();
        }
        try (Connection con = DBConnection.connect()) {
            return dao.getTotalTrends(con, groupBy, start, end, category, purok, status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Arrays.asList();
    }

    public double[] getMonthlyCaseValues(UserSession us, String start, String end) {
        return getMonthlyCaseValues(us, start, end, null, null, null);
    }

    public double[] getMonthlyCaseValues(UserSession us, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, "MONTH", start, end, category, purok, status);
        Map<String, Integer> monthlyTotals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            String label = (String) row[0];
            Integer count = (Integer) row[2];
            monthlyTotals.merge(label, count, Integer::sum);
        }
        return monthlyTotals.values().stream().mapToDouble(Integer::doubleValue).toArray();
    }

    public String[] getMonthlyCaseLabels(UserSession us, String start, String end) {
        return getMonthlyCaseLabels(us, start, end, null, null, null);
    }

    public String[] getMonthlyCaseLabels(UserSession us, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, "MONTH", start, end, category, purok, status);
        Map<String, Integer> monthlyTotals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            monthlyTotals.putIfAbsent((String) row[0], 0);
        }
        return monthlyTotals.keySet().toArray(new String[0]);
    }

    public String[] getMonthlyCaseDetails(UserSession us, String start, String end) {
        return getMonthlyCaseDetails(us, start, end, null, null, null);
    }

    public String[] getMonthlyCaseDetails(UserSession us, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, "MONTH", start, end, category, purok, status);
        Map<String, Integer> monthTotals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            String label = (String) row[0];
            Integer count = (Integer) row[2];
            monthTotals.merge(label, count, Integer::sum);
        }
        return monthTotals.entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .toArray(String[]::new);
    }

    public int[] getCategoryValues(UserSession us, String start, String end) {
        return getCategoryValues(us, start, end, null, null, null);
    }

    public int[] getCategoryValues(UserSession us, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, "TYPE", start, end, category, purok, status);
        Map<String, Integer> typeTotals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            String label = (String) row[0];
            Integer count = (Integer) row[2];
            typeTotals.merge(label, count, Integer::sum);
        }
        return typeTotals.values().stream().mapToInt(Integer::intValue).toArray();
    }

    public String[] getCategoryLabels(UserSession us, String start, String end) {
        return getCategoryLabels(us, start, end, null, null, null);
    }

    public String[] getCategoryLabels(UserSession us, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, "TYPE", start, end, category, purok, status);
        Map<String, Integer> typeTotals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            typeTotals.putIfAbsent((String) row[0], 0);
        }
        return typeTotals.keySet().toArray(new String[0]);
    }

    // ------------------------------------------------------------
    // Case Status Counts — all overloads call the 5-param DAO method
    // ------------------------------------------------------------
    public int[] getCaseStatusCounts(UserSession us) {
        return getCaseStatusCounts(us, null, null, null, null, null);
    }

    public int[] getCaseStatusCounts(UserSession us, String start, String end,
            String category, String purok) {
        return getCaseStatusCounts(us, start, end, category, purok, null);
    }

    public int[] getCaseStatusCounts(UserSession us, String start, String end,
            String category, String purok, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return new int[0];
        }
        try (Connection con = DBConnection.connect()) {
            Map<String, Integer> data = dao.getCaseStatusCounts(con, start, end, category, purok, status);
            return data.values().stream().mapToInt(Integer::intValue).toArray();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[0];
    }

    public String[] getCaseStatusLabels(UserSession us) {
        return getCaseStatusLabels(us, null, null, null, null, null);
    }

    public String[] getCaseStatusLabels(UserSession us, String start, String end,
            String category, String purok) {
        return getCaseStatusLabels(us, start, end, category, purok, null);
    }

    public String[] getCaseStatusLabels(UserSession us, String start, String end,
            String category, String purok, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return new String[0];
        }
        try (Connection con = DBConnection.connect()) {
            Map<String, Integer> data = dao.getCaseStatusCounts(con, start, end, category, purok, status);
            return data.keySet().toArray(new String[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    public int[] getReportSourceCounts(UserSession us) {
        return getReportSourceCounts(us, null, null, null, null, null);
    }

    public int[] getReportSourceCounts(UserSession us, String start, String end,
            String category, String purok, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return new int[0];
        }
        try (Connection con = DBConnection.connect()) {
            Map<String, Integer> data = dao.getReportSourceByRole(con, start, end, category, purok, status);
            return data.values().stream().mapToInt(Integer::intValue).toArray();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[0];
    }

    public String[] getReportSourceLabels(UserSession us) {
        return getReportSourceLabels(us, null, null, null, null, null);
    }

    public String[] getReportSourceLabels(UserSession us, String start, String end,
            String category, String purok, String status) {
        if (!isAuthorized(us)) {
            showForbiddenError();
            return new String[0];
        }
        try (Connection con = DBConnection.connect()) {
            Map<String, Integer> data = dao.getReportSourceByRole(con, start, end, category, purok, status);
            return data.keySet().toArray(new String[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    // ------------------------------------------------------------
    // Generic trend methods — used by dynamic line graph
    // ------------------------------------------------------------
    public double[] getTrendValues(UserSession us, String groupBy, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, groupBy, start, end, category, purok, status);
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            String label = (String) row[0];
            Integer count = (Integer) row[2];
            totals.merge(label, count, Integer::sum);
        }
        return totals.values().stream().mapToDouble(Integer::doubleValue).toArray();
    }

    public String[] getTrendLabels(UserSession us, String groupBy, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, groupBy, start, end, category, purok, status);
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            totals.putIfAbsent((String) row[0], 0);
        }
        return totals.keySet().toArray(new String[0]);
    }

    public String[] getTrendDetails(UserSession us, String groupBy, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, groupBy, start, end, category, purok, status);
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            String label = (String) row[0];
            Integer count = (Integer) row[2];
            totals.merge(label, count, Integer::sum);
        }
        return totals.entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .toArray(String[]::new);
    }

    private boolean isAuthorized(UserSession us) {
        String role = us.getRole();
        return role.equalsIgnoreCase("captain") || role.equalsIgnoreCase("secretary");
    }

    private void showForbiddenError() {
        JOptionPane.showMessageDialog(null,
                "Error: Forbidden access!",
                "Forbidden",
                JOptionPane.WARNING_MESSAGE);
    }
}