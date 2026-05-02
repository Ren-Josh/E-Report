package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import models.ComplaintHistoryDetail;

/**
 * DAO for updating complaint status and recording history.
 * All operations are transaction-safe when passed an active Connection.
 */
public class UpdateComplaintStatusDao {

    // ===== SQL STRINGS =====
    private String updateStatusSQL;
    private String insertHistoryDetailSQL;
    private String selectHistorySQL;
    private String selectCurrentStatusSQL;

    public UpdateComplaintStatusDao() {
        // ===== INIT SQL =====
        updateStatusSQL = "UPDATE Complaint_Detail SET current_status = ? WHERE CD_ID = ?";

        insertHistoryDetailSQL = "INSERT INTO Complaint_History_Detail "
                + "(CD_ID, status, process, date_time_updated, updated_by) VALUES (?, ?, ?, ?, ?)";

        selectHistorySQL = """
                SELECT CHD_ID, status, process, date_time_updated, updated_by
                FROM Complaint_History_Detail
                WHERE CD_ID = ?
                ORDER BY date_time_updated DESC
                """;

        selectCurrentStatusSQL = "SELECT current_status FROM Complaint_Detail WHERE CD_ID = ?";
    }

    /**
     * Atomically updates complaint status and records history.
     * Must be called inside a transaction (Connection with autoCommit=false).
     *
     * @param con       Active DB connection (transaction in progress)
     * @param cdId      Complaint Detail ID
     * @param newStatus New status value
     * @param process   Notes about what was done
     * @param updatedBy User ID (UI_ID) of the staff making the update
     * @return true if successful
     * @throws SQLException if any step fails — caller should rollback
     */
    public boolean updateStatus(Connection con, int cdId, String newStatus,
            String process, int updatedBy) throws SQLException {

        // ===== UPDATE STATUS =====
        try (PreparedStatement stmt = con.prepareStatement(updateStatusSQL)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, cdId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No complaint found with CD_ID: " + cdId);
            }
        }

        // ===== INSERT HISTORY DETAIL =====
        try (PreparedStatement stmt = con.prepareStatement(
                insertHistoryDetailSQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, cdId);
            stmt.setString(2, newStatus);
            stmt.setString(3, process != null ? process : "");
            stmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setInt(5, updatedBy);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to insert Complaint_History_Detail");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Failed to retrieve generated CHD_ID");
                }
            }
        }

        return true;
    }

    /**
     * Retrieves the current status of a complaint.
     */
    public String getCurrentStatus(Connection con, int cdId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(selectCurrentStatusSQL)) {
            stmt.setInt(1, cdId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("current_status");
                }
            }
        }
        return null;
    }

    /**
     * Retrieves full history for a complaint.
     */
    public List<ComplaintHistoryDetail> getHistory(Connection con, int cdId) throws SQLException {
        List<ComplaintHistoryDetail> history = new ArrayList<>();

        try (PreparedStatement stmt = con.prepareStatement(selectHistorySQL)) {
            stmt.setInt(1, cdId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
                    chd.setStatus(rs.getString("status"));
                    chd.setProcess(rs.getString("process"));
                    chd.setDateTimeUpdated(rs.getTimestamp("date_time_updated"));
                    chd.setUpdatedBy(rs.getInt("updated_by"));
                    history.add(chd);
                }
            }
        }
        return history;
    }
}