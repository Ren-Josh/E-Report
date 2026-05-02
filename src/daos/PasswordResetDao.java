package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.UserInfo;

/**
 * DAO for password reset operations.
 */
public class PasswordResetDao {

    // ===== SQL STRINGS =====
    private String queryFindByUsername;
    private String queryFindByEmail;
    private String queryUpdatePassword;

    public PasswordResetDao() {
        queryFindByUsername = """
                SELECT ui.UI_ID, ui.first_name, ui.middle_name, ui.last_name,
                       ui.sex, ui.contact_number, ui.email_address,
                       ui.house_number, ui.purok,
                       c.username
                FROM User_Info ui
                INNER JOIN Credential c ON c.UI_ID = ui.UI_ID
                WHERE c.username = ?
                """;

        queryFindByEmail = """
                SELECT ui.UI_ID, ui.first_name, ui.middle_name, ui.last_name,
                       ui.sex, ui.contact_number, ui.email_address,
                       ui.house_number, ui.purok,
                       c.username
                FROM User_Info ui
                INNER JOIN Credential c ON c.UI_ID = ui.UI_ID
                WHERE ui.email_address = ?
                """;

        queryUpdatePassword = """
                UPDATE Credential
                SET password = ?
                WHERE UI_ID = ?
                """;
    }

    /**
     * Finds a user by username.
     */
    public UserInfo findByUsername(Connection con, String username) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(queryFindByUsername)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds a user by email.
     */
    public UserInfo findByEmail(Connection con, String email) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(queryFindByEmail)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Updates password for a user.
     */
    public boolean updatePassword(Connection con, int userId, String newPassword) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(queryUpdatePassword)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private UserInfo mapUser(ResultSet rs) throws SQLException {
        UserInfo ui = new UserInfo();
        ui.setUI_ID(rs.getInt("UI_ID"));
        ui.setFName(rs.getString("first_name"));
        ui.setMName(rs.getString("middle_name"));
        ui.setLName(rs.getString("last_name"));
        ui.setSex(rs.getString("sex"));
        ui.setContact(rs.getString("contact_number"));
        ui.setEmail(rs.getString("email_address"));
        ui.setHouseNum(rs.getString("house_number"));
        ui.setPurok(rs.getString("purok"));
        return ui;
    }
}