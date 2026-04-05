package tests.units;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import config.DBConnection;
import models.ComplaintDetail;
import services.controller.ComplaintService;

public class ComplaintServiceTest {

    private static boolean testPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING COMPLAINT SERVICE CONTROLLER TEST =====\n");

        ComplaintService service = new ComplaintService();
        testAddComplaintWithRealImage(service);

        System.out.println("====================================================");
        if (testPassed) {
            System.out.println("CONTROLLER TEST PASSED!");
        } else {
            System.out.println("CONTROLLER TEST FAILED. See logs above.");
        }
    }

    public static void testAddComplaintWithRealImage(ComplaintService service) {
        System.out.println("[TEST] Add Complaint via Service Controller (Real Image)");

        Connection con = DBConnection.connect();
        if (con == null) {
            System.out.println("-> FAIL: Database connection failed. Cannot verify test.");
            testPassed = false;
            return;
        }

        File realImageFile = null;

        try {
            // 1. Arrange: Get initial count of records
            int initialCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            // 2. Arrange: Download a REAL image from the internet
            String realFilePath = System.getProperty("user.dir") + "/downloaded_test_image.jpg";
            realImageFile = new File(realFilePath);

            System.out.println("-> Downloading a real image for testing...");
            URL url = URI.create("https://picsum.photos/200/300").toURL();
            try (InputStream in = url.openStream();
                    FileOutputStream out = new FileOutputStream(realImageFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("-> Real image successfully downloaded to: " + realFilePath);

            // 3. Arrange: Populate the mock frontend complaint data
            ComplaintDetail cd = new ComplaintDetail();
            cd.setSubject("Stolen Bicycle outside Brgy. Hall");
            cd.setType("Theft");
            cd.setPersonsInvolved("Unidentified suspect wearing a black hoodie and blue cap");

            cd.setDetails("I parked my mountain bike (red and black Trinx) outside the Barangay Hall " +
                    "while I went inside to secure a permit at around 2:30 PM today. When I came out " +
                    "at around 3:00 PM, the bike was gone. The lock was cut and left on the ground. " +
                    "The CCTV in the area might have captured the incident.");

            cd.setCurrentStatus("Pending");
            cd.setDateTime(new Timestamp(System.currentTimeMillis()));

            cd.setStreet("Rizal Street");
            cd.setPurok("Purok 4");
            cd.setLatitude(15.6625);
            cd.setLongitude(121.0142);

            int mockUserId = 1; // Assuming User ID 1 exists in your database

            // 4. Act: Call your frontend controller method
            System.out.println("-> Calling service.addComplaint()...");
            service.addComplaint(mockUserId, cd, realImageFile);

            // 5. Assert A: Check if the row count in the database increased
            int afterCount = getTableRowCount(con, "COMPLAINT_DETAIL");
            if (afterCount != initialCount + 1) {
                System.out.println("-> FAIL: Database row count did not increase!");
                testPassed = false;
                return;
            }

            // 6. Assert B: Check if the file physically exists in your target directory
            String savedPath = cd.getPhotoAttachment();
            if (savedPath != null) {
                File copiedFile = new File(savedPath);
                if (copiedFile.exists()) {
                    System.out.println("-> PASS: Real image physically verified in your project images folder!");

                    // Note: I'm leaving this delete line commented out so you can manually
                    // go to your OneDrive images folder and check that the real photo is there!
                    // copiedFile.delete();
                } else {
                    System.out
                            .println("-> FAIL: Path saved in model, but physical file is missing from target folder!");
                    testPassed = false;
                }
            } else {
                System.out.println("-> FAIL: Photo attachment path was not set in the model!");
                testPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred during test execution.");
            e.printStackTrace();
            testPassed = false;
        } finally {
            // Clean up the downloaded file from your project root
            if (realImageFile != null && realImageFile.exists()) {
                realImageFile.delete();
            }
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
    }

    // HELPER METHOD: Counts rows in the database table
    private static int getTableRowCount(Connection con, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("[Warning] Could not get count for " + tableName);
        }
        return 0;
    }
}