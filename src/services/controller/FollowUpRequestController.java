package services.controller;

import daos.FollowUpRequestDao;
import models.FollowUpRequest;
import config.database.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Controller for follow-up request operations.
 */
public class FollowUpRequestController {

    private final FollowUpRequestDao dao;

    public FollowUpRequestController() {
        this.dao = new FollowUpRequestDao();
    }

    public boolean requestFollowUp(int cdId, int uiId, String notes) {
        FollowUpRequest req = new FollowUpRequest();
        req.setCdId(cdId);
        req.setUiId(uiId);
        req.setStatus("Pending");
        req.setNotes(notes != null && !notes.isBlank() ? notes : null);

        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            boolean result = dao.insert(con, req);
            con.commit();
            return result;
        } catch (SQLException e) {
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
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasActiveFollowUp(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return dao.hasActiveRequest(con, cdId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public FollowUpRequest getLatestFollowUp(int cdId) {
        try (Connection con = DBConnection.connect()) {
            return dao.findLatestByComplaintId(con, cdId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean acknowledgeFollowUp(int furId) {
        return updateStatus(furId, "Acknowledged");
    }

    public boolean resolveFollowUp(int furId) {
        return updateStatus(furId, "Resolved");
    }

    private boolean updateStatus(int furId, String status) {
        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            boolean result = dao.updateStatus(con, furId, status);
            con.commit();
            return result;
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