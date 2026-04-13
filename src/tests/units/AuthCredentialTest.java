package tests.units;

import config.DBConnection;
import models.UserSession;
import services.controller.AuthCredential;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Unit tests for AuthCredential.authenticateUser().
 *
 * Tests cover:
 *   - Null username/password guards (returns null without querying DB)
 *   - Valid credentials => non-null UserSession with correct userId and role
 *   - Wrong password  => null returned (no session created)
 *   - Non-existent user => null returned
 */
public class AuthCredentialTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING AUTH CREDENTIAL TESTS =====\n");

        testAuthenticateWithNullUsername();
        testAuthenticateWithNullPassword();
        testAuthenticateWithBothNull();
        testAuthenticateWithValidCredentials();
        testAuthenticateWithWrongPassword();
        testAuthenticateWithNonExistentUser();

        System.out.println("===========================================");
        if (allTestsPassed) {
            System.out.println("ALL AUTH CREDENTIAL TESTS PASSED!");
        } else {
            System.out.println("SOME AUTH CREDENTIAL TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: null username — returns null immediately
    // ==========================================
    public static void testAuthenticateWithNullUsername() {
        System.out.println("[TEST] authenticateUser() — null username returns null");

        UserSession session = AuthCredential.authenticateUser(null, "anypassword");

        if (session == null) {
            System.out.println("-> PASS: Returned null for null username\n");
        } else {
            System.out.println("-> FAIL: Expected null but got a UserSession\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: null password — returns null immediately
    // ==========================================
    public static void testAuthenticateWithNullPassword() {
        System.out.println("[TEST] authenticateUser() — null password returns null");

        UserSession session = AuthCredential.authenticateUser("anyuser", null);

        if (session == null) {
            System.out.println("-> PASS: Returned null for null password\n");
        } else {
            System.out.println("-> FAIL: Expected null but got a UserSession\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: both null — returns null immediately
    // ==========================================
    public static void testAuthenticateWithBothNull() {
        System.out.println("[TEST] authenticateUser() — both null returns null");

        UserSession session = AuthCredential.authenticateUser(null, null);

        if (session == null) {
            System.out.println("-> PASS: Returned null for both-null inputs\n");
        } else {
            System.out.println("-> FAIL: Expected null but got a UserSession\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: valid credentials — returns UserSession with correct data
    // ==========================================
    public static void testAuthenticateWithValidCredentials() {
        System.out.println("[TEST] authenticateUser() — valid credentials return a UserSession");

        // Discover a real credential pair from the database
        String[] creds = findFirstCredentialPair();

        if (creds == null) {
            System.out.println("-> SKIP: No credentials in DB to test against.\n");
            return;
        }

        String username = creds[0];
        String password = creds[1];
        int    expectedUserId = Integer.parseInt(creds[2]);
        String expectedRole   = creds[3];

        UserSession session = AuthCredential.authenticateUser(username, password);

        if (session == null) {
            System.out.println("-> FAIL: authenticateUser() returned null for valid credentials \""
                    + username + "\"\n");
            allTestsPassed = false;
            return;
        }

        boolean userIdMatch = session.getUserId() == expectedUserId;
        boolean roleMatch   = expectedRole.equals(session.getRole());

        if (userIdMatch && roleMatch) {
            System.out.println("-> PASS: Session created — userId=" + session.getUserId()
                    + ", role=\"" + session.getRole()
                    + "\", verified=" + session.isVerified() + "\n");
        } else {
            if (!userIdMatch) {
                System.out.println("-> FAIL: userId mismatch (expected " + expectedUserId
                        + ", got " + session.getUserId() + ")\n");
            }
            if (!roleMatch) {
                System.out.println("-> FAIL: role mismatch (expected \"" + expectedRole
                        + "\", got \"" + session.getRole() + "\")\n");
            }
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: wrong password — returns null (no session)
    // ==========================================
    public static void testAuthenticateWithWrongPassword() {
        System.out.println("[TEST] authenticateUser() — wrong password returns null");

        String existingUsername = findFirstUsername();

        if (existingUsername == null) {
            System.out.println("-> SKIP: No credentials in DB to test against.\n");
            return;
        }

        UserSession session = AuthCredential.authenticateUser(
                existingUsername, "WRONG_PASSWORD_UNLIKELY_TO_MATCH_##");

        if (session == null) {
            System.out.println("-> PASS: Correctly returned null for wrong password\n");
        } else {
            System.out.println("-> FAIL: Expected null for wrong password but got a UserSession\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: non-existent user — returns null
    // ==========================================
    public static void testAuthenticateWithNonExistentUser() {
        System.out.println("[TEST] authenticateUser() — non-existent user returns null");

        // Generate a username that is virtually impossible to exist
        String ghostUser = "ghost_user_" + System.nanoTime();

        UserSession session = AuthCredential.authenticateUser(ghostUser, "password123");

        if (session == null) {
            System.out.println("-> PASS: Correctly returned null for non-existent user\n");
        } else {
            System.out.println("-> FAIL: Expected null but got a UserSession for a non-existent user\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    /**
     * Returns { username, password, UI_ID, role } for the first credential in DB,
     * or null if the table is empty.
     */
    private static String[] findFirstCredentialPair() {
        String query = "SELECT c.UI_ID, c.username, c.password, c.role "
                + "FROM Credential c LIMIT 1";
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return new String[]{
                    rs.getString("username"),
                    rs.getString("password"),
                    String.valueOf(rs.getInt("UI_ID")),
                    rs.getString("role")
                };
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Returns the username of the first credential row, or null if empty. */
    private static String findFirstUsername() {
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM Credential LIMIT 1")) {
            if (rs.next()) return rs.getString("username");
        } catch (Exception ignored) {}
        return null;
    }
}
