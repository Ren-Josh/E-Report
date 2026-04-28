package daos;

import features.core.usermanagement.UserData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManagementDao {

    private String queryAllUsers;
    private String queryUpdateUser;
    private String querySetBanStatus;
    private String querySearchUsers;

    public UserManagementDao() {
        queryAllUsers = """
                SELECT ui.UI_ID, ui.first_name, ui.middle_name, ui.last_name,
                       ui.contact_number, ui.email_address, ui.house_number, ui.street, ui.purok,
                       c.role, c.is_verified
                FROM User_Info ui
                INNER JOIN Credential c ON ui.UI_ID = c.UI_ID
                ORDER BY ui.UI_ID;
                """;

        queryUpdateUser = """
                UPDATE User_Info
                SET first_name = ?, contact_number = ?, email_address = ?,
                    house_number = ?, street = ?, purok = ?
                WHERE UI_ID = ?;
                """;

        querySetBanStatus = """
                UPDATE Credential
                SET is_verified = ?
                WHERE UI_ID = ?;
                """;

        querySearchUsers = """
                SELECT ui.UI_ID, ui.first_name, ui.middle_name, ui.last_name,
                       ui.contact_number, ui.email_address, ui.house_number, ui.street, ui.purok,
                       c.role, c.is_verified
                FROM User_Info ui
                INNER JOIN Credential c ON ui.UI_ID = c.UI_ID
                WHERE (LOWER(ui.first_name) LIKE ? OR LOWER(ui.last_name) LIKE ?)
                  AND (? = 'All Roles' OR c.role = ?)
                  AND (? = 'All Puroks' OR ui.purok = ?)
                  AND (? = 'All Statuses'
                       OR (? = 'Active' AND c.is_verified = 1)
                       OR (? = 'Banned' AND c.is_verified = 0))
                ORDER BY ui.UI_ID;
                """;
    }

    public List<UserData> getAllUsers(Connection con) {
        List<UserData> users = new ArrayList<>();
        try (con;
                PreparedStatement stmt = con.prepareStatement(queryAllUsers);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving all users from User_Info / Credential tables");
            System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
        }
        return users;
    }

    public boolean updateUser(Connection con, UserData user) {
        try (con;
                PreparedStatement stmt = con.prepareStatement(queryUpdateUser)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPhone());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getHouseNumber());
            stmt.setString(5, user.getStreet());
            stmt.setString(6, user.getPurok());
            stmt.setInt(7, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user in User_Info table");
            System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
            return false;
        }
    }

    public boolean setBanStatus(Connection con, int userId, boolean banned) {
        try (con;
                PreparedStatement stmt = con.prepareStatement(querySetBanStatus)) {

            stmt.setBoolean(1, !banned); // is_verified = !banned
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating ban status in Credential table");
            System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
            return false;
        }
    }

    public List<UserData> searchUsers(Connection con, String name, String role, String purok, String status) {
        List<UserData> users = new ArrayList<>();
        try (con;
                PreparedStatement stmt = con.prepareStatement(querySearchUsers)) {

            String likeName = "%" + (name == null ? "" : name.toLowerCase()) + "%";
            stmt.setString(1, likeName);
            stmt.setString(2, likeName);
            stmt.setString(3, role == null ? "All Roles" : role);
            stmt.setString(4, role == null ? "All Roles" : role);
            stmt.setString(5, purok == null ? "All Puroks" : purok);
            stmt.setString(6, purok == null ? "All Puroks" : purok);
            stmt.setString(7, status == null ? "All Statuses" : status);
            stmt.setString(8, status == null ? "All Statuses" : status);
            stmt.setString(9, status == null ? "All Statuses" : status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error searching users from User_Info / Credential tables");
            System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
        }
        return users;
    }

    private UserData mapRow(ResultSet rs) throws SQLException {
        String fullName = rs.getString("first_name") + " "
                + (rs.getString("middle_name") == null ? "" : rs.getString("middle_name") + " ")
                + rs.getString("last_name");

        return new UserData(
                rs.getInt("UI_ID"),
                fullName.trim(),
                rs.getString("role"),
                rs.getString("purok"),
                rs.getString("contact_number"),
                !rs.getBoolean("is_verified"), // banned = !is_verified
                rs.getString("house_number"),
                rs.getString("street"),
                rs.getString("email_address"));
    }
}