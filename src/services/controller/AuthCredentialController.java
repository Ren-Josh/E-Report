package services.controller;

import daos.GetUserDao;
import config.database.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import models.Credential;
import models.UserSession;

/**
 * Handles user authentication by validating credentials against the database.
 */
public class AuthCredentialController {

	// ===== DAO INSTANCE =====
	private GetUserDao userDAO;

	public AuthCredentialController() {
		userDAO = new GetUserDao();
	}

	/**
	 * Authenticates a user against the database credentials.
	 * 
	 * @param username The username of the user attempting to log in
	 * @param password The password corresponding to the username
	 * @return UserSession object if authentication succeeds; null otherwise
	 */
	public UserSession authenticateUser(String username, String password) {
		try (Connection con = DBConnection.connect()) {
			Credential credential = userDAO.getCredential(con, username, password);

			if (credential != null) {
				return new UserSession(
						credential.getUI_ID(),
						credential.getRole());
			}
		} catch (SQLException e) {
			System.err.println("Authentication error: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}