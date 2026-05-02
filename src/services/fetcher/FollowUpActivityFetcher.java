package services.fetcher;

import config.database.DBConnection;
import features.core.dashboardpanel.captain.panels.ActivityItem;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches follow-up request activities dynamically from the database.
 * Only returns requests from the last {@value #VISIBILITY_DAYS} days.
 */
public class FollowUpActivityFetcher {

    private static final int VISIBILITY_DAYS = 2;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final List<Runnable> dataChangeListeners = new ArrayList<>();

    public void addDataChangeListener(Runnable listener) {
        dataChangeListeners.add(listener);
    }

    /**
     * Queries the database for follow-up requests requested within the last 2 days.
     * Joins with User_Info, Credential, and Complaint_Detail to build the display
     * strings.
     *
     * @return List of ActivityItem ordered newest first.
     */
    public List<ActivityItem> fetchRecentActivities() {
        List<ActivityItem> activities = new ArrayList<>();

        Timestamp cutoff = Timestamp.from(
                Instant.now().minus(VISIBILITY_DAYS, ChronoUnit.DAYS));

        String sql = """
                SELECT
                    fur.request_date,
                    fur.CD_ID,
                    fur.UI_ID,
                    c.role,
                    CONCAT(COALESCE(ui.first_name, ''), ' ', COALESCE(ui.last_name, '')) AS full_name,
                    cd.type,
                    cd.subject
                FROM Follow_Up_Request fur
                JOIN User_Info ui ON fur.UI_ID = ui.UI_ID
                JOIN Credential c ON fur.UI_ID = c.UI_ID
                JOIN Complaint_Detail cd ON fur.CD_ID = cd.CD_ID
                WHERE fur.request_date >= ?
                ORDER BY fur.request_date DESC
                """;

        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, cutoff);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp reqDate = rs.getTimestamp("request_date");

                    String time = reqDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .format(TIME_FORMATTER);
                    String date = reqDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .format(DATE_FORMATTER);

                    String role = safe(rs.getString("role"));
                    String name = safe(rs.getString("full_name"));
                    int userId = rs.getInt("UI_ID");
                    int complaintId = rs.getInt("CD_ID");
                    String complaintType = safe(rs.getString("type"));
                    String complaintTitle = safe(rs.getString("subject"));

                    String description = String.format(
                            "%s: %s (ID: %d) has requested a follow up on Complaint (ID: %d): %s - %s",
                            role, name, userId, complaintId, complaintType, complaintTitle);

                    activities.add(new ActivityItem("Follow Up Request", description, time, date));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    /**
     * Convenience method to notify any registered listeners after a manual refresh.
     */
    public void notifyListeners() {
        for (Runnable listener : dataChangeListeners) {
            listener.run();
        }
    }

    private String safe(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }
}