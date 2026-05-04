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

    /** Data access object for status-related database operations. */
    private final UpdateComplaintStatusDao statusDao;

    /** Status constant: complaint has been submitted but not yet acted upon. */
    public static final String STATUS_PENDING = "Pending";
    /** Status constant: complaint is currently being investigated or processed. */
    public static final String STATUS_IN_PROGRESS = "In Progress";
    /** Status constant: complaint has been successfully addressed. */
    public static final String STATUS_RESOLVED = "Resolved";
    /** Status constant: complaint has been declined or dismissed. */
    public static final String STATUS_REJECTED = "Rejected";

    /** Initializes the controller and creates the DAO instance. */
    public ComplaintStatusController() {
        this.statusDao = new UpdateComplaintStatusDao();
    }

    /**
     * Updates the status of a complaint with full transaction safety.
     * Allows same-status updates if a process note is provided.
     * 
     * @param cdId      the complaint detail ID to update
     * @param newStatus the target status string
     * @param process   optional process note explaining the update
     * @param session   the current user session (for audit trail)
     * @return true if the update was committed successfully; false otherwise
     */
    public boolean updateComplaintStatus(int cdId, String newStatus,
            String process, UserSession session) {

        // Extract the user ID from the session for the history audit trail.
        int updatedBy = (session != null) ? session.getUserId() : 0;

        Connection con = null;
        try {
            // Open a connection and disable auto-commit so we can manage the transaction
            // manually.
            con = DBConnection.connect();
            con.setAutoCommit(false);

            // Verify the complaint exists before attempting any update.
            String currentStatus = statusDao.getCurrentStatus(con, cdId);
            if (currentStatus == null) {
                throw new IllegalStateException("Complaint not found: " + cdId);
            }

            boolean isSameStatus = currentStatus.equals(newStatus);
            boolean hasNote = process != null && !process.isBlank();

            // Reject no-op updates unless the user provides a process note.
            if (isSameStatus && !hasNote) {
                con.rollback();
                return false;
            }

            // Enforce the allowed status transition rules.
            if (!isSameStatus && !isValidTransition(currentStatus, newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid status transition: " + currentStatus + " -> " + newStatus);
            }

            // Execute the status update and history insertion inside the same transaction.
            boolean success = statusDao.updateStatus(con, cdId, newStatus, process, updatedBy);

            if (success) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }

        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            // On any failure, roll back the transaction to keep the database consistent.
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
            // Always close the connection to avoid connection leaks.
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
     * Retrieves the full status change history for a given complaint.
     * 
     * @param cdId the complaint detail ID
     * @return a list of history records ordered by timestamp; empty list on error
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
     * Looks up the current status of a complaint without fetching its full history.
     * 
     * @param cdId the complaint detail ID
     * @return the current status string, or null if the complaint does not exist or
     *         an error occurs
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
     * Returns the list of statuses that a complaint may transition into
     * from the given current status, based on the business workflow rules.
     * 
     * @param currentStatus the starting status
     * @return a list of valid next statuses; empty list if the starting status is
     *         unrecognized
     */
    public static List<String> getValidNextStatuses(String currentStatus) {
        return switch (currentStatus) {
            case STATUS_PENDING -> List.of(STATUS_IN_PROGRESS, STATUS_REJECTED);
            case STATUS_IN_PROGRESS -> List.of(STATUS_RESOLVED, STATUS_REJECTED);
            case STATUS_RESOLVED -> List.of(STATUS_IN_PROGRESS);
            case STATUS_REJECTED -> List.of(STATUS_IN_PROGRESS);
            default -> List.of();
        };
    }

    /**
     * Checks whether a direct transition from one status to another is allowed.
     * 
     * @param from the current status
     * @param to   the proposed new status
     * @return true if the transition is valid according to the workflow rules
     */
    public static boolean isValidTransition(String from, String to) {
        return getValidNextStatuses(from).contains(to);
    }
}