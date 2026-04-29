package services.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import config.database.DBConnection;
import daos.UpdateComplaintStatusDao;
import models.ComplaintHistoryDetail;
import models.UserSession;

/**
 * Service controller for complaint status operations.
 * Handles transaction management and business logic.
 */
public class ComplaintStatusController {

    private final UpdateComplaintStatusDao statusDao;

    // Valid status values in the system
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED = "Resolved";
    public static final String STATUS_REJECTED = "Rejected";

    public ComplaintStatusController() {
        this.statusDao = new UpdateComplaintStatusDao();
    }

    /**
     * Updates the status of a complaint with full transaction safety.
     *
     * @param cdId      Complaint Detail ID
     * @param newStatus New status to set
     * @param process   Notes about the action taken
     * @param session   Current user session (for updated_by)
     * @return true if successful
     */
    public boolean updateComplaintStatus(int cdId, String newStatus,
            String process, UserSession session) {

        String updatedBy = buildUpdatedByString(session);

        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            // Validate status transition
            String currentStatus = statusDao.getCurrentStatus(con, cdId);
            if (currentStatus == null) {
                throw new IllegalStateException("Complaint not found: " + cdId);
            }

            if (!isValidTransition(currentStatus, newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid status transition: " + currentStatus + " -> " + newStatus);
            }

            // Perform update
            boolean success = statusDao.updateStatus(con, cdId, newStatus, process, updatedBy);

            if (success) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }

        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            System.err.println("Status update failed: " + e.getMessage());
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

    /**
     * Retrieves the status history for a complaint.
     *
     * @param cdId Complaint Detail ID
     * @return List of history records, or empty list if none
     */
    public List<ComplaintHistoryDetail> getStatusHistory(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return statusDao.getHistory(con, cdId);
        } catch (SQLException e) {
            System.err.println("Failed to fetch history for CD_ID: " + cdId);
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Gets the current status of a complaint.
     */
    public String getCurrentStatus(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return statusDao.getCurrentStatus(con, cdId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns valid next statuses based on current status.
     */
    public static List<String> getValidNextStatuses(String currentStatus) {
        return switch (currentStatus) {
            case STATUS_PENDING -> List.of(STATUS_IN_PROGRESS, STATUS_REJECTED);
            case STATUS_IN_PROGRESS -> List.of(STATUS_RESOLVED, STATUS_REJECTED);
            case STATUS_RESOLVED -> List.of(STATUS_IN_PROGRESS); // Re-open
            case STATUS_REJECTED -> List.of(STATUS_IN_PROGRESS); // Re-open
            default -> List.of();
        };
    }

    /**
     * Checks if a status transition is valid.
     */
    public static boolean isValidTransition(String from, String to) {
        return getValidNextStatuses(from).contains(to);
    }

    /**
     * Builds the updated_by string from session info.
     */
    private String buildUpdatedByString(UserSession session) {
        if (session == null)
            return "System";
        return session.getRole() + " (ID:" + session.getUserId() + ")";
    }
}