package DAOs;

import java.sql.*;
import config.AppConfig;
import config.DBConnection;
import models.UserInfo;
import models.Credential;

public class AddUserDAO {

    /**
     * Inserts user and returns generated ID. Uses the provided connection.
     */
    public static int addUser(Connection con, UserInfo ui) throws SQLException {
        String query = """
                INSERT INTO %s(first_name, middle_name, last_name, sex,
                                contact_number, email_address, house_number,
                                street, purok)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                """.formatted(AppConfig.TABLE_USER_INFO);

        try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ui.getFName());
            stmt.setString(2, ui.getMName());
            stmt.setString(3, ui.getLName());
            stmt.setString(4, ui.getSex());
            stmt.setString(5, ui.getContact());
            stmt.setString(6, ui.getEmail());
            stmt.setString(7, ui.getHouseNum());
            stmt.setString(8, ui.getStreet());
            stmt.setString(9, ui.getPurok());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Inserts credentials. Uses the provided connection.
     */
    public static boolean addCredential(Connection con, int userID, Credential c) throws SQLException {
        String query = """
                INSERT INTO %s(UI_ID, username, password, role, is_verified)
                VALUES (?, ?, ?, ?, ?);
                """.formatted(AppConfig.TABLE_CREDENTIAL);

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userID);
            stmt.setString(2, c.getUsername());
            stmt.setString(3, c.getPassword());
            stmt.setString(4, c.getRole());
            stmt.setBoolean(5, c.getIsVerified());

            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean isUsernameTaken(String username) {
        String query = "SELECT COUNT(*) FROM %s WHERE username = ?".formatted(AppConfig.TABLE_CREDENTIAL);

        try (Connection con = DBConnection.connect();
                PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Returns true if count > 0
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks the total number of credentials in the database.
     * 
     * @return the number of registered accounts.
     */
    public static int getUserCount() {
        String query = "SELECT COUNT(*) FROM %s".formatted(AppConfig.TABLE_CREDENTIAL);

        try (Connection con = DBConnection.connect();
                PreparedStatement stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}