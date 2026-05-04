package services.controller;

import daos.FollowUpRequestDao;
import models.FollowUpRequest;
import config.database.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Controller for follow-up request operations.
 * Manages transaction boundaries and business logic for creating,
 * querying, and updating follow-up requests.
 */
public class FollowUpRequestController {

    /** Data access object for follow-up request database operations. */
    private final FollowUpRequestDao dao;

    /** Initializes the controller and creates the DAO instance. */
    public FollowUpRequestController() {
        this.dao = new FollowUpRequestDao();
    }

    /**
     * Creates a new follow-up request for a complaint.
     * Wraps the insert in a manual transaction to ensure atomicity.
     * 
     * @param cdId  the complaint detail ID being followed up
     * @param uiId  the user ID requesting the follow-up
     * @param notes optional additional notes; blank strings are stored as null
     * @return true if the request was committed successfully; false otherwise
     */
    public boolean requestFollowUp(int cdId, int uiId, String notes) {
        // Build the domain object with default "Pending" status.
        FollowUpRequest req = new FollowUpRequest();
        req.setCdId(cdId);
        req.setUiId(uiId);
        req.setStatus("Pending");
        // Store null instead of empty strings to keep the database clean.
        req.setNotes(notes != null && !notes.isBlank() ? notes : null);

        Connection con = null;
        try {
            // Open a connection and manage the transaction manually.
            con = DBConnection.connect();
            con.setAutoCommit(false);

            boolean result = dao.insert(con, req);
            con.commit();
            return result;
        } catch (SQLException e) {
            // Roll back on any SQL failure to avoid partial inserts.
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            System.err.println("Follow-up request failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Always close the connection to prevent connection pool exhaustion.
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
     * Checks whether a complaint currently has an active (non-resolved) follow-up
     * request.
     * 
     * @param cdId the complaint detail ID to check
     * @return true if an active request exists; false if none exists or on error
     */
    public boolean hasActiveFollowUp(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return dao.hasActiveRequest(con, cdId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the most recent follow-up request for a given complaint.
     * 
     * @param cdId the complaint detail ID
     * @return the latest FollowUpRequest, or null if none exists or on error
     */
    public FollowUpRequest getLatestFollowUp(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return dao.findLatestByComplaintId(con, cdId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Marks a follow-up request as acknowledged by staff.
     * 
     * @param furId the follow-up request ID
     * @return true if the status update was committed successfully
     */
    public boolean acknowledgeFollowUp(int furId) {
        return updateStatus(furId, "Acknowledged");
    }

    /**
     * Marks a follow-up request as resolved/completed.
     * 
     * @param furId the follow-up request ID
     * @return true if the status update was committed successfully
     */
    public boolean resolveFollowUp(int furId) {
        return updateStatus(furId, "Resolved");
    }

    /**
     * Shared helper that updates the status of a follow-up request.
     * Wraps the DAO call in a manual transaction for consistency.
     * 
     * @param furId  the follow-up request ID to update
     * @param status the new status string to set
     * @return true if the update was committed successfully; false otherwise
     */
    private boolean updateStatus(int furId, String status) {
        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            boolean result = dao.updateStatus(con, furId, status);
            con.commit();
            return result;
        } catch (SQLException e) {
            // Roll back on failure to prevent inconsistent state.
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
            // Ensure the connection is always closed.
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