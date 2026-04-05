
package DAOs;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

/**
 * Adding logic shall be the following:
 * addComplaint() -> addComplaintHistory() -> addComplaintAction
 * 
 * All methods in this Class
 * addComplaint(Connection con, int user_ID, ComplaintDetail cd);
 * addComplaintHistory(Connection con, int complaint_ID, ComplaintHistoryDetail
 * chd)
 * addComplaintAction(Connection con, int complaint_ID, ComplaintAction ca)
 * 
 */

public class AddComplaintDAO {

    /**
     * This method is used to add complaint on complaint details table
     * addComplaint();
     * 
     * @params DB Connection, ComplaintDetail data
     * @return none
     */

    public static void addComplaint(Connection con, int user_ID, ComplaintDetail cd) {
        String query1, query2;
        int rows, cdID;
        ResultSet rs;
        PreparedStatement statement1 = null, statement2 = null;

        try {
            query1 = "INSERT INTO Complaint_Detail (current_status, subject, type, date_time, street, purok, longitude, latitude, persons_involved, details, photo_attachment) VALUES (?,?,?,?,?,?,?,?,?,?,?);";
            statement1 = con.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
            statement1.setString(1, cd.getCurrentStatus());
            statement1.setString(2, cd.getSubject());
            statement1.setString(3, cd.getType());
            statement1.setTimestamp(4, cd.getDateTime());
            statement1.setString(5, cd.getStreet());
            statement1.setString(6, cd.getPurok());
            statement1.setDouble(7, cd.getLongitude());
            statement1.setDouble(8, cd.getLatitude());
            statement1.setString(9, cd.getPersonsInvolved());
            statement1.setString(10, cd.getDetails());
            statement1.setString(11, cd.getPhotoAttachment());

            rows = statement1.executeUpdate();
            System.out.println(rows + " rows(s) inserted on Complaint_Detail");

            rs = statement1.getGeneratedKeys();
            if (rs.next()) {
                try {
                    cdID = rs.getInt(1);

                    query2 = "INSERT INTO Complaint(CD_ID, UI_ID) VALUES(?,?);";
                    statement2 = con.prepareStatement(query2);
                    statement2.setInt(1, cdID);
                    statement2.setInt(2, user_ID);

                    rows = statement2.executeUpdate();
                    System.out.println(rows + " rows(s) inserted on Complaint");
                } catch (SQLException e) {
                    System.out.println("Insertion of Complaint failed ");
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.out.println("Insertion of Complaint_Detail failed ");
            e.printStackTrace();
        } finally {
            try {
                if (statement1 != null)
                    statement1.close();
                if (statement2 != null)
                    statement2.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is used to update the status of a complaint by inserting details
     * on complaint_history_detail
     * addComplaintHistory();
     * 
     * @params DB Connection, Complaint_ID, ComplaintHistoryDetail data
     * @return none
     */

    public void addComplaintHistory(Connection con, int complaint_ID, ComplaintHistoryDetail chd) {
        String query1, query2;
        int rows, chdID;
        ResultSet rs;
        PreparedStatement statement1 = null, statement2 = null;

        try {
            query1 = "INSERT INTO Complaint_History_Detail(status, process, date_time_updated, updated_by) VALUES(?,?,?,?);";
            statement1 = con.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
            statement1.setString(1, chd.getStatus());
            statement1.setString(2, chd.getProcess());
            statement1.setTimestamp(3, chd.getDateTimeUpdated());
            statement1.setString(4, chd.getUpdatedBy());

            rows = statement1.executeUpdate();
            System.out.println(rows + " rows(s) inserted on Complaint_History_Detail");

            rs = statement1.getGeneratedKeys();
            if (rs.next()) {
                try {
                    chdID = rs.getInt(1);

                    query2 = "INSERT INTO Complaint_History(CD_ID, CHD_ID) VALUES(?,?);";
                    statement2 = con.prepareStatement(query2);
                    statement2.setInt(1, complaint_ID);
                    statement2.setInt(2, chdID);

                    rows = statement2.executeUpdate();
                    System.out.println(rows + " rows(s) inserted on Complaint_History");
                } catch (SQLException e) {
                    System.out.println("Insertion of Complaint_History failed ");
                    e.printStackTrace();
                }
            }

            rs.close();

        } catch (SQLException e) {
            System.out.println("Insertion of Complaint_History_Detail failed ");
            e.printStackTrace();
        } finally {
            try {
                if (statement1 != null)
                    statement1.close();
                if (statement2 != null)
                    statement2.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is used to add the complaint action on the complaint
     * addComplaintAction();
     * 
     * @params DB Connection, Complaint_ID, ComplaintAction data
     * @return none
     */
    public void addComplaintAction(Connection con, int complaint_ID, ComplaintAction ca) {
        String query1;
        int rows;
        ResultSet rs;
        PreparedStatement statement1 = null, statement2 = null;

        try {
            query1 = "INSERT INTO Complaint_Action(CD_ID, action_taken, recommendation, oic, resolution_date_time) VALUES(?,?,?,?,?);";
            statement1 = con.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
            statement1.setInt(1, complaint_ID);
            statement1.setString(2, ca.getActionTaken());
            statement1.setString(3, ca.getRecommendation());
            statement1.setString(4, ca.getOIC());
            statement1.setTimestamp(5, ca.getResolutionDateTime());

            rows = statement1.executeUpdate();
            System.out.println(rows + " rows(s) inserted on Complaint_Action");

            rs = statement1.getGeneratedKeys();

            rs.close();
        } catch (SQLException e) {
            System.out.println("Insertion of Complaint_Action failed ");

            e.printStackTrace();
        } finally {
            try {
                if (statement1 != null)
                    statement1.close();
                if (statement2 != null)
                    statement2.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}