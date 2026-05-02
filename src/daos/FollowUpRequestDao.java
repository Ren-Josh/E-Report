package daos;

import models.FollowUpRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Follow_Up_Request table operations.
 */
public class FollowUpRequestDao {

    // ===== SQL STRINGS =====
    private String insertSQL;
    private String findByComplaintIdSQL;
    private String findLatestSQL;
    private String hasActiveSQL;
    private String updateStatusSQL;

    public FollowUpRequestDao() {
        insertSQL = "INSERT INTO Follow_Up_Request (CD_ID, UI_ID, status, notes) VALUES (?, ?, ?, ?)";
        findByComplaintIdSQL = "SELECT * FROM Follow_Up_Request WHERE CD_ID = ? ORDER BY request_date DESC";
        findLatestSQL = "SELECT * FROM Follow_Up_Request WHERE CD_ID = ? ORDER BY request_date DESC LIMIT 1";
        hasActiveSQL = "SELECT COUNT(*) FROM Follow_Up_Request WHERE CD_ID = ? AND status = 'Pending'";
        updateStatusSQL = "UPDATE Follow_Up_Request SET status = ? WHERE FUR_ID = ?";
    }

    /**
     * Inserts a new follow-up request.
     */
    public boolean insert(Connection con, FollowUpRequest req) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getCdId());
            ps.setInt(2, req.getUiId());
            ps.setString(3, req.getStatus());
            ps.setString(4, req.getNotes());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds all follow-up requests for a complaint.
     */
    public List<FollowUpRequest> findByComplaintId(Connection con, int cdId) throws SQLException {
        List<FollowUpRequest> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(findByComplaintIdSQL)) {
            ps.setInt(1, cdId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds the latest follow-up request for a complaint.
     */
    public FollowUpRequest findLatestByComplaintId(Connection con, int cdId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(findLatestSQL)) {
            ps.setInt(1, cdId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Checks if there is an active (Pending) request.
     */
    public boolean hasActiveRequest(Connection con, int cdId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(hasActiveSQL)) {
            ps.setInt(1, cdId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Updates the status of a follow-up request.
     */
    public boolean updateStatus(Connection con, int furId, String status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(updateStatusSQL)) {
            ps.setString(1, status);
            ps.setInt(2, furId);
            return ps.executeUpdate() > 0;
        }
    }

    private FollowUpRequest mapRow(ResultSet rs) throws SQLException {
        FollowUpRequest req = new FollowUpRequest();
        req.setFurId(rs.getInt("FUR_ID"));
        req.setCdId(rs.getInt("CD_ID"));
        req.setUiId(rs.getInt("UI_ID"));
        req.setRequestDate(rs.getTimestamp("request_date"));
        req.setStatus(rs.getString("status"));
        req.setNotes(rs.getString("notes"));
        return req;
    }
}