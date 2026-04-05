package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

/**
 * 
 * All method in this class
 * ComplaintDetail mapResultSetToComplaintDetail(ResultSet rs)
 * ComplaintDetail getComplaint(int UI_ID, int CD_ID)
 * List<ComplaintDetail> getAllComplaint(int UI_ID)
 * ComplaintHistoryDetail mapResultSetToComplaintHistoryDetail(ResultSet rs)
 * List<ComplaintHistoryDetail> getComplaintHistory(int CD_ID, int CHD_ID)
 * ComplaintAction getComplaintAction(int CD_ID)
 * 
 */

public class GetComplaintDAO {

    /**
     * This method is used to automatically sets the value of each passed object
     * mapResultSetToComplaintDetail();
     * 
     * @params ResultSet rs data
     * @return ComplaintDetail object, SQLException if error
     */

    private ComplaintDetail mapResultSetToComplaintDetail(ResultSet rs) throws SQLException {
        ComplaintDetail cd = new ComplaintDetail();
        cd.setCurrentStatus(rs.getString("current_status"));
        cd.setSubject(rs.getString("subject"));
        cd.setType(rs.getString("type"));
        cd.setDateTime(rs.getTimestamp("date_time"));
        cd.setStreet(rs.getString("street"));
        cd.setPurok(rs.getString("purok"));
        cd.setLongitude(rs.getDouble("longitude"));
        cd.setLatitude(rs.getDouble("latitude"));
        cd.setPersonsInvolved(rs.getString("persons_involved"));
        cd.setDetails(rs.getString("details"));
        cd.setPhotoAttachment(rs.getString("photo_attachment"));
        return cd;
    }

    /**
     * This method is used to get the Complaint Detail object from database with a
     * specified Complaint ID and User Id
     * getComplaint();
     * 
     * @params int UI_ID, int CD_ID data
     * @return ComplaintDetail object, null if error
     */

    public ComplaintDetail getComplaint(int UI_ID, int CD_ID) {
        String query = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection con = null;

        try {
            con = DBConnection.connect();
            query = "SELECT cd.* FROM Complaint c INNER JOIN Complaint_Detail cd ON cd.CD_ID = c.CD_ID WHERE c.UI_ID = ? AND cd.CD_ID = ?";
            statement = con.prepareStatement(query);

            statement.setInt(1, UI_ID);
            statement.setInt(2, CD_ID);

            rs = statement.executeQuery();

            if (rs.next()) {
                return mapResultSetToComplaintDetail(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error on getting complaint details on Complaint_Detail Table");
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (rs != null)
                    rs.close();
                if (con != null)
                    con.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * This method is used to get the Complaint Detail object list from database
     * with a
     * specified User Id
     * getAllComplaint();
     * 
     * @params int UI_ID data
     * @return List<ComplaintDetail> object, null if error
     */

    public List<ComplaintDetail> getAllComplaint(int UI_ID) {
        List<ComplaintDetail> cdList = new ArrayList<>();
        String query = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection con = null;

        try {
            con = DBConnection.connect();
            query = "SELECT cd.* FROM Complaint_Detail cd INNER JOIN Complaint c ON c.CD_ID = cd.CD_ID WHERE c.UI_ID = ? ";
            statement = con.prepareStatement(query);

            statement.setInt(1, UI_ID);

            rs = statement.executeQuery();

            while (rs.next()) {
                cdList.add(mapResultSetToComplaintDetail(rs));
            }

            return cdList;

        } catch (SQLException e) {
            System.out.println("Error on getting complaint details on Complaint_Detail Table");
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (rs != null)
                    rs.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * This method is used to automatically sets the value of each passed object
     * mapResultSetToComplaintHistoryDetail();
     * 
     * @params ResultSet rs data
     * @return ComplaintHistoryDetail object, SQLException if error
     */

    private ComplaintHistoryDetail mapResultSetToComplaintHistoryDetail(ResultSet rs) throws SQLException {
        ComplaintHistoryDetail chd = new ComplaintHistoryDetail();

        chd.setStatus(rs.getString("status"));
        chd.setProcess(rs.getString("process"));
        chd.setDateTimeUpdated(rs.getTimestamp("date_time_updated"));
        chd.setUpdatedBy(rs.getString("updated_by"));

        return chd;
    }

    /**
     * This method is used to get the Complaint History Detail object from database
     * with a
     * specified Complaint ID and Complaint History ID
     * getHistoryDetail();
     * 
     * @params int CD_ID, int CHD_ID data
     * @return ComplaintHistoryDetail object, null if error
     */

    public List<ComplaintHistoryDetail> getComplaintHistory(int CD_ID) {
        List<ComplaintHistoryDetail> chdList = new ArrayList<>();
        String query = null;
        PreparedStatement statement = null;
        Connection con = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            query = "SELECT chd.* FROM Complaint_History_Detail chd INNER JOIN Complaint_History ch ON ch.CHD_ID = chd.CHD_ID WHERE ch.CD_ID = ?";
            statement = con.prepareStatement(query);

            statement.setInt(1, CD_ID);

            rs = statement.executeQuery();

            while (rs.next()) {
                chdList.add(mapResultSetToComplaintHistoryDetail(rs));
            }
            return chdList;

        } catch (SQLException e) {
            System.out.println("Error on getting complaint history on Complaint_History Table");
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (rs != null)
                    rs.close();
                if (con != null)
                    con.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * This method is used to get the Complaint Action object from database with a
     * specified Complaint ID
     * getComplaintAction();
     * 
     * @params int CD_ID data
     * @return ComplaintAction object, null if error
     */

    public ComplaintAction getComplaintAction(int CD_ID) {
        ComplaintAction ca = new ComplaintAction();
        String query = null;
        PreparedStatement statement = null;
        Connection con = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            query = "SELECT ca.* FROM Complaint_Action ca WHERE ca.CD_ID = ?";

            statement = con.prepareStatement(query);
            statement.setInt(1, CD_ID);

            rs = statement.executeQuery();
            if (rs.next()) {
                ca.setActionTaken(rs.getString("action_taken"));
                ca.setRecommendation(rs.getString("recommendation"));
                ca.setOIC(rs.getString("oic"));
                ca.setDateTimeAssigned(rs.getTimestamp("date_time_assigned"));
                ca.setResolutionDateTime(rs.getTimestamp("resolution_date_time"));
                return ca;
            }

        } catch (SQLException e) {
            System.out.println("Error on getting complaint action on Complaint_Action Table");
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (rs != null)
                    rs.close();
                if (con != null)
                    con.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
