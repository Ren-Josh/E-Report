package services.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import daos.AddComplaintDao;
import daos.GetComplaintDao;
import config.database.DBConnection;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.UserSession;

/**
 * ComplaintServiceController
 * 
 * Handles operations related to complaints submitted by users.
 */
public class ComplaintServiceController {

	// ===== DAO INSTANCE =====
	private AddComplaintDao addComplaintDAO;

	public ComplaintServiceController() {
		// ===== INIT DAO =====
		addComplaintDAO = new AddComplaintDao();
	}

	/**
	 * Adds a new complaint for a given user.
	 * 
	 * @param UI_ID       The user ID filing the complaint
	 * @param cd          The ComplaintDetail object containing complaint data
	 * @param droppedFile Optional file to attach (e.g., an image); can be null
	 */
	public void addComplaint(int UI_ID, ComplaintDetail cd, File droppedFile) {
		Connection con = null;

		try {
			// ===== CREATE CONNECTION =====
			con = DBConnection.connect();

			// ===== PROCESS IMAGE =====
			if (droppedFile != null) {
				try {
					processAndAttachImage(cd, droppedFile);
				} catch (Exception e) {
					System.err
							.println("Non-critical Error: Image failed to save, continuing with complaint submission.");
					e.printStackTrace();
				}
			}

			// ===== ADD COMPLAINT =====
			addComplaintDAO.addComplaint(con, UI_ID, cd);
			System.out.println("Complaint successfully saved!");

		} catch (SQLException e) {
			System.err.println("Database Error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// ===== CLOSE CONNECTION =====
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<ComplaintDetail> getAllComplaints() {
		GetComplaintDao gcd = new GetComplaintDao();

		try (Connection con = DBConnection.connect()) {
			return gcd.getAllComplaints(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Processes and attaches an image file to the ComplaintDetail object.
	 * 
	 * @param cd          The ComplaintDetail object to attach the image to
	 * @param droppedFile The image file to read and attach
	 */
	public void processAndAttachImage(ComplaintDetail cd, File droppedFile) {
		// ===== READ IMAGE =====
		try {
			byte[] fileBytes = Files.readAllBytes(droppedFile.toPath());
			cd.setPhotoAttachmentBytes(fileBytes);
			System.out.println("Image successfully read and attached as BLOB: " + droppedFile.getName());
		} catch (IOException e) {
			System.err.println("Failed to read the image file!");
			e.printStackTrace();
		}
	}

	public List<ComplaintDetail> getAllComplaintByUser(UserSession us) {
		GetComplaintDao gcd = new GetComplaintDao();

		try (Connection con = DBConnection.connect();) {
			return gcd.getAllComplaint(con, us.getUserId());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<ComplaintDetail> getRecentComplaintByUser(UserSession us, int limit) {
		GetComplaintDao gcd = new GetComplaintDao();

		try (Connection con = DBConnection.connect();) {
			return gcd.getRecentComplaint(con, us.getUserId(), limit);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean updateComplaintStatus(int complaintId, String newStatus, String note, String updatedBy) {
		AddComplaintDao dao = new AddComplaintDao();
		Connection con = null;
		try {
			con = DBConnection.connect();
			con.setAutoCommit(false);

			String updateStatusSql = "UPDATE Complaint_Detail SET current_status = ? WHERE CD_ID = ?";
			try (PreparedStatement stmt = con.prepareStatement(updateStatusSql)) {
				stmt.setString(1, newStatus);
				stmt.setInt(2, complaintId);
				stmt.executeUpdate();
			}

			ComplaintHistoryDetail history = new ComplaintHistoryDetail();
			history.setStatus(newStatus);
			history.setProcess(note != null ? note : "");
			history.setDateTimeUpdated(new Timestamp(System.currentTimeMillis()));
			history.setUpdatedBy(updatedBy != null ? updatedBy : "Staff");

			dao.addComplaintHistory(con, complaintId, history);
			con.commit();
			return true;
		} catch (SQLException e) {
			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException rollbackEx) {
					rollbackEx.printStackTrace();
				}
			}
			e.printStackTrace();
			return false;
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}