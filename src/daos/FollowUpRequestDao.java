package daos;

import config.database.DBConnection;
import models.FollowUpRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Follow_Up_Request table operations.
 */
public class FollowUpRequestDao {

    public boolean insert(FollowUpRequest req) {
        String sql = "INSERT INTO Follow_Up_Request (CD_ID, UI_ID, status, notes) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, req.getCdId());
            ps.setInt(2, req.getUiId());
            ps.setString(3, req.getStatus());
            ps.setString(4, req.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FollowUpRequest> findByComplaintId(int cdId) {
        List<FollowUpRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM Follow_Up_Request WHERE CD_ID = ? ORDER BY request_date DESC";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cdId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public FollowUpRequest findLatestByComplaintId(int cdId) {
        String sql = "SELECT * FROM Follow_Up_Request WHERE CD_ID = ? ORDER BY request_date DESC LIMIT 1";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cdId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasActiveRequest(int cdId) {
        String sql = "SELECT COUNT(*) FROM Follow_Up_Request WHERE CD_ID = ? AND status = 'Pending'";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cdId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(int furId, String status) {
        String sql = "UPDATE Follow_Up_Request SET status = ? WHERE FUR_ID = ?";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, furId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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