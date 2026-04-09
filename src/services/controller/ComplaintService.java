package services.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import DAOs.AddComplaintDAO;
import config.DBConnection;
import models.ComplaintDetail;

public class ComplaintService {

    /**
     * Add Complaint for Frontend Use
     * 
     * @params int UI_ID user Id, ComplaintDetail cd complaint, File droppedFile for
     *         files
     * @return none
     */

    public void addComplaint(int UI_ID, ComplaintDetail cd, File droppedFile) {
        Connection con = null;

        try {
            if (droppedFile != null) {
                try {
                    processAndAttachImage(cd, droppedFile);
                } catch (Exception e) {
                    System.err
                            .println("Non-critical Error: Image failed to save, continuing with complaint submission.");
                    e.printStackTrace();
                }
            }

            con = DBConnection.connect();
            AddComplaintDAO.addComplaint(con, UI_ID, cd);
            System.out.println("Complaint successfully saved!");

        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Warning: Failed to close the database connection.");
                }
            }
        }
    }

    /**
     * Process the image and save it into specified directory
     * 
     * @params ComplaintDetail cd complaint, File droppedFile for
     *         files
     * @return none
     */

    public void processAndAttachImage(ComplaintDetail cd, File droppedFile) {
        try {
            // Read the file into a byte array
            byte[] fileBytes = Files.readAllBytes(droppedFile.toPath());

            // Attach the byte array to the ComplaintDetail model
            cd.setPhotoAttachmentBytes(fileBytes); // You need to add this field in your model

            System.out.println("Image successfully read and attached as BLOB: " + droppedFile.getName());

        } catch (IOException e) {
            System.err.println("Failed to read the image file!");
            e.printStackTrace();
        }
    }
}