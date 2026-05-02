package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.Credential;
import models.UserInfo;

/**
 * DAO for retrieving users and credentials.
 */
public class GetUserDao {

	// ===== SQL STRINGS =====
	private String queryUser;
	private String queryCredential;

	public GetUserDao() {
		queryUser = """
				SELECT UI_ID, first_name, middle_name, last_name, sex,
					contact_number, email_address, house_number, purok
				FROM User_Info
				WHERE UI_ID = ?;
				""";

		queryCredential = """
				SELECT UI_ID, username, password, role
				FROM Credential
				WHERE username = ? AND password = ?;
				""";
	}

	/**
	 * Retrieves a UserInfo by ID.
	 *
	 * @param con   Active DB connection
	 * @param UI_ID User ID
	 * @return UserInfo or null
	 */
	public UserInfo getUser(Connection con, int UI_ID) {
		try (PreparedStatement stmt = con.prepareStatement(queryUser)) {
			stmt.setInt(1, UI_ID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					UserInfo ui = new UserInfo();
					ui.setUI_ID(rs.getInt("UI_ID"));
					ui.setFName(rs.getString("first_name"));
					ui.setMName(rs.getString("middle_name"));
					ui.setLName(rs.getString("last_name"));
					ui.setSex(rs.getString("sex"));
					ui.setContact(rs.getString("contact_number"));
					ui.setEmail(rs.getString("email_address"));
					ui.setHouseNum(rs.getString("house_number"));
					ui.setPurok(rs.getString("purok"));
					return ui;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving user info from User_Info table");
			System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
		}
		return null;
	}

	/**
	 * Retrieves a Credential by username and password.
	 *
	 * @param con      Active DB connection
	 * @param username Username
	 * @param password Password
	 * @return Credential or null
	 */
	public Credential getCredential(Connection con, String username, String password) {
		try (PreparedStatement stmt = con.prepareStatement(queryCredential)) {
			stmt.setString(1, username);
			stmt.setString(2, password);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Credential c = new Credential();
					c.setUI_ID(rs.getInt("UI_ID"));
					c.setUsername(rs.getString("username"));
					c.setPassword(rs.getString("password"));
					c.setRole(rs.getString("role"));
					return c;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving credential from Credential table");
			System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
		}
		return null;
	}
}