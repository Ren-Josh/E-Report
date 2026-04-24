package services.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        try (Connection con = DBConnection.connect()) {
            List<Object[]> rows = dao.getRecentStatusUpdates(con, limit);

            for (Object[] row : rows) {
                int cId = (Integer) row[0];
                String status = (String) row[1];
                java.sql.Timestamp ts = (java.sql.Timestamp) row[2];
                String updatedBy = (String) row[3];

                String actor = (updatedBy != null && !updatedBy.isEmpty()) ? updatedBy : "System";
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