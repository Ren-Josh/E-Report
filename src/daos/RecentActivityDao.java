package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecentActivityDao {

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
                JOIN Complaint_History ch ON chd.CHD_ID = ch.CHD_ID
                JOIN Complaint_Detail cd ON ch.CD_ID = cd.CD_ID
                JOIN Complaint c ON cd.CD_ID = c.CD_ID
                JOIN User_Info ui ON c.UI_ID = ui.UI_ID
                JOIN Credential cr ON ui.UI_ID = cr.UI_ID
                WHERE chd.status = cd.current_status
                  AND chd.date_time_updated >= ?
                ORDER BY chd.date_time_updated DESC
                LIMIT ?
                """;
    }

    public List<Object[]> getRecentStatusUpdates(Connection con, int limit, java.sql.Timestamp cutoff) {
        List<Object[]> results = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(queryRecentStatusUpdates)) {
            stmt.setTimestamp(1, cutoff);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
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
        } catch (SQLException e) {
            System.err.println("Error retrieving recent activities");
            e.printStackTrace();
        }
        return results;
    }
}