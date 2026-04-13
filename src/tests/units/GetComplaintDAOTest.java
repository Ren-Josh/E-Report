package tests.units;

import config.DBConnection;
import DAOs.GetComplaintDAO;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.ComplaintAction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class GetComplaintDAOTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING GET COMPLAINT DAO TESTS =====\n");

        GetComplaintDAO dao = new GetComplaintDAO();

        // Dynamically locate valid IDs from the database
        int[] complaintIds    = findValidComplaintIds();
        int[] historyIds      = findValidComplaintHistoryIds();
        int   validActionCdId = findValidComplaintActionId();

        // --- Happy-path tests ---
        if (complaintIds != null) {
            testGetComplaint(dao, complaintIds[0], complaintIds[1]);
            testGetAllComplaint(dao, complaintIds[0]);
        } else {
            System.out.println("[SKIP] testGetComplaint / testGetAllComplaint: No complaint data in DB.\n");
        }

        if (historyIds != null) {
            testGetComplaintHistory(dao, historyIds[0]);
        } else {
            System.out.println("[SKIP] testGetComplaintHistory (happy path): No history data in DB.\n");
        }

        if (validActionCdId != -1) {
            testGetComplaintAction(dao, validActionCdId);
        } else {
            System.out.println("[SKIP] testGetComplaintAction (happy path): No action data in DB.\n");
        }

        // --- Edge-case / negative tests (no DB data required) ---
        testGetComplaintNotFound(dao);
        testGetAllComplaintEmptyForUnknownUser(dao);
        testGetComplaintHistoryEmptyForUnknownCdId(dao);
        testGetComplaintActionNotFound(dao);

        System.out.println("============================================");
        if (allTestsPassed) {
            System.out.println("ALL GET COMPLAINT DAO TESTS PASSED!");
        } else {
            System.out.println("SOME GET COMPLAINT DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: Get Single Complaint (happy path)
    // ==========================================
    public static void testGetComplaint(GetComplaintDAO dao, int uiId, int cdId) {
        System.out.println("[TEST] getComplaint() — fetches a known complaint");

        try {
            ComplaintDetail cd = dao.getComplaint(uiId, cdId);
            if (cd != null) {
                System.out.println("-> PASS: Fetched complaint CD_ID=" + cdId
                        + ", subject=\"" + cd.getSubject() + "\"");

                byte[] photo = cd.getPhotoAttachmentBytes();
                if (photo != null && photo.length > 0) {
                    System.out.println("   Photo BLOB: " + photo.length + " bytes");
                } else {
                    System.out.println("   Photo BLOB: [none attached]");
                }
                System.out.println();
            } else {
                System.out.println("-> FAIL: getComplaint() returned null for CD_ID=" + cdId + "\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Get All Complaints (happy path)
    // ==========================================
    public static void testGetAllComplaint(GetComplaintDAO dao, int uiId) {
        System.out.println("[TEST] getAllComplaint() — fetches all complaints for a user");
        try {
            List<ComplaintDetail> list = dao.getAllComplaint(uiId);
            if (list != null && !list.isEmpty()) {
                System.out.println("-> PASS: Retrieved " + list.size()
                        + " complaint(s) for UI_ID=" + uiId + "\n");
            } else {
                System.out.println("-> FAIL: Returned list was null or empty for UI_ID=" + uiId + "\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Get Complaint History (happy path)
    // ==========================================
    public static void testGetComplaintHistory(GetComplaintDAO dao, int cdId) {
        System.out.println("[TEST] getComplaintHistory() — fetches history for a known CD_ID");
        try {
            List<ComplaintHistoryDetail> list = dao.getComplaintHistory(cdId);
            if (list != null && !list.isEmpty()) {
                System.out.println("-> PASS: Retrieved " + list.size()
                        + " history record(s) for CD_ID=" + cdId);
                for (int i = 0; i < list.size(); i++) {
                    ComplaintHistoryDetail chd = list.get(i);
                    System.out.println("   [" + (i + 1) + "] status=\"" + chd.getStatus()
                            + "\", updatedBy=\"" + chd.getUpdatedBy() + "\"");
                }
                System.out.println();
            } else {
                System.out.println("-> FAIL: History list was null or empty for CD_ID=" + cdId + "\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Get Complaint Action (happy path)
    // ==========================================
    public static void testGetComplaintAction(GetComplaintDAO dao, int cdId) {
        System.out.println("[TEST] getComplaintAction() — fetches action for a known CD_ID");
        try {
            ComplaintAction ca = dao.getComplaintAction(cdId);
            if (ca != null) {
                System.out.println("-> PASS: Fetched action for CD_ID=" + cdId
                        + ", oic=\"" + ca.getOIC() + "\"\n");
            } else {
                System.out.println("-> FAIL: getComplaintAction() returned null for CD_ID=" + cdId + "\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getComplaint() returns null for non-existent IDs
    // ==========================================
    public static void testGetComplaintNotFound(GetComplaintDAO dao) {
        System.out.println("[TEST] getComplaint() — returns null for non-existent IDs");
        try {
            ComplaintDetail cd = dao.getComplaint(Integer.MAX_VALUE, Integer.MAX_VALUE);
            if (cd == null) {
                System.out.println("-> PASS: Correctly returned null for non-existent UI_ID/CD_ID\n");
            } else {
                System.out.println("-> FAIL: Expected null but got a non-null object\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception thrown instead of returning null: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getAllComplaint() returns empty list for unknown user
    // ==========================================
    public static void testGetAllComplaintEmptyForUnknownUser(GetComplaintDAO dao) {
        System.out.println("[TEST] getAllComplaint() — returns empty list for non-existent UI_ID");
        try {
            List<ComplaintDetail> list = dao.getAllComplaint(Integer.MAX_VALUE);
            if (list != null && list.isEmpty()) {
                System.out.println("-> PASS: Returned an empty (not null) list for non-existent UI_ID\n");
            } else if (list == null) {
                System.out.println("-> FAIL: Returned null instead of an empty list\n");
                allTestsPassed = false;
            } else {
                System.out.println("-> FAIL: Expected empty list but got " + list.size() + " record(s)\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception thrown: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getComplaintHistory() returns empty list for unknown CD_ID
    // ==========================================
    public static void testGetComplaintHistoryEmptyForUnknownCdId(GetComplaintDAO dao) {
        System.out.println("[TEST] getComplaintHistory() — returns empty list for non-existent CD_ID");
        try {
            List<ComplaintHistoryDetail> list = dao.getComplaintHistory(Integer.MAX_VALUE);
            if (list != null && list.isEmpty()) {
                System.out.println("-> PASS: Returned an empty (not null) list for non-existent CD_ID\n");
            } else if (list == null) {
                System.out.println("-> FAIL: Returned null instead of an empty list\n");
                allTestsPassed = false;
            } else {
                System.out.println("-> FAIL: Expected empty list but got " + list.size() + " record(s)\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception thrown: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: getComplaintAction() returns null for non-existent CD_ID
    // ==========================================
    public static void testGetComplaintActionNotFound(GetComplaintDAO dao) {
        System.out.println("[TEST] getComplaintAction() — returns null for non-existent CD_ID");
        try {
            ComplaintAction ca = dao.getComplaintAction(Integer.MAX_VALUE);
            if (ca == null) {
                System.out.println("-> PASS: Correctly returned null for non-existent CD_ID\n");
            } else {
                System.out.println("-> FAIL: Expected null but got a non-null object\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception thrown instead of returning null: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // ID DISCOVERY HELPERS
    // ==========================================

    private static int[] findValidComplaintIds() {
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UI_ID, CD_ID FROM Complaint LIMIT 1")) {
            if (rs.next())
                return new int[]{ rs.getInt("UI_ID"), rs.getInt("CD_ID") };
        } catch (Exception ignored) {}
        return null;
    }

    private static int[] findValidComplaintHistoryIds() {
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT CD_ID, CHD_ID FROM Complaint_History LIMIT 1")) {
            if (rs.next())
                return new int[]{ rs.getInt("CD_ID"), rs.getInt("CHD_ID") };
        } catch (Exception ignored) {}
        return null;
    }

    private static int findValidComplaintActionId() {
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT CD_ID FROM Complaint_Action LIMIT 1")) {
            if (rs.next()) return rs.getInt("CD_ID");
        } catch (Exception ignored) {}
        return -1;
    }
}
