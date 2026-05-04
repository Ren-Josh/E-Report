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
 * Only returns requests from the last VISIBILITY_DAYS days.
 */
public class FollowUpActivityFetcher {

    /** Number of days back from now to include in the activity feed. */
    private static final int VISIBILITY_DAYS = 2;

    /**
     * Formatter for the time portion of an activity timestamp (e.g., "02:30 PM").
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    /**
     * Formatter for the date portion of an activity timestamp (e.g., "Jan 15,
     * 2024").
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Registered callbacks invoked when the dataset changes (e.g., after a
     * refresh).
     */
    private final List<Runnable> dataChangeListeners = new ArrayList<>();

    /**
     * Registers a callback to be invoked whenever the fetcher notifies listeners.
     * Typically used by UI panels to trigger a repaint or re-fetch.
     * 
     * @param listener a no-argument Runnable executed on the caller's thread
     */
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

        // Compute the cutoff timestamp: anything before this is excluded.
        Timestamp cutoff = Timestamp.from(
                Instant.now().minus(VISIBILITY_DAYS, ChronoUnit.DAYS));

        // SQL joins four tables to resolve user names, roles, and complaint details.
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

                    // Split the SQL timestamp into human-readable time and date strings.
                    String time = reqDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .format(TIME_FORMATTER);
                    String date = reqDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .format(DATE_FORMATTER);

                    // Extract and sanitize all fields from the joined result set.
                    String role = safe(rs.getString("role"));
                    String name = safe(rs.getString("full_name"));
                    int userId = rs.getInt("UI_ID");
                    int complaintId = rs.getInt("CD_ID");
                    String complaintType = safe(rs.getString("type"));
                    String complaintTitle = safe(rs.getString("subject"));

                    // Build a single-line description summarizing the follow-up request.
                    String description = String.format(
                            "%s: %s (ID: %d) has requested a follow up on Complaint (ID: %d): %s - %s",
                            role, name, userId, complaintId, complaintType, complaintTitle);

                    activities.add(new ActivityItem("Follow Up Request", description, time, date));
                }
            }
        } catch (SQLException e) {
            // Log the error but return whatever was collected so the UI does not hang.
            e.printStackTrace();
        }

        return activities;
    }

    /**
     * Convenience method to notify any registered listeners after a manual refresh.
     * Iterates the listener list and invokes each Runnable in sequence.
     */
    public void notifyListeners() {
        for (Runnable listener : dataChangeListeners) {
            listener.run();
        }
    }

    /**
     * Sanitizes a database string to prevent null or blank values in the UI.
     * 
     * @param value the raw string from the ResultSet
     * @return the original value if non-null and non-blank; otherwise an em-dash
     *         placeholder
     */
    private String safe(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }
}