package services.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import daos.AddComplaintDao;
import daos.GetComplaintDao;
import daos.UpdateComplaintStatusDao;
import config.database.DBConnection;
import models.ComplaintDetail;
import models.UserSession;

/**
 * Handles operations related to complaints submitted by users.
 */
public class ComplaintServiceController {

	// ===== DAO INSTANCES =====
	private AddComplaintDao addComplaintDAO;
	private UpdateComplaintStatusDao statusDao;

	public ComplaintServiceController() {
		addComplaintDAO = new AddComplaintDao();
		statusDao = new UpdateComplaintStatusDao();
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
			con = DBConnection.connect();
			con.setAutoCommit(false);

			if (droppedFile != null) {
				try {
					processAndAttachImage(cd, droppedFile);
				} catch (Exception e) {
					System.err
							.println("Non-critical Error: Image failed to save, continuing with complaint submission.");
					e.printStackTrace();
				}
			}

			int cdId = addComplaintDAO.addComplaint(con, UI_ID, cd);
			con.commit();
			System.out.println("Complaint successfully saved! ID: " + cdId);

		} catch (SQLException e) {
			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException rollbackEx) {
					rollbackEx.printStackTrace();
				}
			}
			System.err.println("Database Error: " + e.getMessage());
			e.printStackTrace();
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
	 */
	public void processAndAttachImage(ComplaintDetail cd, File droppedFile) {
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
		try (Connection con = DBConnection.connect()) {
			return gcd.getAllComplaint(con, us.getUserId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ComplaintDetail> getRecentComplaintByUser(UserSession us, int limit) {
		GetComplaintDao gcd = new GetComplaintDao();
		try (Connection con = DBConnection.connect()) {
			return gcd.getRecentComplaint(con, us.getUserId(), limit);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Updates complaint status and records history atomically.
	 * 
	 * @param complaintId Complaint Detail ID
	 * @param newStatus   New status value
	 * @param note        Process notes
	 * @param updatedBy   User ID (UI_ID) of the staff making the update
	 * @return true if successful
	 */
	public boolean updateComplaintStatus(int complaintId, String newStatus, String note, int updatedBy) {
		Connection con = null;
		try {
			con = DBConnection.connect();
			con.setAutoCommit(false);

			boolean success = statusDao.updateStatus(con, complaintId, newStatus, note, updatedBy);

			if (success) {
				con.commit();
				return true;
			} else {
				con.rollback();
				return false;
			}
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