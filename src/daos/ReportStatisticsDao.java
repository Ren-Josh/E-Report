package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportStatisticsDao {

    private final String queryUserReportCount = "SELECT COUNT(C_ID) AS Total FROM Complaint WHERE UI_ID = ?";
    private final String queryAllReportCount = "SELECT COUNT(C_ID) AS Total FROM Complaint";
    private final String queryStatusCount = "SELECT COUNT(*) AS Total FROM Complaint_Detail cd " +
            "INNER JOIN Complaint c ON cd.CD_ID = c.CD_ID " +
            "WHERE c.UI_ID = ? AND cd.current_status = ?";
    private final String queryAllStatusCount = "SELECT COUNT(*) AS Total FROM Complaint_Detail WHERE current_status = ?";
    private final String queryReportCountByDate = "SELECT COUNT(*) AS Total FROM Complaint_Detail cd " +
            "INNER JOIN Complaint c ON cd.CD_ID = c.CD_ID " +
            "WHERE cd.date_time_created BETWEEN ? AND ?";

    public ReportStatisticsDao() {
    }

    public int countTotalReportByUser(Connection con, int UI_ID) {
        try (PreparedStatement stmt = con.prepareStatement(queryUserReportCount)) {
            stmt.setInt(1, UI_ID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving report count for UI_ID: " + UI_ID);
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReport(Connection con) {
        try (PreparedStatement stmt = con.prepareStatement(queryAllReportCount)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving total report count");
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByDate(Connection con, String start, String end) {
        try (PreparedStatement stmt = con.prepareStatement(queryReportCountByDate)) {
            stmt.setString(1, start);
            stmt.setString(2, end);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving report count by date");
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByUserAndStatus(Connection con, int UI_ID, String status) {
        try (PreparedStatement stmt = con.prepareStatement(queryStatusCount)) {
            stmt.setInt(1, UI_ID);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving status count for UI_ID: " + UI_ID);
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByStatus(Connection con, String status) {
        try (PreparedStatement stmt = con.prepareStatement(queryAllStatusCount)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving report count for status: " + status);
            e.printStackTrace();
        }
        return -1;
    }

    public List<Object[]> getTotalTrends(Connection con, String groupBy, String start, String end,
            String category, String purok, String status) {
        List<Object[]> list = new ArrayList<>();
        boolean hasDates = start != null && !start.isEmpty() && end != null && !end.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasPurok = purok != null && !purok.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();

        String select;
        String groupByClause;
        String orderBy;

        if ("MONTH".equals(groupBy)) {
            select = "MONTHNAME(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
            groupByClause = "GROUP BY YEAR(cd.date_time_created), MONTH(cd.date_time_created), MONTHNAME(cd.date_time_created), cd.type";
            orderBy = "ORDER BY YEAR(cd.date_time_created), MONTH(cd.date_time_created), cd.type";
        } else if ("YEAR".equals(groupBy)) {
            select = "YEAR(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
            groupByClause = "GROUP BY YEAR(cd.date_time_created), cd.type";
            orderBy = "ORDER BY YEAR(cd.date_time_created), cd.type";
        } else if ("DAY".equals(groupBy)) {
            select = "DATE(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
            groupByClause = "GROUP BY DATE(cd.date_time_created), cd.type";
            orderBy = "ORDER BY DATE(cd.date_time_created), cd.type";
        } else if ("TYPE".equals(groupBy)) {
            select = "cd.type AS label, cd.type, COUNT(*) AS total";
            groupByClause = "GROUP BY cd.type";
            orderBy = "ORDER BY total DESC, cd.type";
        } else {
            select = "DATE(cd.date_time_created) AS label, cd.type, COUNT(*) AS total";
            groupByClause = "GROUP BY DATE(cd.date_time_created), cd.type";
            orderBy = "ORDER BY DATE(cd.date_time_created), cd.type";
        }

        String sql = "SELECT " + select + " " +
                "FROM Complaint_Detail cd " +
                "INNER JOIN Complaint c ON cd.CD_ID = c.CD_ID " +
                "LEFT JOIN (SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated " +
                "FROM Complaint_History ch " +
                "INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID " +
                "GROUP BY ch.CD_ID) latest_chd ON latest_chd.CD_ID = cd.CD_ID";

        List<String> params = new ArrayList<>();

        if (hasDates) {
            sql += " WHERE (CASE WHEN cd.current_status = 'Pending' THEN cd.date_time_created ELSE latest_chd.latest_dateTimeUpdated END) BETWEEN ? AND ?";
            params.add(start);
            params.add(end);
        }
        if (hasCategory) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.type = ?";
            params.add(category);
        }
        if (hasPurok) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.purok = ?";
            params.add(purok);
        }
        if (hasStatus) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.current_status = ?";
            params.add(status);
        }

        sql += " " + groupByClause + " " + orderBy;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
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

    public Map<String, Integer> getCaseStatusCounts(Connection con) {
        Map<String, Integer> statuses = new LinkedHashMap<>();
        String sql = "SELECT current_status, COUNT(*) AS total FROM Complaint_Detail " +
                "GROUP BY current_status ORDER BY total DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                statuses.put(rs.getString("current_status"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving case status counts");
            e.printStackTrace();
        }
        return statuses;
    }

    public Map<String, Integer> getReportSourceByRole(Connection con) {
        Map<String, Integer> sources = new LinkedHashMap<>();
        String sql = "SELECT cr.role, COUNT(*) AS total FROM Complaint c " +
                "INNER JOIN Credential cr ON c.UI_ID = cr.UI_ID " +
                "GROUP BY cr.role ORDER BY total DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sources.put(rs.getString("role"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving report source by role");
            e.printStackTrace();
        }
        return sources;
    }

    public int countTotalReportWithFilters(Connection con, String start, String end,
            String category, String purok, String status) {
        boolean hasDates = start != null && !start.isEmpty() && end != null && !end.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasPurok = purok != null && !purok.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();

        String sql = "SELECT COUNT(DISTINCT c.C_ID) AS Total " +
                "FROM Complaint c " +
                "INNER JOIN Complaint_Detail cd ON c.CD_ID = cd.CD_ID " +
                "LEFT JOIN (SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated " +
                "FROM Complaint_History ch " +
                "INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID " +
                "GROUP BY ch.CD_ID) latest_chd ON latest_chd.CD_ID = cd.CD_ID";

        List<String> params = new ArrayList<>();

        if (hasDates) {
            sql += " WHERE (CASE WHEN cd.current_status = 'Pending' THEN cd.date_time_created ELSE latest_chd.latest_dateTimeUpdated END) BETWEEN ? AND ?";
            params.add(start);
            params.add(end);
        }
        if (hasCategory) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.type = ?";
            params.add(category);
        }
        if (hasPurok) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.purok = ?";
            params.add(purok);
        }
        if (hasStatus) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.current_status = ?";
            params.add(status);
        }

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in countTotalReportWithFilters");
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReportByStatusWithFilters(Connection con, String statusFilter,
            String start, String end, String category, String purok, String status) {
        boolean hasDates = start != null && !start.isEmpty() && end != null && !end.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasPurok = purok != null && !purok.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();

        String sql = "SELECT COUNT(DISTINCT c.C_ID) AS Total " +
                "FROM Complaint c " +
                "INNER JOIN Complaint_Detail cd ON c.CD_ID = cd.CD_ID " +
                "LEFT JOIN (SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated " +
                "FROM Complaint_History ch " +
                "INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID " +
                "GROUP BY ch.CD_ID) latest_chd ON latest_chd.CD_ID = cd.CD_ID " +
                "WHERE cd.current_status = ?";

        List<String> params = new ArrayList<>();
        params.add(statusFilter);

        if (hasDates) {
            sql += " AND (CASE WHEN cd.current_status = 'Pending' THEN cd.date_time_created ELSE latest_chd.latest_dateTimeUpdated END) BETWEEN ? AND ?";
            params.add(start);
            params.add(end);
        }
        if (hasCategory) {
            sql += " AND cd.type = ?";
            params.add(category);
        }
        if (hasPurok) {
            sql += " AND cd.purok = ?";
            params.add(purok);
        }
        if (hasStatus) {
            sql += " AND cd.current_status = ?";
            params.add(status);
        }

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in countTotalReportByStatusWithFilters");
            e.printStackTrace();
        }
        return -1;
    }

    public Map<String, Integer> getCaseStatusCounts(Connection con, String start, String end,
            String category, String purok, String status) {
        Map<String, Integer> statuses = new LinkedHashMap<>();
        boolean hasDates = start != null && !start.isEmpty() && end != null && !end.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasPurok = purok != null && !purok.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();

        String sql = "SELECT cd.current_status, COUNT(DISTINCT c.C_ID) AS total " +
                "FROM Complaint_Detail cd " +
                "INNER JOIN Complaint c ON cd.CD_ID = c.CD_ID " +
                "LEFT JOIN (SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated " +
                "FROM Complaint_History ch " +
                "INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID " +
                "GROUP BY ch.CD_ID) latest_chd ON latest_chd.CD_ID = cd.CD_ID";

        List<String> params = new ArrayList<>();

        if (hasDates) {
            sql += " WHERE (CASE WHEN cd.current_status = 'Pending' THEN cd.date_time_created ELSE latest_chd.latest_dateTimeUpdated END) BETWEEN ? AND ?";
            params.add(start);
            params.add(end);
        }
        if (hasCategory) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.type = ?";
            params.add(category);
        }
        if (hasPurok) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.purok = ?";
            params.add(purok);
        }
        if (hasStatus) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.current_status = ?";
            params.add(status);
        }

        sql += " GROUP BY cd.current_status ORDER BY total DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statuses.put(rs.getString("current_status"), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getCaseStatusCounts (filtered)");
            e.printStackTrace();
        }
        return statuses;
    }

    public Map<String, Integer> getReportSourceByRole(Connection con, String start, String end,
            String category, String purok, String status) {
        Map<String, Integer> sources = new LinkedHashMap<>();
        boolean hasDates = start != null && !start.isEmpty() && end != null && !end.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasPurok = purok != null && !purok.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();

        String sql = "SELECT cr.role, COUNT(DISTINCT c.C_ID) AS total " +
                "FROM Complaint_Detail cd " +
                "INNER JOIN Complaint c ON cd.CD_ID = c.CD_ID " +
                "INNER JOIN Credential cr ON c.UI_ID = cr.UI_ID " +
                "LEFT JOIN (SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated " +
                "FROM Complaint_History ch " +
                "INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID " +
                "GROUP BY ch.CD_ID) latest_chd ON latest_chd.CD_ID = cd.CD_ID";

        List<String> params = new ArrayList<>();

        if (hasDates) {
            sql += " WHERE (CASE WHEN cd.current_status = 'Pending' THEN cd.date_time_created ELSE latest_chd.latest_dateTimeUpdated END) BETWEEN ? AND ?";
            params.add(start);
            params.add(end);
        }
        if (hasCategory) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.type = ?";
            params.add(category);
        }
        if (hasPurok) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.purok = ?";
            params.add(purok);
        }
        if (hasStatus) {
            sql += (params.isEmpty() ? " WHERE " : " AND ") + "cd.current_status = ?";
            params.add(status);
        }

        sql += " GROUP BY cr.role ORDER BY total DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sources.put(rs.getString("role"), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getReportSourceByRole (filtered)");
            e.printStackTrace();
        }
        return sources;
    }
}