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
    private String queryUserReportCount, queryAllReportCount, queryStatusCount, queryAllStatusCount,
            queryReportCountRole, queryReportCountByDate;

    public ReportStatisticsDao() {
        queryUserReportCount = """
                SELECT COUNT(C_ID) AS Total
                FROM Complaint
                WHERE UI_ID = ?;
                """;

        queryAllReportCount = """
                SELECT COUNT(C_ID) AS Total
                FROM Complaint;
                """;

        queryStatusCount = """
                SELECT COUNT(*) AS Total
                FROM Complaint_Detail cd INNER JOIN Complaint c
                ON cd.cd_id = c.cd_id
                WHERE c.ui_id = ? AND cd.current_status = ?;
                """;
        queryAllStatusCount = """
                SELECT COUNT(*) AS Total
                FROM Complaint
                WHERE status = ?
                """;
        queryReportCountRole = """
                SELECT u.role, COUNT(*) AS Total
                FROM Complaint c
                INNER JOIN User u ON c.user_id = u.id
                GROUP BY u.role
                ORDER BY u.role;
                """;

        queryReportCountByDate = """
                SELECT COUNT(*) AS Total
                FROM Complaint
                WHERE created_at BETWEEN ? AND ?""";
    }

    public int countTotalReportByUser(Connection con, int UI_ID) {
        // ===== GET USER REPORT COUNT =====
        try (PreparedStatement stmt = con.prepareStatement(queryUserReportCount)) {

            stmt.setInt(1, UI_ID);

            // ===== EXECUTE QUERY =====
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
        // ===== GET TOTAL REPORT COUNT =====
        try (PreparedStatement stmt = con.prepareStatement(queryAllReportCount)) {

            // ===== EXECUTE QUERY =====
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving report count");
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
            System.err.println("Error retrieving report count");
            e.printStackTrace();
        }

        return -1;
    }

    public int countTotalReportByUserAndStatus(Connection con, int UI_ID, String status) {
        // ===== GET STATUS COUNT =====
        try (PreparedStatement stmt = con.prepareStatement(queryStatusCount)) {

            stmt.setInt(1, UI_ID);
            stmt.setString(2, status);

            // ===== EXECUTE QUERY =====
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

    public List<Object[]> getTotalTrends(Connection con, String groupBy, String start, String end) {

        List<Object[]> list = new ArrayList<>();

        String groupPart = switch (groupBy) {
            case "YEAR" -> "YEAR(created_at)";
            case "MONTH" -> "MONTH(created_at)";
            case "DAY" -> "DAY(created_at)";
            default -> "DATE(created_at)";
        };

        String sql = "SELECT " + groupPart + " AS label, type, COUNT(*) AS total " +
                "FROM Complaint " +
                "WHERE created_at BETWEEN ? AND ? " +
                "GROUP BY label, type " +
                "ORDER BY label, type";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Object[] {
                        rs.getString("label"),
                        rs.getString("type"),
                        rs.getInt("total")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Map<String, Integer> countTotalReportByRole(Connection con) {

        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = queryReportCountRole;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                map.put(
                        rs.getString("role"),
                        rs.getInt("Total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
