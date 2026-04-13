package tests.units;

import config.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class DBTest {

    private static boolean allTestsPassed = true;

    // Canonical list of expected tables — single source of truth used by all tests
    private static final String[] EXPECTED_TABLES = {
        "Complaint",
        "Complaint_Action",
        "Complaint_Detail",
        "Complaint_History",
        "Complaint_History_Detail",
        "Credential",
        "User_Info"
    };

    public static void main(String[] args) {

        System.out.println("===== STARTING DB TESTS =====\n");

        testCreateDatabase();
        testConnectionSuccess();
        testTableCreation();
        testTableRecreation();
        testAllExpectedTablesExist();

        System.out.println("==============================");
        if (allTestsPassed) {
            System.out.println("ALL DB TESTS PASSED!");
        } else {
            System.out.println("SOME DB TESTS FAILED. See the logs above.");
        }
    }

    // =========================
    // TEST: DBCreate
    // =========================

    public static void testCreateDatabase() {
        System.out.println("[TEST] Database Creation");

        try {
            DBCreate.createDatabase();
            System.out.println("-> PASS: Database creation handled without exception\n");
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception during database creation: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // =========================
    // TEST: DBConnection
    // =========================

    public static void testConnectionSuccess() {
        System.out.println("[TEST] Connection SUCCESS");

        Connection con = DBConnection.connect();

        if (con != null) {
            System.out.println("-> PASS: Connection returned a non-null Connection object\n");
            try { con.close(); } catch (Exception ignored) {}
        } else {
            System.out.println("-> FAIL: DBConnection.connect() returned null\n");
            allTestsPassed = false;
        }
    }

    // =========================
    // TEST: TBCreate (first run)
    // =========================

    public static void testTableCreation() {
        System.out.println("[TEST] Table Creation (First Run)");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("-> FAIL: Cannot test table creation — DB connection failed\n");
            allTestsPassed = false;
            return;
        }

        try {
            TBCreate.createTables(con);
            verifyTablesExist(con);
            System.out.println("-> PASS: All expected tables exist after first run\n");
        } catch (Exception e) {
            System.out.println("-> FAIL: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    // =========================
    // TEST: TBCreate (second run — idempotency)
    // =========================

    public static void testTableRecreation() {
        System.out.println("[TEST] Table Creation (Second Run — Idempotency)");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("-> FAIL: Cannot test table recreation — DB connection failed\n");
            allTestsPassed = false;
            return;
        }

        try {
            // Running createTables() a second time must not throw or drop tables
            TBCreate.createTables(con);
            verifyTablesExist(con);
            System.out.println("-> PASS: All expected tables still exist after second run (idempotent)\n");
        } catch (Exception e) {
            System.out.println("-> FAIL: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    // =========================
    // TEST: Verify expected table count via DatabaseMetaData
    // =========================

    public static void testAllExpectedTablesExist() {
        System.out.println("[TEST] Expected Table Count via DatabaseMetaData");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("-> FAIL: Cannot verify table count — DB connection failed\n");
            allTestsPassed = false;
            return;
        }

        try {
            DatabaseMetaData meta = con.getMetaData();
            int foundCount = 0;

            for (String table : EXPECTED_TABLES) {
                try (ResultSet rs = meta.getTables(null, null, table, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        foundCount++;
                    } else {
                        System.out.println("   [MISSING] Table not found in metadata: " + table);
                    }
                }
            }

            if (foundCount == EXPECTED_TABLES.length) {
                System.out.println("-> PASS: All " + EXPECTED_TABLES.length
                        + " expected tables confirmed via DatabaseMetaData\n");
            } else {
                System.out.println("-> FAIL: Only " + foundCount + " of " + EXPECTED_TABLES.length
                        + " expected tables found\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception during metadata check: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    // =========================
    // HELPER: Queries each expected table — throws if any is missing
    // =========================

    private static void verifyTablesExist(Connection con) throws Exception {
        for (String table : EXPECTED_TABLES) {
            con.createStatement().executeQuery("SELECT 1 FROM " + table + " LIMIT 1");
        }
    }
}
