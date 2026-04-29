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

    // Update the current status on Complaint_Detail
    private static final String UPDATE_STATUS_SQL = "UPDATE Complaint_Detail SET current_status = ? WHERE CD_ID = ?";

    // Insert a new history detail record
    private static final String INSERT_HISTORY_DETAIL_SQL = "INSERT INTO Complaint_History_Detail (status, process, date_time_updated, updated_by) "
            +
            "VALUES (?, ?, ?, ?)";

    // Link the history detail to the complaint
    private static final String INSERT_HISTORY_SQL = "INSERT INTO Complaint_History (CD_ID, CHD_ID) VALUES (?, ?)";

    // Fetch all history records for a complaint
    private static final String SELECT_HISTORY_SQL = """
            SELECT chd.CHD_ID, chd.status, chd.process, chd.date_time_updated, chd.updated_by
            FROM Complaint_History_Detail chd
            INNER JOIN Complaint_History ch ON ch.CHD_ID = chd.CHD_ID
            WHERE ch.CD_ID = ?
            ORDER BY chd.date_time_updated DESC
            """;

    // Fetch current status of a complaint
    private static final String SELECT_CURRENT_STATUS_SQL = "SELECT current_status FROM Complaint_Detail WHERE CD_ID = ?";

    /**
     * Atomically updates complaint status and records history.
     * Must be called inside a transaction (Connection with autoCommit=false).
     *
     * @param con       Active DB connection (transaction in progress)
     * @param cdId      Complaint Detail ID
     * @param newStatus New status value
     * @param process   Notes about what was done
     * @param updatedBy Name/ID of the staff making the update
     * @return true if successful
     * @throws SQLException if any step fails — caller should rollback
     */
    public boolean updateStatus(Connection con, int cdId, String newStatus,
            String process, String updatedBy) throws SQLException {

        // 1. Update Complaint_Detail status
        try (PreparedStatement stmt = con.prepareStatement(UPDATE_STATUS_SQL)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, cdId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No complaint found with CD_ID: " + cdId);
            }
        }

        // 2. Insert Complaint_History_Detail
        int chdId;
        try (PreparedStatement stmt = con.prepareStatement(
                INSERT_HISTORY_DETAIL_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, process != null ? process : "");
            stmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setString(4, updatedBy);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to insert Complaint_History_Detail");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    chdId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated CHD_ID");
                }
            }
        }

        // 3. Link to Complaint_History
        try (PreparedStatement stmt = con.prepareStatement(INSERT_HISTORY_SQL)) {
            stmt.setInt(1, cdId);
            stmt.setInt(2, chdId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to insert Complaint_History link");
            }
        }

        return true;
    }

    /**
     * Retrieves the current status of a complaint.
     */
    public String getCurrentStatus(Connection con, int cdId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(SELECT_CURRENT_STATUS_SQL)) {
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

        try (PreparedStatement stmt = con.prepareStatement(SELECT_HISTORY_SQL)) {
            stmt.setInt(1, cdId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
                    chd.setStatus(rs.getString("status"));
                    chd.setProcess(rs.getString("process"));
                    chd.setDateTimeUpdated(rs.getTimestamp("date_time_updated"));
                    chd.setUpdatedBy(rs.getString("updated_by"));
                    history.add(chd);
                }
            }
        }
        return history;
    }
}