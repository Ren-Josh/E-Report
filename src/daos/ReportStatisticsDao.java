package daos;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for report statistics and aggregated counts.
 */
public class ReportStatisticsDao {

    // ===== SQL STRINGS =====
    private String countUserReportsSQL;
    private String countAllReportsSQL;
    private String countByDateSQL;
    private String baseFilteredFromSQL;
    private String dateFilterColumnSQL;

    public ReportStatisticsDao() {
        // ===== INIT SQL =====
        countUserReportsSQL = "SELECT COUNT(CD_ID) AS Total FROM Complaint_Detail WHERE UI_ID = ?";
        countAllReportsSQL = "SELECT COUNT(CD_ID) AS Total FROM Complaint_Detail";
        countByDateSQL = "SELECT COUNT(*) AS Total FROM Complaint_Detail WHERE date_time_created BETWEEN ? AND ?";

        baseFilteredFromSQL = """
                FROM Complaint_Detail cd
                LEFT JOIN (
                    SELECT CD_ID, MAX(date_time_updated) AS latest_dateTimeUpdated
                    FROM Complaint_History_Detail
                    GROUP BY CD_ID
                ) latest_chd ON latest_chd.CD_ID = cd.CD_ID
                """;

        dateFilterColumnSQL = "(CASE WHEN cd.current_status = 'Pending' THEN cd.date_time_created ELSE latest_chd.latest_dateTimeUpdated END)";
    }

    // ═══════════════════════════════════════════════════════════════
    // Simple counts (no filtering)
    // ═══════════════════════════════════════════════════════════════

    public int countTotalReportByUser(Connection con, int UI_ID) {
        return executeIntQuery(con, countUserReportsSQL, UI_ID);
    }

    public int countTotalReport(Connection con) {
        return executeIntQuery(con, countAllReportsSQL);
    }

    public int countTotalReportByDate(Connection con, String start, String end) {
        return executeIntQuery(con, countByDateSQL, start, end);
    }

    public int countTotalReportByUserAndStatus(Connection con, int UI_ID, String status) {
        String sql = "SELECT COUNT(*) AS Total FROM Complaint_Detail WHERE UI_ID = ? AND current_status = ?";
        return executeIntQuery(con, sql, UI_ID, status);
    }

    public int countTotalReportByStatus(Connection con, String status) {
        String sql = "SELECT COUNT(*) AS Total FROM Complaint_Detail WHERE current_status = ?";
        return executeIntQuery(con, sql, status);
    }

    // ═══════════════════════════════════════════════════════════════
    // Filtered counts — DYNAMIC SQL BUILDER eliminates duplication
    // ═══════════════════════════════════════════════════════════════

    public int countTotalReportWithFilters(Connection con, String start, String end,
            String category, String purok, String status) {
        String sql = buildCountSql(null) + buildWhereClause(start, end, category, purok, status, false);
        return executeIntQueryWithParams(con, sql, start, end, category, purok, status);
    }

    public int countTotalReportByStatusWithFilters(Connection con, String statusFilter,
            String start, String end, String category, String purok, String status) {
        String sql = buildCountSql("cd.current_status = ?") +
                buildWhereClause(start, end, category, purok, status, true);
        return executeIntQueryWithStatusFilter(con, sql, statusFilter, start, end, category, purok, status);
    }

    // ═══════════════════════════════════════════════════════════════
    // Grouped / trend queries
    // ═══════════════════════════════════════════════════════════════

    public List<Object[]> getTotalTrends(Connection con, String groupBy, String start, String end,
            String category, String purok, String status) {
        String select = buildTrendSelect(groupBy);
        String groupByClause = buildTrendGroupBy(groupBy);
        String orderBy = buildTrendOrderBy(groupBy);

        String sql = "SELECT " + select + " " + baseFilteredFromSQL +
                buildWhereClause(start, end, category, purok, status, false) +
                " " + groupByClause + " " + orderBy;

        return executeTrendQuery(con, sql, start, end, category, purok, status);
    }

    public Map<String, Integer> getCaseStatusCounts(Connection con) {
        String sql = "SELECT current_status, COUNT(*) AS total FROM Complaint_Detail " +
                "GROUP BY current_status ORDER BY total DESC";
        return executeStringIntMap(con, sql);
    }

    public Map<String, Integer> getCaseStatusCounts(Connection con, String start, String end,
            String category, String purok, String status) {
        String sql = "SELECT cd.current_status, COUNT(DISTINCT cd.CD_ID) AS total " + baseFilteredFromSQL +
                buildWhereClause(start, end, category, purok, status, false) +
                " GROUP BY cd.current_status ORDER BY total DESC";
        return executeStringIntMap(con, sql, start, end, category, purok, status);
    }

    public Map<String, Integer> getReportSourceByRole(Connection con) {
        String sql = "SELECT cr.role, COUNT(*) AS total FROM Complaint_Detail cd " +
                "INNER JOIN Credential cr ON cd.UI_ID = cr.UI_ID " +
                "GROUP BY cr.role ORDER BY total DESC";
        return executeStringIntMap(con, sql);
    }

    public Map<String, Integer> getReportSourceByRole(Connection con, String start, String end,
            String category, String purok, String status) {
        String sql = "SELECT cr.role, COUNT(DISTINCT cd.CD_ID) AS total " + baseFilteredFromSQL +
                "INNER JOIN Credential cr ON cd.UI_ID = cr.UI_ID " +
                buildWhereClause(start, end, category, purok, status, false) +
                " GROUP BY cr.role ORDER BY total DESC";
        return executeStringIntMap(con, sql, start, end, category, purok, status);
    }

