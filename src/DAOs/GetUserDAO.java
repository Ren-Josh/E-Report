package DAOs;

import models.Credential;
import models.UserInfo;
import config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * 
 * All method in this class
 * 
 * getUser();
 * getCredential();
 */

public class GetUserDAO {

    /**
     * This method is used to get the UserInfo object from database with a
     * specified User Id
     * getUser();
     * 
     * @params int UI_ID data
     * @return UserInfo object, null if error
     */

    public UserInfo getUser(int UI_ID) {

        UserInfo ui = new UserInfo();
        String query = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection con = DBConnection.connect();

        try {
            query = "SELECT * FROM USER_INFO WHERE User_Info.UI_ID = ?;";
            statement = con.prepareStatement(query);

            statement.setInt(1, UI_ID);
            rs = statement.executeQuery();

            if (rs.next()) {
                ui.setUI_ID(rs.getInt("UI_ID"));
                ui.setFName(rs.getString("first_name"));
                ui.setMName(rs.getString("middle_name"));
                ui.setLName(rs.getString("last_name"));
                ui.setSex(rs.getString("sex"));
                ui.setContact(rs.getString("contact_number"));
                ui.setEmail(rs.getString("email_address"));
                ui.setHouseNum(rs.getInt("house_number"));
                ui.setStreet(rs.getString("street"));
                ui.setPurok(rs.getString("purok"));

                return ui;
            }

        } catch (SQLException e) {
            System.out.println("Error on getting user info on User_Info Table");
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (rs != null)
                    rs.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * This method is used to get the Credential object from database with a
     * specified username and password
     * getCredential();
     * 
     * @params String username, String password;
     * @return Credential object, null if error
     */

    public Credential getCredential(String username, String password) {
        Credential c = new Credential();
        String query = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection con = DBConnection.connect();

        try {
            query = "SELECT * FROM USER_INFO INNER JOIN CREDENTIAL ON Credential.UI_ID = User_Info.UI_ID WHERE username = ? AND password = ?;";
            statement = con.prepareStatement(query);

            statement.setString(1, username);
            statement.setString(2, password);

            rs = statement.executeQuery();

            if (rs.next()) {
                c.setUI_ID(rs.getInt("UI_ID"));
                c.setUsername(rs.getString("username"));
                c.setPassword(rs.getString("password"));
                return c;
            }
        } catch (SQLException e) {
            System.out.println("Error on getting credential on Credential Table");
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (rs != null)
                    rs.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
