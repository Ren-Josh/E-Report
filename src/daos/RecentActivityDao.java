package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for retrieving recent activity feeds.
 */
public class RecentActivityDao {

    // ===== SQL STRINGS =====
    private String queryRecentStatusUpdates;

    public RecentActivityDao() {
        queryRecentStatusUpdates = """
                SELECT
                    cd.CD_ID AS complaint_id,
                    cd.current_status,
                    chd.date_time_updated,
                    cr.role AS updated_by_role,
                    CONCAT(ui.first_name, ' ', ui.last_name) AS user_name,
                    ui.UI_ID AS user_id
                FROM Complaint_History_Detail chd
                JOIN Complaint_Detail cd ON chd.CD_ID = cd.CD_ID
                JOIN User_Info ui ON chd.updated_by = ui.UI_ID
                JOIN Credential cr ON ui.UI_ID = cr.UI_ID
                WHERE chd.status = cd.current_status
                  AND chd.date_time_updated >= ?
                ORDER BY chd.date_time_updated DESC
                LIMIT ?
                """;
    }

    /**
     * Retrieves recent status updates that match the current complaint status.
     *
     * @param con    Active DB connection
     * @param limit  Maximum number of records
     * @param cutoff Timestamp threshold
     * @return List of activity rows
     */
    public List<Object[]> getRecentStatusUpdates(Connection con, int limit, java.sql.Timestamp cutoff) {
        List<Object[]> results = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(queryRecentStatusUpdates)) {
            stmt.setTimestamp(1, cutoff);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[6];
                    row[0] = rs.getInt("complaint_id");
                    row[1] = rs.getString("current_status");
                    row[2] = rs.getTimestamp("date_time_updated");
                    row[3] = rs.getString("updated_by_role");
                    row[4] = rs.getString("user_name");
                    row[5] = rs.getInt("user_id");
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recent activities");
            e.printStackTrace();
        }
        return results;
    }
}