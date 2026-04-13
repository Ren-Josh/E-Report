package tests.units;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import config.DBConnection;
import models.ComplaintDetail;
import services.controller.ComplaintServiceController;

/**
 * Unit tests for ComplaintServiceController.
 *
 * All tests use a locally generated mock image file instead of downloading
 * from the internet, so they run offline and produce deterministic results.
 */
public class ComplaintServiceTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING COMPLAINT SERVICE CONTROLLER TESTS =====\n");

        ComplaintServiceController service = new ComplaintServiceController();

        testAddComplaintWithMockImage(service);
        testAddComplaintWithNoImage(service);
        testProcessAndAttachImage(service);

        System.out.println("=======================================================");
        if (allTestsPassed) {
            System.out.println("ALL COMPLAINT SERVICE TESTS PASSED!");
        } else {
            System.out.println("SOME COMPLAINT SERVICE TESTS FAILED. See logs above.");
        }
    }

    // ==========================================
    // TEST: addComplaint() with a mock local image
    // ==========================================
    public static void testAddComplaintWithMockImage(ComplaintServiceController service) {
        System.out.println("[TEST] addComplaint() — with a mock local image file");

        Connection con = DBConnection.connect();
        if (con == null) {
            System.out.println("-> FAIL: Database connection failed.\n");
            allTestsPassed = false;
            return;
        }

        File mockImageFile = null;

        try {
            int initialCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            // Create a local dummy image file — no internet required
            String mockFilePath = System.getProperty("user.dir") + "/mock_test_image.jpg";
            mockImageFile = new File(mockFilePath);
            try (FileWriter writer = new FileWriter(mockImageFile)) {
                writer.write("MOCK_JPEG_BYTES_FOR_UNIT_TESTING_" + System.currentTimeMillis());
            }

            ComplaintDetail cd = buildMockComplaint("Noise Complaint — Mock Image Test", "Disturbance");

            // Pre-attach bytes so service can verify them after the call
            cd.setPhotoAttachmentBytes(Files.readAllBytes(mockImageFile.toPath()));

            int userId = findFirstValidUserId(con);
            service.addComplaint(userId, cd, mockImageFile);

            int afterCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            boolean rowAdded   = afterCount == initialCount + 1;
            boolean blobStored = cd.getPhotoAttachmentBytes() != null
                                 && cd.getPhotoAttachmentBytes().length > 0;

            if (rowAdded && blobStored) {
                System.out.println("-> PASS: Complaint saved; row count " + initialCount
                        + " -> " + afterCount
                        + "; image bytes (" + cd.getPhotoAttachmentBytes().length + " bytes) attached\n");
            } else {
                if (!rowAdded) {
                    System.out.println("-> FAIL: Row count did not increase (expected "
                            + (initialCount + 1) + ", got " + afterCount + ")\n");
                }
                if (!blobStored) {
                    System.out.println("-> FAIL: Photo BLOB is missing in the model after addComplaint()\n");
                }
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception during test: " + e.getMessage() + "\n");
            e.printStackTrace();
            allTestsPassed = false;
        } finally {
            if (mockImageFile != null && mockImageFile.exists()) mockImageFile.delete();
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    // ==========================================
    // TEST: addComplaint() with null file (no image attached)
    // ==========================================
    public static void testAddComplaintWithNoImage(ComplaintServiceController service) {
        System.out.println("[TEST] addComplaint() — with null file (no image)");

        Connection con = DBConnection.connect();
        if (con == null) {
            System.out.println("-> FAIL: Database connection failed.\n");
            allTestsPassed = false;
            return;
        }

        try {
            int initialCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            ComplaintDetail cd = buildMockComplaint("Vandalism Report — No Image", "Vandalism");
            int userId = findFirstValidUserId(con);

            // Pass null as the file — should gracefully skip image processing
            service.addComplaint(userId, cd, null);

            int afterCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: Complaint saved without an image; row count "
                        + initialCount + " -> " + afterCount + "\n");
            } else {
                System.out.println("-> FAIL: Row count did not increase when submitting without image (expected "
                        + (initialCount + 1) + ", got " + afterCount + ")\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            e.printStackTrace();
            allTestsPassed = false;
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    // ==========================================
    // TEST: processAndAttachImage() sets photo path in model
    // ==========================================
    public static void testProcessAndAttachImage(ComplaintServiceController service) {
        System.out.println("[TEST] processAndAttachImage() — attaches image path to ComplaintDetail");

        File mockFile = null;
        try {
            String mockFilePath = System.getProperty("user.dir") + "/process_image_test.jpg";
            mockFile = new File(mockFilePath);
            try (FileWriter writer = new FileWriter(mockFile)) {
                writer.write("PROCESS_IMAGE_TEST_BYTES_" + System.currentTimeMillis());
            }

            ComplaintDetail cd = new ComplaintDetail();

            service.processAndAttachImage(cd, mockFile);

            String attachedPath = cd.getPhotoAttachment();

            if (attachedPath != null && !attachedPath.isEmpty()) {
                System.out.println("-> PASS: Photo path set on ComplaintDetail: \"" + attachedPath + "\"\n");
            } else {
                System.out.println("-> FAIL: Photo path was not set on ComplaintDetail after processAndAttachImage()\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            if (mockFile != null && mockFile.exists()) mockFile.delete();
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private static ComplaintDetail buildMockComplaint(String subject, String type) {
        ComplaintDetail cd = new ComplaintDetail();
        cd.setSubject(subject);
        cd.setType(type);
        cd.setPersonsInvolved("Unidentified suspect (test)");
        cd.setDetails("Automated test complaint record. Safe to ignore.");
        cd.setCurrentStatus("Pending");
        cd.setDateTime(new Timestamp(System.currentTimeMillis()));
        cd.setStreet("Rizal Street");
        cd.setPurok("Purok 4");
        cd.setLatitude(15.6625);
        cd.setLongitude(121.0142);
        return cd;
    }

    /** Returns the UI_ID of the first user in DB, or 1 as fallback. */
    private static int findFirstValidUserId(Connection con) {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UI_ID FROM User_Info LIMIT 1")) {
            if (rs.next()) return rs.getInt("UI_ID");
        } catch (Exception ignored) {}
        return 1;
    }

    private static int getTableRowCount(Connection con, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("[Warning] Could not get count for " + tableName);
        }
        return 0;
    }
}