    // ═══════════════════════════════════════════════════════════════
    // SQL BUILDER HELPERS
    // ═══════════════════════════════════════════════════════════════

    private String buildCountSql(String extraWhere) {
        String base = "SELECT COUNT(DISTINCT cd.CD_ID) AS Total " + baseFilteredFromSQL;
        if (extraWhere != null) {
            base += " WHERE " + extraWhere;
        }
        return base;
    }

    private String buildWhereClause(String start, String end, String category,
            String purok, String status, boolean hasLeadingWhere) {
        boolean hasDates = hasValue(start) && hasValue(end);
        boolean hasCategory = hasValue(category);
        boolean hasPurok = hasValue(purok);
        boolean hasStatus = hasValue(status);

        List<String> conditions = new ArrayList<>();
        if (hasDates) {
            conditions.add(dateFilterColumnSQL + " BETWEEN ? AND ?");
        }
        if (hasCategory)
            conditions.add("cd.type = ?");
        if (hasPurok)
            conditions.add("cd.purok = ?");
        if (hasStatus)
            conditions.add("cd.current_status = ?");

        if (conditions.isEmpty())
            return "";

        String prefix = hasLeadingWhere ? " AND " : " WHERE ";
        return prefix + String.join(" AND ", conditions);
    }

    private String buildTrendSelect(String groupBy) {
        return switch (groupBy) {
            case "MONTH" -> "MONTHNAME(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
            case "YEAR" -> "YEAR(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
            case "DAY" -> "DATE(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
            case "TYPE" -> "cd.type AS label, cd.type, COUNT(*) AS total";
            default -> "DATE(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
        };
    }

    private String buildTrendGroupBy(String groupBy) {
        return switch (groupBy) {
            case "MONTH" ->
                "GROUP BY YEAR(cd.date_time_created), MONTH(cd.date_time_created), MONTHNAME(cd.date_time_created), cd.type";
            case "YEAR" -> "GROUP BY YEAR(cd.date_time_created), cd.type";
            case "DAY", "TYPE" -> "GROUP BY DATE(cd.date_time_created), cd.type";
            default -> "GROUP BY DATE(cd.date_time_created), cd.type";
        };
    }

    private String buildTrendOrderBy(String groupBy) {
        return "TYPE".equals(groupBy)
                ? "ORDER BY total DESC, cd.type"
                : "ORDER BY label, cd.type";
    }

    // ═══════════════════════════════════════════════════════════════
    // EXECUTION HELPERS
    // ═══════════════════════════════════════════════════════════════

    private boolean hasValue(String s) {
        return s != null && !s.isEmpty();
    }

    private int executeIntQuery(Connection con, String sql, Object... params) {
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("Total") : -1;
            }
        } catch (SQLException e) {
            System.err.println("Error in executeIntQuery: " + sql);
            e.printStackTrace();
            return -1;
        }
    }

    private int executeIntQueryWithParams(Connection con, String sql,
            String start, String end, String category, String purok, String status) {
        List<Object> params = new ArrayList<>();
        if (hasValue(start) && hasValue(end)) {
            params.add(start);
            params.add(end);
        }
        if (hasValue(category))
            params.add(category);
        if (hasValue(purok))
            params.add(purok);
        if (hasValue(status))
            params.add(status);
        return executeIntQuery(con, sql, params.toArray());
    }

    private int executeIntQueryWithStatusFilter(Connection con, String sql, String statusFilter,
            String start, String end, String category, String purok, String status) {
        List<Object> params = new ArrayList<>();
        params.add(statusFilter);
        if (hasValue(start) && hasValue(end)) {
            params.add(start);
            params.add(end);
        }
        if (hasValue(category))
            params.add(category);
        if (hasValue(purok))
            params.add(purok);
        if (hasValue(status))
            params.add(status);
        return executeIntQuery(con, sql, params.toArray());
    }

    private List<Object[]> executeTrendQuery(Connection con, String sql,
            String start, String end, String category, String purok, String status) {
        List<Object[]> list = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int idx = 1;
            if (hasValue(start) && hasValue(end)) {
                stmt.setString(idx++, start);
                stmt.setString(idx++, end);
            }
            if (hasValue(category))
                stmt.setString(idx++, category);
            if (hasValue(purok))
                stmt.setString(idx++, purok);
            if (hasValue(status))
                stmt.setString(idx++, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[] {
                            rs.getString("label"),
                            rs.getString("type"),
                            rs.getInt("total")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Map<String, Integer> executeStringIntMap(Connection con, String sql, Object... params) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in executeStringIntMap");
            e.printStackTrace();
        }
        return map;
    }

    private Map<String, Integer> executeStringIntMap(Connection con, String sql,
            String start, String end, String category, String purok, String status) {
        List<Object> params = new ArrayList<>();
        if (hasValue(start) && hasValue(end)) {
            params.add(start);
            params.add(end);
        }
        if (hasValue(category))
            params.add(category);
        if (hasValue(purok))
            params.add(purok);
        if (hasValue(status))
            params.add(status);
        return executeStringIntMap(con, sql, params.toArray());
    }
}