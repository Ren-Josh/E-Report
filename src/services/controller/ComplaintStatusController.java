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

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED = "Resolved";
    public static final String STATUS_REJECTED = "Rejected";

    public ComplaintStatusController() {
        this.statusDao = new UpdateComplaintStatusDao();
    }

    /**
     * Updates the status of a complaint with full transaction safety.
     * Allows same-status updates if a process note is provided.
     */
    public boolean updateComplaintStatus(int cdId, String newStatus,
            String process, UserSession session) {

        int updatedBy = (session != null) ? session.getUserId() : 0;

        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            String currentStatus = statusDao.getCurrentStatus(con, cdId);
            if (currentStatus == null) {
                throw new IllegalStateException("Complaint not found: " + cdId);
            }

            boolean isSameStatus = currentStatus.equals(newStatus);
            boolean hasNote = process != null && !process.isBlank();

            if (isSameStatus && !hasNote) {
                con.rollback();
                return false;
            }

            if (!isSameStatus && !isValidTransition(currentStatus, newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid status transition: " + currentStatus + " -> " + newStatus);
            }

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

    public List<ComplaintHistoryDetail> getStatusHistory(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return statusDao.getHistory(con, cdId);
        } catch (SQLException e) {
            System.err.println("Failed to fetch history for CD_ID: " + cdId);
            e.printStackTrace();
            return List.of();
        }
    }

    public String getCurrentStatus(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return statusDao.getCurrentStatus(con, cdId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getValidNextStatuses(String currentStatus) {
        return switch (currentStatus) {
            case STATUS_PENDING -> List.of(STATUS_IN_PROGRESS, STATUS_REJECTED);
            case STATUS_IN_PROGRESS -> List.of(STATUS_RESOLVED, STATUS_REJECTED);
            case STATUS_RESOLVED -> List.of(STATUS_IN_PROGRESS);
            case STATUS_REJECTED -> List.of(STATUS_IN_PROGRESS);
            default -> List.of();
        };
    }

    public static boolean isValidTransition(String from, String to) {
        return getValidNextStatuses(from).contains(to);
    }
}