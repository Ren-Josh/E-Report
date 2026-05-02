package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import config.AppConfig;
import models.Credential;
import models.UserInfo;

/**
 * DAO for adding users and credentials.
 */
public class AddUserDao {

	// ===== SQL STRINGS =====
	private String queryUserInfo;
	private String queryCredential;
	private String queryCheckUser;
	private String queryCount;

	public AddUserDao() {
		queryUserInfo = """
				INSERT INTO %s(first_name, middle_name, last_name, sex,
					contact_number, email_address, house_number, purok)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?);
				""".formatted(AppConfig.TABLE_USER_INFO);

		queryCredential = """
				INSERT INTO %s(UI_ID, username, password, role)
				VALUES (?, ?, ?, ?);
				""".formatted(AppConfig.TABLE_CREDENTIAL);

		queryCheckUser = "SELECT COUNT(*) FROM %s WHERE username = ?".formatted(AppConfig.TABLE_CREDENTIAL);

		queryCount = "SELECT COUNT(*) FROM %s".formatted(AppConfig.TABLE_CREDENTIAL);
	}

	/**
	 * Inserts a new user into User_Info.
	 *
	 * @param con Active DB connection
	 * @param ui  UserInfo object
	 * @return Generated UI_ID, or -1 on failure
	 * @throws SQLException if insertion fails
	 */
	public int addUser(Connection con, UserInfo ui) throws SQLException {
		try (PreparedStatement stmt = con.prepareStatement(queryUserInfo, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, ui.getFName());
			stmt.setString(2, ui.getMName());
			stmt.setString(3, ui.getLName());
			stmt.setString(4, ui.getSex());
			stmt.setString(5, ui.getContact());
			stmt.setString(6, ui.getEmail());
			stmt.setString(7, ui.getHouseNum());
			stmt.setString(8, ui.getPurok());

			stmt.executeUpdate();

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getInt(1);
				}
			}
		}
		return -1;
	}

	/**
	 * Inserts credential record for a user.
	 *
	 * @param con    Active DB connection
	 * @param userID User ID
	 * @param c      Credential object
	 * @return true if inserted
	 * @throws SQLException if insertion fails
	 */
	public boolean addCredential(Connection con, int userID, Credential c) throws SQLException {
		try (PreparedStatement stmt = con.prepareStatement(queryCredential)) {
			stmt.setInt(1, userID);
			stmt.setString(2, c.getUsername());
			stmt.setString(3, c.getPassword());
			stmt.setString(4, c.getRole());

			return stmt.executeUpdate() > 0;
		}
	}

	/**
	 * Checks if a username is already taken.
	 *
	 * @param con      Active DB connection
	 * @param username Username to check
	 * @return true if taken
	 */
	public boolean isUsernameTaken(Connection con, String username) {
		try (PreparedStatement stmt = con.prepareStatement(queryCheckUser)) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Counts total credentials in the system.
	 *
	 * @param con Active DB connection
	 * @return User count
	 */
	public int getUserCount(Connection con) {
		int userCount = 0;
		try (PreparedStatement stmt = con.prepareStatement(queryCount);
				ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				userCount = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userCount;
	}
}