package services.controller;

import DAOs.AddUserDAO;
import config.DBConnection;
import models.Credential;
import models.UserInfo;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    /**
     * Registers a new user with credentials as a single transaction.
     * 
     * @return true if both operations succeeded.
     */
    public String registerUser(UserInfo ui, Credential c) {
        // 1. Check if username is taken
        if (AddUserDAO.isUsernameTaken(c.getUsername())) {
            return "Username is already taken.";
        }

        // 2. Determine Role based on database count
        int existingUsers = AddUserDAO.getUserCount();
        if (existingUsers == 0) {
            c.setRole("Secretary");
            c.setIsVerified(true); // Auto-verify the first user (optional)
        } else {
            c.setRole("Resident");
            c.setIsVerified(false);
        }

        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false); // Start Transaction

            // Insert User Info
            int userId = AddUserDAO.addUser(con, ui);
            if (userId == -1)
                throw new SQLException("Failed to save user profile.");

            // Insert Credentials with the assigned role
            boolean credSuccess = AddUserDAO.addCredential(con, userId, c);

            if (credSuccess) {
                con.commit();
                return "SUCCESS";
            } else {
                con.rollback();
                return "Failed to create credentials.";
            }

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                }
            }
            return "Database error: " + e.getMessage();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }
}