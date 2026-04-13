package tests.units;

import config.DBConnection;
import DAOs.AddComplaintDAO;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.ComplaintAction;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

public class AddComplaintDAOTest {

    private static boolean allTestsPassed = true;

    // Tracks the CD_ID created during testAddComplaint so subsequent tests
    // can use a guaranteed-valid complaint ID instead of a hardcoded one.
    private static int lastInsertedCdId = -1;

    public static void main(String[] args) {
        System.out.println("===== STARTING ADD COMPLAINT DAO TESTS =====\n");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("CRITICAL: Cannot run tests — database connection failed!");
            return;
        }

        testAddComplaint(con);
        testAddComplaintReturnsValidId(con);
        testAddComplaintHistory(con);
        testAddComplaintAction(con);

        try { con.close(); } catch (Exception ignored) {}

        System.out.println("============================================");
        if (allTestsPassed) {
            System.out.println("ALL ADD COMPLAINT DAO TESTS PASSED!");
        } else {
            System.out.println("SOME ADD COMPLAINT DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: Add Complaint — row count increases by 1
    // ==========================================
    public static void testAddComplaint(Connection con) {
        System.out.println("[TEST] Add Complaint — row count increases");

        File mockFile = null;
        try {
            int initialCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            ComplaintDetail cd = buildMockComplaint();

            // Create a temporary dummy file to simulate an image upload
            String mockFilePath = System.getProperty("user.dir") + "/test_evidence.jpg";
            mockFile = new File(mockFilePath);
            try (FileWriter writer = new FileWriter(mockFile)) {
                writer.write("Simulated image bytes for unit testing");
            }
            cd.setPhotoAttachmentBytes(Files.readAllBytes(mockFile.toPath()));

            // Use the first valid user ID in the DB, or fall back to 1
            int userId = findFirstValidUserId(con);

            int returnedCdId = AddComplaintDAO.addComplaint(con, userId, cd);

            int afterCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: Complaint_Detail row count increased from "
                        + initialCount + " to " + afterCount + "\n");
                lastInsertedCdId = returnedCdId; // store for downstream tests
            } else {
                System.out.println("-> FAIL: Row count did not increase! Expected "
                        + (initialCount + 1) + " but got " + afterCount + "\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            if (mockFile != null && mockFile.exists()) mockFile.delete();
        }
    }

    // ==========================================
    // TEST: Add Complaint — returned CD_ID is positive
    // ==========================================
    public static void testAddComplaintReturnsValidId(Connection con) {
        System.out.println("[TEST] Add Complaint — returned CD_ID is a positive integer");

        if (lastInsertedCdId > 0) {
            System.out.println("-> PASS: addComplaint() returned CD_ID = " + lastInsertedCdId + "\n");
        } else {
            System.out.println("-> FAIL: addComplaint() returned an invalid CD_ID ("
                    + lastInsertedCdId + "). Either the insert failed or testAddComplaint was skipped.\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Add Complaint History — row count increases, return ID > 0
    // ==========================================
    public static void testAddComplaintHistory(Connection con) {
        System.out.println("[TEST] Add Complaint History — row count increases");

        // Use the CD_ID from the insert test; if unavailable, discover one dynamically
        int complaintId = (lastInsertedCdId > 0) ? lastInsertedCdId : findFirstValidComplaintId(con);

        if (complaintId == -1) {
            System.out.println("-> SKIP: No valid complaint found in DB. Run testAddComplaint first.\n");
            return;
        }

        try {
            int initialCount = getTableRowCount(con, "Complaint_History_Detail");

            ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
            chd.setStatus("Under Investigation");
            chd.setProcess("Officer communicated with persons involved for further investigation.");
            chd.setDateTimeUpdated(new Timestamp(System.currentTimeMillis()));
            chd.setUpdatedBy("Admin Officer");

            int returnedChdId = AddComplaintDAO.addComplaintHistory(con, complaintId, chd);

            int afterCount = getTableRowCount(con, "Complaint_History_Detail");

            boolean rowAdded = afterCount == initialCount + 1;
            boolean idValid  = returnedChdId > 0;

            if (rowAdded && idValid) {
                System.out.println("-> PASS: Complaint_History_Detail row added (CHD_ID = "
                        + returnedChdId + ")\n");
            } else {
                if (!rowAdded) {
                    System.out.println("-> FAIL: Row count did not increase! Expected "
                            + (initialCount + 1) + " but got " + afterCount + "\n");
                }
                if (!idValid) {
                    System.out.println("-> FAIL: addComplaintHistory() returned invalid ID: "
                            + returnedChdId + "\n");
                }
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Add Complaint Action — row count increases, returns true
    // ==========================================
    public static void testAddComplaintAction(Connection con) {
        System.out.println("[TEST] Add Complaint Action — row count increases");

        int complaintId = (lastInsertedCdId > 0) ? lastInsertedCdId : findFirstValidComplaintId(con);

        if (complaintId == -1) {
            System.out.println("-> SKIP: No valid complaint found in DB. Run testAddComplaint first.\n");
            return;
        }

        try {
            int initialCount = getTableRowCount(con, "Complaint_Action");

            ComplaintAction ca = new ComplaintAction();
            ca.setActionTaken("Issued a formal verbal warning to the offender.");
            ca.setRecommendation("Monitor the area for 30 days for repeat offenses.");
            ca.setOIC("Officer Reyes");
            ca.setResolutionDateTime(new Timestamp(System.currentTimeMillis()));

            boolean result = AddComplaintDAO.addComplaintAction(con, complaintId, ca);

            int afterCount = getTableRowCount(con, "Complaint_Action");

            boolean rowAdded    = afterCount == initialCount + 1;
            boolean returnedTrue = result;

            if (rowAdded && returnedTrue) {
                System.out.println("-> PASS: Complaint_Action row added and method returned true\n");
            } else {
                if (!rowAdded) {
                    System.out.println("-> FAIL: Row count did not increase! Expected "
                            + (initialCount + 1) + " but got " + afterCount + "\n");
                }
                if (!returnedTrue) {
                    System.out.println("-> FAIL: addComplaintAction() returned false\n");
                }
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    /** Builds a realistic ComplaintDetail for test use (no BLOB attached). */
    private static ComplaintDetail buildMockComplaint() {
        ComplaintDetail cd = new ComplaintDetail();
        cd.setCurrentStatus("Pending");
        cd.setSubject("Noise Complaint — Test");
        cd.setType("Disturbance");
        cd.setDateTime(new Timestamp(System.currentTimeMillis()));
        cd.setStreet("Apple St.");
        cd.setPurok("Purok 1");
        cd.setLongitude(120.1234);
        cd.setLatitude(15.5678);
        cd.setPersonsInvolved("John Doe (test)");
        cd.setDetails("Playing loud music at 2 AM. (automated test record)");
        return cd;
    }

    /** Returns the UI_ID of the first user in the database, or 1 as a safe fallback. */
    private static int findFirstValidUserId(Connection con) {
        String query = "SELECT UI_ID FROM User_Info LIMIT 1";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt("UI_ID");
        } catch (Exception ignored) {}
        return 1; // fallback — tests should be run after AddUserDAOTest
    }

    /** Returns the CD_ID of the first complaint in the database, or -1 if none. */
    private static int findFirstValidComplaintId(Connection con) {
        String query = "SELECT CD_ID FROM Complaint_Detail LIMIT 1";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt("CD_ID");
        } catch (Exception ignored) {}
        return -1;
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
