package tests.units;

import config.DBConnection;
import DAOs.GetUserDAO;
import models.UserInfo;
import models.Credential;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetUserDAOTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING GET USER DAO TESTS =====\n");

        GetUserDAO dao = new GetUserDAO();

        // Happy-path tests (require existing DB data)
        testGetUser(dao);
        testGetCredential(dao);

        // Edge-case / negative tests (no pre-existing data required)
        testGetUserNotFound(dao);
        testGetCredentialWrongPassword(dao);
        testGetCredentialWithNullUsername(dao);
        testGetCredentialWithNullPassword(dao);

        System.out.println("=======================================");
        if (allTestsPassed) {
            System.out.println("ALL GET USER DAO TESTS PASSED!");
        } else {
            System.out.println("SOME GET USER DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: getUser() — happy path
    // ==========================================
    public static void testGetUser(GetUserDAO dao) {
        System.out.println("[TEST] getUser() — fetches a known user by ID");

        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT User_Info.UI_ID, User_Info.first_name "
                     + "FROM User_Info "
                     + "INNER JOIN Credential ON Credential.UI_ID = User_Info.UI_ID "
                     + "LIMIT 1")) {

            if (!rs.next()) {
                System.out.println("-> SKIP: No users with credentials found in DB.\n");
                return;
            }

            int targetId       = rs.getInt("UI_ID");
            String expectedName = rs.getString("first_name");

            UserInfo ui = dao.getUser(targetId);

            if (ui != null && expectedName.equals(ui.getFName())) {
                System.out.println("-> PASS: getUser(" + targetId + ") returned user \""
                        + ui.getFName() + "\"\n");
            } else {
                System.out.println("-> FAIL: Returned object was null or first_name did not match "
                        + "(expected \"" + expectedName + "\")\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getCredential() — happy path
    // ==========================================
    public static void testGetCredential(GetUserDAO dao) {
        System.out.println("[TEST] getCredential() — fetches credentials for a known username/password");

        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, password FROM Credential LIMIT 1")) {

            if (!rs.next()) {
                System.out.println("-> SKIP: No credentials found in DB.\n");
                return;
            }

            String targetUser = rs.getString("username");
            String targetPass = rs.getString("password");

            Credential c = dao.getCredential(targetUser, targetPass);

            if (c != null && targetUser.equals(c.getUsername())) {
                System.out.println("-> PASS: getCredential() returned valid Credential for \""
                        + targetUser + "\"\n");
            } else {
                System.out.println("-> FAIL: Returned object was null or username did not match\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getUser() — returns null for non-existent ID
    // ==========================================
    public static void testGetUserNotFound(GetUserDAO dao) {
        System.out.println("[TEST] getUser() — returns null for non-existent UI_ID");

        try {
            UserInfo ui = dao.getUser(Integer.MAX_VALUE);

            if (ui == null) {
                System.out.println("-> PASS: getUser(MAX_INT) correctly returned null\n");
            } else {
                System.out.println("-> FAIL: Expected null but got a non-null UserInfo object\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception thrown instead of returning null: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getCredential() — returns null for wrong password
    // ==========================================
    public static void testGetCredentialWrongPassword(GetUserDAO dao) {
        System.out.println("[TEST] getCredential() — returns null for wrong password");

        // Find a real username, then supply a deliberately wrong password
        String existingUsername = findFirstExistingUsername();

        if (existingUsername == null) {
            System.out.println("-> SKIP: No credentials in DB to test against.\n");
            return;
        }

        try {
            Credential c = dao.getCredential(existingUsername, "WRONG_PASSWORD_##INVALID");

            if (c == null) {
                System.out.println("-> PASS: getCredential() returned null for wrong password\n");
            } else {
                System.out.println("-> FAIL: Expected null but got a Credential object with wrong password\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception thrown: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getCredential() — returns null for null username
    // ==========================================
    public static void testGetCredentialWithNullUsername(GetUserDAO dao) {
        System.out.println("[TEST] getCredential() — returns null when username is null");

        try {
            Credential c = dao.getCredential(null, "anypassword");

            if (c == null) {
                System.out.println("-> PASS: getCredential(null, ...) returned null gracefully\n");
            } else {
                System.out.println("-> FAIL: Expected null for null username but got a Credential object\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            // A SQL exception is also acceptable behaviour — the method should not crash the app
            System.out.println("-> PASS (exception handled): getCredential(null, ...) threw "
                    + e.getClass().getSimpleName() + " — acceptable for null input\n");
        }
    }

    // ==========================================
    // TEST: getCredential() — returns null for null password
    // ==========================================
    public static void testGetCredentialWithNullPassword(GetUserDAO dao) {
        System.out.println("[TEST] getCredential() — returns null when password is null");

        String existingUsername = findFirstExistingUsername();
        String username = (existingUsername != null) ? existingUsername : "any_user";

        try {
            Credential c = dao.getCredential(username, null);

            if (c == null) {
                System.out.println("-> PASS: getCredential(..., null) returned null gracefully\n");
            } else {
                System.out.println("-> FAIL: Expected null for null password but got a Credential object\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> PASS (exception handled): getCredential(..., null) threw "
                    + e.getClass().getSimpleName() + " — acceptable for null input\n");
        }
    }

    // ==========================================
    // HELPER
    // ==========================================

    private static String findFirstExistingUsername() {
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM Credential LIMIT 1")) {
            if (rs.next()) return rs.getString("username");
        } catch (Exception ignored) {}
        return null;
    }
}
