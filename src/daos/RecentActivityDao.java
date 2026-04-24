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
                    c.C_ID,
                    chd.status,
                    chd.date_time_updated,
                    chd.updated_by
                FROM Complaint_History ch
                INNER JOIN Complaint_History_Detail chd ON ch.CHD_ID = chd.CHD_ID
                INNER JOIN Complaint c ON ch.CD_ID = c.CD_ID
                ORDER BY chd.date_time_updated DESC
                LIMIT ?
                """;
    }

    public List<Object[]> getRecentStatusUpdates(Connection con, int limit) {
        List<Object[]> list = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(queryRecentStatusUpdates)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[] {
                        rs.getInt("C_ID"),
                        rs.getString("status"),
                        rs.getTimestamp("date_time_updated"),
                        rs.getString("updated_by")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recent activities");
            e.printStackTrace();
        }
        return list;
    }
}