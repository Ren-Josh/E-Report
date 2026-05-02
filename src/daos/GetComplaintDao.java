package daos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

/**
 * DAO for retrieving complaints, history, and actions.
 */
public class GetComplaintDao {

	// ===== SQL STRINGS =====
	private String queryComplaint;
	private String queryAllComplaints;
	private String queryResidentRecent;
	private String queryAllNoFilter;
	private String queryHistory;
	private String queryAction;

	// ===== REUSABLE FRAGMENTS =====
	private static final String COMPLAINT_COLUMNS = """
			cd.CD_ID, cd.current_status, cd.subject, cd.type,
			cd.date_time_created AS date_time, cd.street, cd.purok,
			cd.longitude, cd.latitude, cd.persons_involved, cd.details,
			cd.photo_attachment
			""";

	private static final String LAST_UPDATE_SUBQUERY = """
			LEFT JOIN (
			    SELECT CD_ID, MAX(date_time_updated) AS latest_dateTimeUpdated
			    FROM Complaint_History_Detail
			    GROUP BY CD_ID
			) latest_chd ON latest_chd.CD_ID = cd.CD_ID
			""";

	private static final String LAST_UPDATE_CASE = """
			CASE
			    WHEN cd.current_status = 'Pending' THEN cd.date_time_created
			    ELSE latest_chd.latest_dateTimeUpdated
			END AS last_update
			""";

	public GetComplaintDao() {
		// ===== INIT SQL =====
		queryComplaint = buildBaseSelect("WHERE cd.UI_ID = ? AND cd.CD_ID = ?");
		queryAllComplaints = buildBaseSelectWithLastUpdate("WHERE cd.UI_ID = ?");
		queryResidentRecent = buildBaseSelectWithLastUpdate(
				"WHERE cd.UI_ID = ? ORDER BY cd.date_time_created DESC LIMIT ?");
		queryAllNoFilter = buildBaseSelectWithLastUpdate("ORDER BY cd.date_time_created DESC");

		queryHistory = """
				SELECT CHD_ID, status, process, date_time_updated, updated_by
				FROM Complaint_History_Detail
				WHERE CD_ID = ?;
				""";

		queryAction = """
				SELECT CD_ID, action_taken, recommendation, oic,
				    date_time_assigned, resolution_date_time
				FROM Complaint_Action
				WHERE CD_ID = ?;
				""";
	}

	// ===== QUERY BUILDERS =====
	private String buildBaseSelect(String where) {
		return "SELECT " + COMPLAINT_COLUMNS + " FROM Complaint_Detail cd " + where;
	}

	private String buildBaseSelectWithLastUpdate(String where) {
		return "SELECT " + COMPLAINT_COLUMNS + ", " + LAST_UPDATE_CASE +
				" FROM Complaint_Detail cd " +
				LAST_UPDATE_SUBQUERY + " " + where;
	}

	// ===== MAPPERS =====
	private ComplaintDetail mapToComplaintDetail(ResultSet rs) throws SQLException {
		ComplaintDetail cd = new ComplaintDetail();
		cd.setComplaintId(rs.getInt("CD_ID"));
		cd.setCurrentStatus(rs.getString("current_status"));
		cd.setSubject(rs.getString("subject"));
		cd.setType(rs.getString("type"));
		cd.setDateTime(rs.getTimestamp("date_time"));
		cd.setStreet(rs.getString("street"));
		cd.setPurok(rs.getString("purok"));
		cd.setLongitude(rs.getDouble("longitude"));
		cd.setLatitude(rs.getDouble("latitude"));
		cd.setPersonsInvolved(rs.getString("persons_involved"));
		cd.setDetails(rs.getString("details"));
		cd.setPhotoAttachmentBytes(rs.getBytes("photo_attachment"));

		try {
			cd.setLastUpdateTimestamp(rs.getTimestamp("last_update"));
		} catch (SQLException ignored) {
		}

		return cd;
	}

	private ComplaintHistoryDetail mapToHistoryDetail(ResultSet rs) throws SQLException {
		ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
		chd.setStatus(rs.getString("status"));
		chd.setProcess(rs.getString("process"));
		chd.setDateTimeUpdated(rs.getTimestamp("date_time_updated"));
		chd.setUpdatedBy(rs.getInt("updated_by"));
		return chd;
	}

	// ===== PUBLIC METHODS =====
	public ComplaintDetail getComplaint(Connection con, int UI_ID, int CD_ID) {
		return executeSingle(con, queryComplaint, UI_ID, CD_ID);
	}

	public List<ComplaintDetail> getAllComplaint(Connection con, int UI_ID) {
		return executeList(con, queryAllComplaints, UI_ID);
	}

	public List<ComplaintDetail> getAllComplaints(Connection con) {
		return executeList(con, queryAllNoFilter);
	}

	public List<ComplaintDetail> getRecentComplaint(Connection con, int UI_ID, int limit) {
		return executeList(con, queryResidentRecent, UI_ID, limit);
	}

	public List<ComplaintHistoryDetail> getComplaintHistory(Connection con, int CD_ID) {
		List<ComplaintHistoryDetail> list = new ArrayList<>();
		try (PreparedStatement stmt = con.prepareStatement(queryHistory)) {
			stmt.setInt(1, CD_ID);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					list.add(mapToHistoryDetail(rs));
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving complaint history for CD_ID: " + CD_ID);
			e.printStackTrace();
		}
		return list;
	}

	public ComplaintAction getComplaintAction(Connection con, int CD_ID) {
		try (PreparedStatement stmt = con.prepareStatement(queryAction)) {
			stmt.setInt(1, CD_ID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					ComplaintAction ca = new ComplaintAction();
					ca.setActionTaken(rs.getString("action_taken"));
					ca.setRecommendation(rs.getString("recommendation"));
					ca.setOIC(rs.getString("oic"));
					ca.setDateTimeAssigned(rs.getTimestamp("date_time_assigned"));
					ca.setResolutionDateTime(rs.getTimestamp("resolution_date_time"));
					return ca;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving complaint action for CD_ID: " + CD_ID);
			e.printStackTrace();
		}
		return null;
	}

	// ===== EXECUTION HELPERS =====
	private ComplaintDetail executeSingle(Connection con, String sql, Object... params) {
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? mapToComplaintDetail(rs) : null;
			}
		} catch (SQLException e) {
			System.err.println("Error in executeSingle");
			e.printStackTrace();
			return null;
		}
	}

	private List<ComplaintDetail> executeList(Connection con, String sql, Object... params) {
		List<ComplaintDetail> list = new ArrayList<>();
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					list.add(mapToComplaintDetail(rs));
			}
		} catch (SQLException e) {
			System.err.println("Error in executeList");
			e.printStackTrace();
		}
		return list;
	}
}