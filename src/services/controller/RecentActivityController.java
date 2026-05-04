package services.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;

import config.database.DBConnection;
import daos.RecentActivityDao;
import features.core.dashboardpanel.captain.panels.ActivityItem;
import models.UserSession;

/**
 * Controller for retrieving recent activity feed data.
 * Fetches status update history from the database, formats it into
 * human-readable ActivityItem objects, and enforces role-based access control.
 */
public class RecentActivityController {

    /**
     * Formatter for the time portion of an activity timestamp (e.g., "2:30 PM").
     */
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("h:mm a");
    /**
     * Formatter for the date portion of an activity timestamp (e.g., "05-04-2026").
     */
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM-dd-yyyy");

    /**
     * Retrieves the most recent status update activities for the activity feed.
     * Only Captain and Secretary roles are authorized to view this data.
     * 
     * @param us    the current user session (used for authorization)
     * @param limit maximum number of activity items to return
     * @return a list of ActivityItem objects ordered by timestamp; empty list if
     *         unauthorized or on error
     */
    public List<ActivityItem> getRecentActivities(UserSession us, int limit) {
        // Enforce role-based access control before touching the database.
        if (!isAuthorized(us)) {
            showForbiddenError();
            return new ArrayList<>();
        }

        RecentActivityDao dao = new RecentActivityDao();
        List<ActivityItem> activities = new ArrayList<>();

        // Calculate cutoff: 2 days ago at midnight (00:00:00)
        // Dynamically based on current date — always rolls with the calendar
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -2); // go back 2 days
        cal.set(Calendar.HOUR_OF_DAY, 0); // midnight
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        java.sql.Timestamp cutoff = new java.sql.Timestamp(cal.getTimeInMillis());

        try (Connection con = DBConnection.connect()) {
            // Pull raw rows from the DAO; each row contains complaint ID, status,
            // timestamp, role, user name, and user ID.
            List<Object[]> rows = dao.getRecentStatusUpdates(con, limit, cutoff);

            for (Object[] row : rows) {
                int cId = (Integer) row[0];
                String status = (String) row[1];
                java.sql.Timestamp ts = (java.sql.Timestamp) row[2];
                String role = (String) row[3];
                String userName = (String) row[4];
                Integer userId = (Integer) row[5];

                // Build the actor string: Role: Name (ID: value)
                // Gracefully degrade if some fields are missing.
                String actor;
                if (role != null && userName != null && userId != null) {
                    actor = role + ": " + userName + " (ID: " + userId + ")";
                } else if (role != null && userName != null) {
                    actor = role + ": " + userName;
                } else {
                    actor = (role != null && !role.isEmpty()) ? role : "System";
                }

                // Compose the human-readable activity description.
                String title = "Report Status Updated";
                String desc = actor + " updated Report #" + cId + " (ID: C" + cId + ") to " + status;
                String time = TIME_FMT.format(ts);
                String date = DATE_FMT.format(ts);

                activities.add(new ActivityItem(title, desc, time, date));
            }
        } catch (SQLException e) {
            // Log the error but return whatever was collected so the UI does not hang.
            e.printStackTrace();
        }

        return activities;
    }

    /**
     * Checks whether the given user session has a role authorized to view
     * the recent activity feed. Only Captain and Secretary are allowed.
     * 
     * @param us the user session to check
     * @return true if the role is Captain or Secretary; false otherwise
     */
    private boolean isAuthorized(UserSession us) {
        String role = us.getRole();
        return role.equalsIgnoreCase("captain") || role.equalsIgnoreCase("secretary");
    }

    /**
     * Displays a modal warning dialog when an unauthorized user attempts
     * to access the recent activity data.
     */
    private void showForbiddenError() {
        JOptionPane.showMessageDialog(null,
                "Error: Forbidden access!",
                "Forbidden",
                JOptionPane.WARNING_MESSAGE);
    }
}