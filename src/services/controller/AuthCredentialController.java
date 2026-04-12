package services.controller;

import DAOs.GetUserDAO;
import models.Credential;
import models.UserSession;

/**
 * AuthCredential
 * 
 * This service class handles user authentication by validating credentials
 * against the database. It provides functionality to verify a user's username
 * and password and returns a session object if authentication is successful.
 * 
 * Responsibilities:
 * - Authenticate users using their username and password.
 * - Return a UserSession object containing user ID, role, and verification
 * status.
 * 
 * Usage:
 * - Call authenticateUser() with the username and password.
 * - Returns null if the credentials are invalid or missing.
 */
public class AuthCredentialController {

    /**
     * Authenticates a user against the database credentials.
     * 
     * Process:
     * 1. Use GetUserDAO to retrieve the Credential object from the database.
     * 2. If a valid Credential is found, create and return a UserSession object.
     * 3. Return null if authentication fails or credentials are missing.
     * 
     * @param username The username of the user attempting to log in.
     * @param password The password corresponding to the username.
     * @return A UserSession object containing the user's ID, role, and verification
     *         status
     *         if authentication succeeds; null otherwise.
     */
    public static UserSession authenticateUser(String username, String password) {

        GetUserDAO userDAO = new GetUserDAO();
        Credential credential = userDAO.getCredential(username, password);

        if (credential != null) {
            return new UserSession(
                    credential.getUI_ID(), // User ID
                    credential.getRole(), // User role
                    credential.getIsVerified() // Verification status
            );
        }

        return null;
    }
}