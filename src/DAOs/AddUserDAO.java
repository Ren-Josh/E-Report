package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import models.UserInfo;
import models.Credential;

/**
 * Adding logic shall be the following:
 * addUser() -> addCredential();
 * 
 * All method in this class
 * addUser(Connection con, UserInfo ui)
 * addCredential(Connection con, int userID, Credential c)
 */

public class AddUserDAO {

    /**
     * This method is used to add user info on user_info table
     * addUser();
     * 
     * @params DB Connection, UserInfo data
     * @return none
     */

    public void addUser(Connection con, UserInfo ui) {

        String query = "INSERT INTO User_Info(first_name, middle_name, last_name, sex, contact_number, email_address, house_number, street, purok) VALUES(?,?,?,?,?,?,?,?,?);";
        int rows;
        PreparedStatement statement;

        try {
            statement = con.prepareStatement(query);
            statement.setString(1, ui.getFName());
            statement.setString(2, ui.getMName());
            statement.setString(3, ui.getLName());
            statement.setString(4, ui.getSex());
            statement.setString(5, ui.getContact());
            statement.setString(6, ui.getEmail());
            statement.setInt(7, ui.getHouseNum());
            statement.setString(8, ui.getStreet());
            statement.setString(9, ui.getPurok());

            rows = statement.executeUpdate();
            System.out.println(rows + " rows(s) inserted on User_Info");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is used to add credential on credential table
     * addCredential();
     * 
     * @params DB Connection, user Id, Credential data
     * @return none
     */

    public void addCredential(Connection con, int userID, Credential c) {
        String query = "INSERT INTO Credential(UI_ID, username, password, role, is_verified) VALUES(?,?,?,?,?);";
        int rows;
        PreparedStatement statement;

        try {
            statement = con.prepareStatement(query);
            statement = con.prepareStatement(query);
            statement.setInt(1, userID);
            statement.setString(2, c.getUsername());
            statement.setString(3, c.getPassword());
            statement.setString(4, c.getRole());
            statement.setBoolean(5, c.getIsVerified());

            rows = statement.executeUpdate();
            System.out.println(rows + " rows(s) inserted on Credential");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
