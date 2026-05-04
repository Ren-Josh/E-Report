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

/**
 * Controller for report statistics queries.
 * Provides convenience overloads for count, trend, category, status, and source
 * statistics. All methods enforce role-based access control (Captain or
 * Secretary).
 */
public class ReportStatisticsController {

    /** Data access object for statistics database operations. */
    private final ReportStatisticsDao dao = new ReportStatisticsDao();

    /**
     * Counts total reports submitted by a specific user.
     * No authorization check — any authenticated user may query their own data.
     * 
     * @param us the user session
     * @return the total count, or -1 on database error
     */
    public int countTotalReportByUser(UserSession us) {
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportByUser(con, us.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Counts total reports across the entire system.
     * Requires Captain or Secretary authorization.
     * 
     * @param us the user session
     * @return the total count, or -1 if unauthorized or on error
     */
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

    /**
     * Counts total reports within a date range.
     * Requires Captain or Secretary authorization.
     * 
     * @param us    the user session
     * @param start start date string (inclusive), or null for no lower bound
     * @param end   end date string (inclusive), or null for no upper bound
     * @return the count, or -1 if unauthorized or on error
     */
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

    /**
     * Counts total reports matching a specific status.
     * Requires Captain or Secretary authorization.
     * 
     * @param us     the user session
     * @param status the status string to match
     * @return the count, or -1 if unauthorized or on error
     */
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

    /**
     * Counts total reports by a specific user and status.
     * No authorization check — any authenticated user may query their own data.
     * 
     * @param us     the user session
     * @param status the status string to match
     * @return the count, or -1 on database error
     */
    public int countTotalReportByUserAndStatus(UserSession us, String status) {
        try (Connection con = DBConnection.connect()) {
            return dao.countTotalReportByUserAndStatus(con, us.getUserId(), status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Counts total reports matching all provided filter criteria.
     * Requires Captain or Secretary authorization.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return the count, or -1 if unauthorized or on error
     */
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

    /**
     * Counts total reports matching a specific status and all other filter
     * criteria.
     * Requires Captain or Secretary authorization.
     * 
     * @param us           the user session
     * @param statusFilter the status to count
     * @param start        start date string, or null
     * @param end          end date string, or null
     * @param category     category filter, or null
     * @param purok        purok filter, or null
     * @param status       additional status filter, or null
     * @return the count, or -1 if unauthorized or on error
     */
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

    /**
     * Overload: returns unfiltered trends grouped by the specified time unit.
     * 
     * @param us      the user session
     * @param groupBy SQL GROUP BY clause (e.g., "YEAR", "MONTH", "DAY")
     * @return a list of Object[] rows from the DAO
     */
    public List<Object[]> getTrends(UserSession us, String groupBy) {
        return getTrends(us, groupBy, null, null, null, null, null);
    }

    /**
     * Overload: returns trends grouped by time unit within a date range.
     * 
     * @param us      the user session
     * @param groupBy SQL GROUP BY clause
     * @param start   start date string, or null
     * @param end     end date string, or null
     * @return a list of Object[] rows from the DAO
     */
    public List<Object[]> getTrends(UserSession us, String groupBy, String start, String end) {
        return getTrends(us, groupBy, start, end, null, null, null);
    }

    /**
     * Core trend query with full filter support.
     * Requires Captain or Secretary authorization.
     * 
     * @param us       the user session
     * @param groupBy  SQL GROUP BY clause
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return a list of Object[] rows; empty list if unauthorized or on error
     */
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

    /**
     * Overload: returns monthly case trend values without additional filters.
     * 
     * @param us    the user session
     * @param start start date string, or null
     * @param end   end date string, or null
     * @return array of counts per month
     */
    public double[] getMonthlyCaseValues(UserSession us, String start, String end) {
        return getMonthlyCaseValues(us, start, end, null, null, null);
    }

    /**
     * Returns monthly case counts as a double array for chart rendering.
     * Aggregates duplicate month labels by summing their counts.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of monthly totals
     */
    public double[] getMonthlyCaseValues(UserSession us, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, "MONTH", start, end, category, purok, status);
        // Use LinkedHashMap to preserve chronological order while merging duplicates.
        Map<String, Integer> monthlyTotals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            String label = (String) row[0];
            Integer count = (Integer) row[2];
            monthlyTotals.merge(label, count, Integer::sum);
        }
        return monthlyTotals.values().stream().mapToDouble(Integer::doubleValue).toArray();
    }

    /**
     * Overload: returns monthly case trend labels without additional filters.
     * 
     * @param us    the user session
     * @param start start date string, or null
     * @param end   end date string, or null
     * @return array of month label strings
     */
    public String[] getMonthlyCaseLabels(UserSession us, String start, String end) {
        return getMonthlyCaseLabels(us, start, end, null, null, null);
    }

    /**
     * Returns the unique month labels for monthly trend data.
     * Uses putIfAbsent so duplicates do not overwrite existing entries.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of month label strings in chronological order
     */
    public String[] getMonthlyCaseLabels(UserSession us, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, "MONTH", start, end, category, purok, status);
        Map<String, Integer> monthlyTotals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            monthlyTotals.putIfAbsent((String) row[0], 0);
        }
        return monthlyTotals.keySet().toArray(new String[0]);
    }

    /**
     * Overload: returns monthly case trend details without additional filters.
     * 
     * @param us    the user session
     * @param start start date string, or null
     * @param end   end date string, or null
     * @return array of detail strings (label + count)
     */
    public String[] getMonthlyCaseDetails(UserSession us, String start, String end) {
        return getMonthlyCaseDetails(us, start, end, null, null, null);
    }

    /**
     * Returns human-readable detail strings for each month in the trend.
     * Format: "Month (count)".
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of detail strings
     */
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

    /**
     * Overload: returns category values without additional filters.
     * 
     * @param us    the user session
     * @param start start date string, or null
     * @param end   end date string, or null
     * @return array of category counts
     */
    public int[] getCategoryValues(UserSession us, String start, String end) {
        return getCategoryValues(us, start, end, null, null, null);
    }

    /**
     * Returns category distribution counts as an int array.
     * Aggregates duplicate category labels by summing their counts.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of category totals
     */
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

    /**
     * Overload: returns category labels without additional filters.
     * 
     * @param us    the user session
     * @param start start date string, or null
     * @param end   end date string, or null
     * @return array of category label strings
     */
    public String[] getCategoryLabels(UserSession us, String start, String end) {
        return getCategoryLabels(us, start, end, null, null, null);
    }

    /**
     * Returns the unique category labels for category distribution data.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of category label strings
     */
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

    /**
     * Overload: returns case status counts without filters.
     * 
     * @param us the user session
     * @return array of status counts
     */
    public int[] getCaseStatusCounts(UserSession us) {
        return getCaseStatusCounts(us, null, null, null, null, null);
    }

    /**
     * Overload: returns case status counts with partial filters.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @return array of status counts
     */
    public int[] getCaseStatusCounts(UserSession us, String start, String end,
            String category, String purok) {
        return getCaseStatusCounts(us, start, end, category, purok, null);
    }

    /**
     * Returns case status counts as an int array.
     * Requires Captain or Secretary authorization.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of status counts; empty array if unauthorized or on error
     */
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

    /**
     * Overload: returns case status labels without filters.
     * 
     * @param us the user session
     * @return array of status label strings
     */
    public String[] getCaseStatusLabels(UserSession us) {
        return getCaseStatusLabels(us, null, null, null, null, null);
    }

    /**
     * Overload: returns case status labels with partial filters.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @return array of status label strings
     */
    public String[] getCaseStatusLabels(UserSession us, String start, String end,
            String category, String purok) {
        return getCaseStatusLabels(us, start, end, category, purok, null);
    }

    /**
     * Returns the status labels corresponding to the case status counts.
     * Requires Captain or Secretary authorization.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of status label strings; empty array if unauthorized or on
     *         error
     */
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

    /**
     * Overload: returns report source counts without filters.
     * 
     * @param us the user session
     * @return array of source counts
     */
    public int[] getReportSourceCounts(UserSession us) {
        return getReportSourceCounts(us, null, null, null, null, null);
    }

    /**
     * Returns report source distribution counts as an int array.
     * Requires Captain or Secretary authorization.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of source counts; empty array if unauthorized or on error
     */
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

    /**
     * Overload: returns report source labels without filters.
     * 
     * @param us the user session
     * @return array of source label strings
     */
    public String[] getReportSourceLabels(UserSession us) {
        return getReportSourceLabels(us, null, null, null, null, null);
    }

    /**
     * Returns the source labels corresponding to the report source counts.
     * Requires Captain or Secretary authorization.
     * 
     * @param us       the user session
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of source label strings; empty array if unauthorized or on
     *         error
     */
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

    /**
     * Returns trend values as a double array for chart rendering.
     * Aggregates duplicate labels by summing their counts.
     * 
     * @param us       the user session
     * @param groupBy  SQL GROUP BY clause
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of trend values
     */
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

    /**
     * Returns trend labels as a String array for chart X-axis.
     * Uses putIfAbsent to preserve order and avoid duplicates.
     * 
     * @param us       the user session
     * @param groupBy  SQL GROUP BY clause
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of trend labels
     */
    public String[] getTrendLabels(UserSession us, String groupBy, String start, String end,
            String category, String purok, String status) {
        List<Object[]> trends = getTrends(us, groupBy, start, end, category, purok, status);
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Object[] row : trends) {
            totals.putIfAbsent((String) row[0], 0);
        }
        return totals.keySet().toArray(new String[0]);
    }

    /**
     * Returns trend detail strings combining label and aggregated count.
     * Format: "Label (count)".
     * 
     * @param us       the user session
     * @param groupBy  SQL GROUP BY clause
     * @param start    start date string, or null
     * @param end      end date string, or null
     * @param category category filter, or null
     * @param purok    purok filter, or null
     * @param status   status filter, or null
     * @return array of detail strings
     */
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

    /**
     * Checks whether the given user session has a role authorized to view
     * system-wide statistics. Only Captain and Secretary are allowed.
     * 
     * @param us the user session to check
     * @return true if the role is Captain or Secretary; false otherwise
     */
    private boolean isAuthorized(UserSession us) {
        String role = us.getRole();
        return role.equalsIgnoreCase("captain") || role.equalsIgnoreCase("secretary");
    }

    /**
     * Displays a modal warning dialog when an unauthorized user attempts
     * to access restricted statistics data.
     */
    private void showForbiddenError() {
        JOptionPane.showMessageDialog(null,
                "Error: Forbidden access!",
                "Forbidden",
                JOptionPane.WARNING_MESSAGE);
    }
}