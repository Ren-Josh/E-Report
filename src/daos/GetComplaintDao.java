package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

public class GetComplaintDao {

	private String queryComplaint, queryAllComplaints, queryHistory, queryAction, queryResidentRecentComplaint,
			queryAllComplaintsNoFilter;

	public GetComplaintDao() {
		// Single complaint – now returns date_time_created as `date_time` alias
		queryComplaint = """
				SELECT
				    cd.CD_ID,
				    cd.current_status,
				    cd.subject,
				    cd.type,
				    cd.date_time_created AS date_time,    -- <-- use creation date
				    cd.street,
				    cd.purok,
				    cd.longitude,
				    cd.latitude,
				    cd.persons_involved,
				    cd.details,
				    cd.photo_attachment
				FROM Complaint c
				INNER JOIN Complaint_Detail cd ON cd.CD_ID = c.CD_ID
				WHERE c.UI_ID = ? AND cd.CD_ID = ?;
				""";

		// All complaints for a specific user
		queryAllComplaints = """
				SELECT
				    cd.CD_ID,
				    cd.current_status,
				    cd.subject,
				    cd.type,
				    cd.date_time_created AS date_time,   -- corrected
				    cd.street,
				    cd.purok,
				    cd.longitude,
				    cd.latitude,
				    cd.persons_involved,
				    cd.details,
				    cd.photo_attachment,
				    CASE
				        WHEN cd.current_status = 'Pending' THEN cd.date_time_created
				        ELSE latest_chd.latest_dateTimeUpdated
				    END AS last_update
				FROM Complaint_Detail cd
				INNER JOIN Complaint c ON c.CD_ID = cd.CD_ID
				LEFT JOIN (
				    SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated
				    FROM Complaint_History ch
				    INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID
				    GROUP BY ch.CD_ID
				) latest_chd ON latest_chd.CD_ID = cd.CD_ID
				WHERE c.UI_ID = ?;
				""";

		// Recent complaints for a resident dashboard
		queryResidentRecentComplaint = """
				SELECT
				    cd.CD_ID,
				    cd.current_status,
				    cd.subject,
				    cd.type,
				    cd.date_time_created AS date_time,   -- corrected
				    cd.street,
				    cd.purok,
				    cd.longitude,
				    cd.latitude,
				    cd.persons_involved,
				    cd.details,
				    cd.photo_attachment,
				    CASE
				        WHEN cd.current_status = 'Pending' THEN cd.date_time_created
				        ELSE latest_chd.latest_dateTimeUpdated
				    END AS last_update
				FROM Complaint_Detail cd
				INNER JOIN Complaint c ON c.CD_ID = cd.CD_ID
				LEFT JOIN (
				    SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated
				    FROM Complaint_History ch
				    INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID
				    GROUP BY ch.CD_ID
				) latest_chd ON latest_chd.CD_ID = cd.CD_ID
				WHERE c.UI_ID = ?
				ORDER BY cd.date_time_created DESC
				LIMIT ?;
				""";

		// All complaints (used by AllReportsFetcher)
		queryAllComplaintsNoFilter = """
				SELECT
				    cd.CD_ID,
				    cd.current_status,
				    cd.subject,
				    cd.type,
				    cd.date_time_created AS date_time,   -- corrected
				    cd.street,
				    cd.purok,
				    cd.longitude,
				    cd.latitude,
				    cd.persons_involved,
				    cd.details,
				    cd.photo_attachment,
				    CASE
				        WHEN cd.current_status = 'Pending' THEN cd.date_time_created
				        ELSE latest_chd.latest_dateTimeUpdated
				    END AS last_update
				FROM Complaint_Detail cd
				INNER JOIN Complaint c ON c.CD_ID = cd.CD_ID
				LEFT JOIN (
				    SELECT ch.CD_ID, MAX(chd.date_time_updated) AS latest_dateTimeUpdated
				    FROM Complaint_History ch
				    INNER JOIN Complaint_History_Detail chd ON chd.CHD_ID = ch.CHD_ID
				    GROUP BY ch.CD_ID
				) latest_chd ON latest_chd.CD_ID = cd.CD_ID
				ORDER BY cd.date_time_created DESC;
				""";

		queryHistory = """
				SELECT chd.CHD_ID, chd.status, chd.process, chd.date_time_updated, chd.updated_by
				FROM Complaint_History_Detail chd
				INNER JOIN Complaint_History ch ON ch.CHD_ID = chd.CHD_ID
				WHERE ch.CD_ID = ?;
				""";

		queryAction = """
				SELECT CD_ID, action_taken, recommendation, oic,
				    date_time_assigned, resolution_date_time
				FROM Complaint_Action
				WHERE CD_ID = ?;
				""";
	}

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

		cd.setLastUpdateTimestamp(rs.getTimestamp("last_update"));

		return cd;
	}

	private ComplaintHistoryDetail mapToHistoryDetail(ResultSet rs) throws SQLException {
		ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
		chd.setStatus(rs.getString("status"));
		chd.setProcess(rs.getString("process"));
		chd.setDateTimeUpdated(rs.getTimestamp("date_time_updated"));
		chd.setUpdatedBy(rs.getString("updated_by"));
		return chd;
	}

	public ComplaintDetail getComplaint(Connection con, int UI_ID, int CD_ID) {
		try (PreparedStatement stmt = con.prepareStatement(queryComplaint)) {
			stmt.setInt(1, UI_ID);
			stmt.setInt(2, CD_ID);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapToComplaintDetail(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving complaint detail from Complaint_Detail table");
			e.printStackTrace();
		}
		return null;
	}

	public List<ComplaintDetail> getAllComplaint(Connection con, int UI_ID) {
		List<ComplaintDetail> cdList = new ArrayList<>();

		try (PreparedStatement stmt = con.prepareStatement(queryAllComplaints)) {
			stmt.setInt(1, UI_ID);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					cdList.add(mapToComplaintDetail(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving all complaints for UI_ID: " + UI_ID);
			e.printStackTrace();
		}
		return cdList;
	}

	public List<ComplaintDetail> getAllComplaints(Connection con) {
		List<ComplaintDetail> cdList = new ArrayList<>();

		try (PreparedStatement stmt = con.prepareStatement(queryAllComplaintsNoFilter)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					cdList.add(mapToComplaintDetail(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving all complaints");
			e.printStackTrace();
		}
		return cdList;
	}

	public List<ComplaintDetail> getRecentComplaint(Connection con, int UI_ID, int limit) {
		List<ComplaintDetail> cdList = new ArrayList<>();

		try (PreparedStatement stmt = con.prepareStatement(queryResidentRecentComplaint)) {
			stmt.setInt(1, UI_ID);
			stmt.setInt(2, limit);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					cdList.add(mapToComplaintDetail(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving recent complaints for UI_ID: " + UI_ID);
			e.printStackTrace();
		}
		return cdList;
	}

	public List<ComplaintHistoryDetail> getComplaintHistory(Connection con, int CD_ID) {
		List<ComplaintHistoryDetail> chdList = new ArrayList<>();

		try (PreparedStatement stmt = con.prepareStatement(queryHistory)) {
			stmt.setInt(1, CD_ID);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					chdList.add(mapToHistoryDetail(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving complaint history for CD_ID: " + CD_ID);
			e.printStackTrace();
		}
		return chdList;
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
}