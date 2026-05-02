package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for verifying user passwords.
 */
public class VerifyPasswordDao {

    // ===== SQL STRINGS =====
    private String queryVerify;

    public VerifyPasswordDao() {
        queryVerify = """
                SELECT UI_ID FROM Credential
                WHERE UI_ID = ? AND password = ?;
                """;
    }

    /**
     * Verifies if the given password matches the stored password for the user.
     *
     * @param con      Active DB connection
     * @param userId   User ID
     * @param password Password to verify
     * @return true if matches
     */
    public boolean verifyPassword(Connection con, int userId, String password) {
        try (PreparedStatement stmt = con.prepareStatement(queryVerify)) {
            stmt.setInt(1, userId);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error verifying password");
            e.printStackTrace();
        }
        return false;
    }
}