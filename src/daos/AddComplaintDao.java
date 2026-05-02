package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

/**
 * DAO for adding complaints, complaint history, and actions.
 * All methods throw SQLException on failure, making them
 * safe to use in transactions.
 */
public class AddComplaintDao {

	// ===== SQL STRINGS =====
	private String insertDetailSQL;
	private String insertHistoryDetailSQL;
	private String insertActionSQL;
	private Connection con;
	private int cdID, chdID, rows;
	private ResultSet rs;

	public AddComplaintDao() {
		// ===== INIT SQL =====
		insertDetailSQL = "INSERT INTO Complaint_Detail "
				+ "(UI_ID, current_status, subject, type, date_time, street, purok, longitude, latitude, persons_involved, details, photo_attachment) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";

		insertHistoryDetailSQL = "INSERT INTO Complaint_History_Detail "
				+ "(CD_ID, status, process, date_time_updated, updated_by) VALUES (?,?,?,?,?);";

		insertActionSQL = "INSERT INTO Complaint_Action "
				+ "(CD_ID, action_taken, recommendation, oic, resolution_date_time) VALUES (?,?,?,?,?);";
	}

	/**
	 * Adds a complaint detail record linked directly to a user.
	 * 
	 * @param con    Active DB connection
	 * @param userID User ID filing the complaint
	 * @param cd     ComplaintDetail object
	 * @return Auto-generated ComplaintDetail ID
	 * @throws SQLException if insertion fails
	 */
	public int addComplaint(Connection con, int userID, ComplaintDetail cd) throws SQLException {
		this.con = con;

		// ===== INSERT DETAIL =====
		try (PreparedStatement stmtDetail = con.prepareStatement(insertDetailSQL, Statement.RETURN_GENERATED_KEYS)) {
			stmtDetail.setInt(1, userID);
			stmtDetail.setString(2, cd.getCurrentStatus());
			stmtDetail.setString(3, cd.getSubject());
			stmtDetail.setString(4, cd.getType());
			stmtDetail.setTimestamp(5, cd.getDateTime());
			stmtDetail.setString(6, cd.getStreet());
			stmtDetail.setString(7, cd.getPurok());
			stmtDetail.setDouble(8, cd.getLongitude());
			stmtDetail.setDouble(9, cd.getLatitude());
			stmtDetail.setString(10, cd.getPersonsInvolved());
			stmtDetail.setString(11, cd.getDetails());
			stmtDetail.setBytes(12, cd.getPhotoAttachmentBytes());

			rows = stmtDetail.executeUpdate();
			if (rows == 0)
				throw new SQLException("Failed to insert Complaint_Detail");

			// ===== GET GENERATED ID =====
			try (ResultSet rs = stmtDetail.getGeneratedKeys()) {
				if (rs.next()) {
					cdID = rs.getInt(1);
					return cdID;
				} else {
					throw new SQLException("Failed to retrieve generated ComplaintDetail ID");
				}
			}
		}
	}

	/**
	 * Adds complaint history directly linked to a complaint.
	 * 
	 * @param con         Active DB connection
	 * @param complaintID Complaint Detail ID to link
	 * @param chd         ComplaintHistoryDetail object
	 * @return Auto-generated ComplaintHistoryDetail ID
	 * @throws SQLException if insertion fails
	 */
	public int addComplaintHistory(Connection con, int complaintID, ComplaintHistoryDetail chd) throws SQLException {
		this.con = con;

		// ===== INSERT HISTORY DETAIL =====
		try (PreparedStatement stmtHistoryDetail = con.prepareStatement(insertHistoryDetailSQL,
				Statement.RETURN_GENERATED_KEYS)) {
			stmtHistoryDetail.setInt(1, complaintID);
			stmtHistoryDetail.setString(2, chd.getStatus());
			stmtHistoryDetail.setString(3, chd.getProcess());
			stmtHistoryDetail.setTimestamp(4, chd.getDateTimeUpdated());
			stmtHistoryDetail.setInt(5, chd.getUpdatedBy());

			rows = stmtHistoryDetail.executeUpdate();
			if (rows == 0)
				throw new SQLException("Failed to insert Complaint_History_Detail");

			// ===== GET GENERATED ID =====
			try (ResultSet rs = stmtHistoryDetail.getGeneratedKeys()) {
				if (rs.next()) {
					chdID = rs.getInt(1);
					return chdID;
				} else {
					throw new SQLException("Failed to retrieve generated ComplaintHistoryDetail ID");
				}
			}
		}
	}

	/**
	 * Adds an action related to a complaint.
	 * 
	 * @param con         Active DB connection
	 * @param complaintID Complaint Detail ID
	 * @param ca          ComplaintAction object
	 * @return true if insertion succeeds
	 * @throws SQLException if insertion fails
	 */
	public boolean addComplaintAction(Connection con, int complaintID, ComplaintAction ca) throws SQLException {
		this.con = con;

		// ===== INSERT ACTION =====
		try (PreparedStatement stmtAction = con.prepareStatement(insertActionSQL, Statement.RETURN_GENERATED_KEYS)) {
			stmtAction.setInt(1, complaintID);
			stmtAction.setString(2, ca.getActionTaken());
			stmtAction.setString(3, ca.getRecommendation());
			stmtAction.setString(4, ca.getOIC());
			stmtAction.setTimestamp(5, ca.getResolutionDateTime());

			rows = stmtAction.executeUpdate();
			if (rows == 0)
				throw new SQLException("Failed to insert Complaint_Action");

			return true;
		}
	}
}