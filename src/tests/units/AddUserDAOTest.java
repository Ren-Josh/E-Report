package tests.units;

import config.DBConnection;
import DAOs.AddUserDAO;
import models.UserInfo;
import models.Credential;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddUserDAOTest {

    private static boolean allTestsPassed = true;

    // Stores the generated userId from testAddUser so testAddCredential
    // operates on a known-valid user rather than a hardcoded ID.
    private static int lastInsertedUserId = -1;

    public static void main(String[] args) {
        System.out.println("===== STARTING ADD USER DAO TESTS =====\n");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("CRITICAL: Cannot run tests — database connection failed!");
            return;
        }

        testAddUser(con);
        testAddUserReturnsValidId();
        testAddCredential(con);
        testAddCredentialReturnsTrue(con);
        testIsUsernameTaken();
        testUsernameNotTakenForUnknownUsername();
        testGetUserCount();

        try { con.close(); } catch (Exception ignored) {}

        System.out.println("========================================");
        if (allTestsPassed) {
            System.out.println("ALL ADD USER DAO TESTS PASSED!");
        } else {
            System.out.println("SOME ADD USER DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: Add User — row count increases by 1
    // ==========================================
    public static void testAddUser(Connection con) {
        System.out.println("[TEST] Add User — row count increases");

        try {
            int initialCount = getTableRowCount(con, "User_Info");

            UserInfo ui = buildMockUser();
            int returnedId = AddUserDAO.addUser(con, ui);

            int afterCount = getTableRowCount(con, "User_Info");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: User_Info row count increased from "
                        + initialCount + " to " + afterCount + "\n");
                lastInsertedUserId = returnedId; // pass to downstream tests
            } else {
                System.out.println("-> FAIL: Row count did not increase! Expected "
                        + (initialCount + 1) + " but got " + afterCount + "\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Add User — returned user ID is positive
    // ==========================================
    public static void testAddUserReturnsValidId() {
        System.out.println("[TEST] Add User — returned UI_ID is a positive integer");

        if (lastInsertedUserId > 0) {
            System.out.println("-> PASS: addUser() returned UI_ID = " + lastInsertedUserId + "\n");
        } else {
            System.out.println("-> FAIL: addUser() returned an invalid UI_ID ("
                    + lastInsertedUserId + ")\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Add Credential — row count increases by 1
    // ==========================================
    public static void testAddCredential(Connection con) {
        System.out.println("[TEST] Add Credential — row count increases");

        // Use the ID returned from testAddUser; fall back to any valid user in the DB
        int userId = (lastInsertedUserId > 0) ? lastInsertedUserId : findFirstValidUserId(con);

        if (userId == -1) {
            System.out.println("-> SKIP: No valid user found in DB. Run testAddUser first.\n");
            return;
        }

        try {
            int initialCount = getTableRowCount(con, "Credential");

            Credential c = new Credential();
            // Use a unique username to avoid duplicate-key errors on repeated runs
            c.setUsername("testuser_" + System.currentTimeMillis());
            c.setPassword("securePassword123");
            c.setRole("User");
            c.setIsVerified(true);

            boolean result = AddUserDAO.addCredential(con, userId, c);

            int afterCount = getTableRowCount(con, "Credential");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: Credential row count increased from "
                        + initialCount + " to " + afterCount + "\n");
            } else {
                System.out.println("-> FAIL: Row count did not increase! Expected "
                        + (initialCount + 1) + " but got " + afterCount + "\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Add Credential — method returns true
    // ==========================================
    public static void testAddCredentialReturnsTrue(Connection con) {
        System.out.println("[TEST] Add Credential — method returns true");

        int userId = (lastInsertedUserId > 0) ? lastInsertedUserId : findFirstValidUserId(con);

        if (userId == -1) {
            System.out.println("-> SKIP: No valid user found in DB.\n");
            return;
        }

        try {
            Credential c = new Credential();
            c.setUsername("retval_check_" + System.currentTimeMillis());
            c.setPassword("testPass456");
            c.setRole("User");
            c.setIsVerified(false);

            boolean result = AddUserDAO.addCredential(con, userId, c);

            if (result) {
                System.out.println("-> PASS: addCredential() returned true\n");
            } else {
                System.out.println("-> FAIL: addCredential() returned false unexpectedly\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: isUsernameTaken — returns true for an existing username
    // ==========================================
    public static void testIsUsernameTaken() {
        System.out.println("[TEST] isUsernameTaken() — returns true for existing username");

        // Look up any real username from the database
        String existingUsername = findFirstExistingUsername();

        if (existingUsername == null) {
            System.out.println("-> SKIP: No credential records found in DB to test against.\n");
            return;
        }

        boolean taken = AddUserDAO.isUsernameTaken(existingUsername);

        if (taken) {
            System.out.println("-> PASS: isUsernameTaken(\"" + existingUsername
                    + "\") correctly returned true\n");
        } else {
            System.out.println("-> FAIL: isUsernameTaken(\"" + existingUsername
                    + "\") returned false for a username that exists in the DB\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: isUsernameTaken — returns false for a random unknown username
    // ==========================================
    public static void testUsernameNotTakenForUnknownUsername() {
        System.out.println("[TEST] isUsernameTaken() — returns false for non-existent username");

        // Generate a username that is extremely unlikely to exist
        String ghostUsername = "nonexistent_user_" + System.nanoTime();

        boolean taken = AddUserDAO.isUsernameTaken(ghostUsername);

        if (!taken) {
            System.out.println("-> PASS: isUsernameTaken(\"" + ghostUsername
                    + "\") correctly returned false\n");
        } else {
            System.out.println("-> FAIL: isUsernameTaken() returned true for a username that should not exist\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getUserCount — returns a non-negative integer
    // ==========================================
    public static void testGetUserCount() {
        System.out.println("[TEST] getUserCount() — returns a non-negative count");

        int count = AddUserDAO.getUserCount();

        if (count >= 0) {
            System.out.println("-> PASS: getUserCount() returned " + count + " (valid non-negative count)\n");
        } else {
            System.out.println("-> FAIL: getUserCount() returned a negative value: " + count + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private static UserInfo buildMockUser() {
        UserInfo ui = new UserInfo();
        ui.setFName("Jane");
        ui.setMName("M.");
        ui.setLName("Doe");
        ui.setSex("F");
        ui.setContact("09171234567");
        ui.setEmail("janedoe_" + System.currentTimeMillis() + "@example.com");
        ui.setHouseNum("12");
        ui.setStreet("Rizal Ave.");
        ui.setPurok("Purok 3");
        return ui;
    }

    /** Returns the UI_ID of the first user, or -1 if the table is empty. */
    private static int findFirstValidUserId(Connection con) {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UI_ID FROM User_Info LIMIT 1")) {
            if (rs.next()) return rs.getInt("UI_ID");
        } catch (Exception ignored) {}
        return -1;
    }

    /** Returns any existing username from the Credential table, or null if empty. */
    private static String findFirstExistingUsername() {
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM Credential LIMIT 1")) {
            if (rs.next()) return rs.getString("username");
        } catch (Exception ignored) {}
        return null;
    }

    /** Returns row count for the given table, or 0 on error. */
    private static int getTableRowCount(Connection con, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("[Warning] Could not get count for " + tableName + ": " + e.getMessage());
        }
        return 0;
    }
}
