package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportStatisticsDao {
    private String queryUserCount, queryTotalCount, queryStatusCount;

    public ReportStatisticsDao() {
        queryUserCount = """
                SELECT COUNT(C_ID) AS Total
                FROM Complaint
                WHERE UI_ID = ?;
                """;

        queryTotalCount = """
                SELECT COUNT(C_ID) AS Total
                FROM Complaint;
                """;

        queryStatusCount = """
                SELECT COUNT(*) AS Total
                FROM Complaint_Detail cd INNER JOIN Complaint c
                ON cd.cd_id = c.cd_id
                WHERE c.ui_id = ? AND cd.current_status = ?;
                """;
    }

    public int getUserTotalReportCount(Connection con, int UI_ID) {
        // ===== GET USER REPORT COUNT =====
        try (PreparedStatement stmt = con.prepareStatement(queryUserCount)) {

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

    public int getTotalReportCount(Connection con) {
        // ===== GET TOTAL REPORT COUNT =====
        try (PreparedStatement stmt = con.prepareStatement(queryTotalCount)) {

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

    public int getUserTotalStatusCount(Connection con, int UI_ID, String status) {
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
}
