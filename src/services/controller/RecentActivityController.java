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
import features.core.dashboardpanel.captain.ActivityItem;
import models.UserSession;

public class RecentActivityController {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("h:mm a");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM-dd-yyyy");

    public List<ActivityItem> getRecentActivities(UserSession us, int limit) {
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
            List<Object[]> rows = dao.getRecentStatusUpdates(con, limit, cutoff);

            for (Object[] row : rows) {
                int cId = (Integer) row[0];
                String status = (String) row[1];
                java.sql.Timestamp ts = (java.sql.Timestamp) row[2];
                String role = (String) row[3];
                String userName = (String) row[4];
                Integer userId = (Integer) row[5];

                // Format: Role: Name (ID: value)
                String actor;
                if (role != null && userName != null && userId != null) {
                    actor = role + ": " + userName + " (ID: " + userId + ")";
                } else if (role != null && userName != null) {
                    actor = role + ": " + userName;
                } else {
                    actor = (role != null && !role.isEmpty()) ? role : "System";
                }

                String title = "Report Status Updated";
                String desc = actor + " updated Report #" + cId + " (ID: C" + cId + ") to " + status;
                String time = TIME_FMT.format(ts);
                String date = DATE_FMT.format(ts);

                activities.add(new ActivityItem(title, desc, time, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    private boolean isAuthorized(UserSession us) {
        String role = us.getRole();
        return role.equalsIgnoreCase("captain") || role.equalsIgnoreCase("secretary");
    }

    private void showForbiddenError() {
        JOptionPane.showMessageDialog(null,
                "Error: Forbidden access!",
                "Forbidden",
                JOptionPane.WARNING_MESSAGE);
    }
}