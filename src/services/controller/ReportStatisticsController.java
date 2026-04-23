package services.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import config.database.DBConnection;
import daos.ReportStatisticsDao;
import models.UserSession;

public class ReportStatisticsController {
    public int countTotalReportByUser(UserSession us) {
        ReportStatisticsDao rsd = new ReportStatisticsDao();
        int totalReports;

        try (Connection con = DBConnection.connect();) {
            totalReports = rsd.countTotalReportByUser(con, us.getUserId());
            return totalReports;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countTotalReport(UserSession us) {
        ReportStatisticsDao rsd = new ReportStatisticsDao();
        int totalReports;

        if (us.getRole().equalsIgnoreCase("captain") || us.getRole().equalsIgnoreCase("secretary")) {
            try (Connection con = DBConnection.connect();) {
                totalReports = rsd.countTotalReport(con);
                return totalReports;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error: Forbidden access!",
                    "Forbidden",
                    JOptionPane.WARNING_MESSAGE);
        }
        return -1;
    }

    public int countTotalReportByDate(UserSession us, String start, String end) {
        ReportStatisticsDao rsd = new ReportStatisticsDao();
        int totalReports;

        if (us.getRole().equalsIgnoreCase("captain") || us.getRole().equalsIgnoreCase("secretary")) {
            try (Connection con = DBConnection.connect();) {
                totalReports = rsd.countTotalReportByDate(con, start, end);
                return totalReports;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error: Forbidden access!",
                    "Forbidden",
                    JOptionPane.WARNING_MESSAGE);
        }
        return -1;
    }

    public int countTotalReportByStatus(UserSession us, String status) {
        ReportStatisticsDao rsd = new ReportStatisticsDao();
        int totalReports;

        if (us.getRole().equalsIgnoreCase("captain") || us.getRole().equalsIgnoreCase("secretary")) {
            try (Connection con = DBConnection.connect();) {
                totalReports = rsd.countTotalReportByStatus(con, status);
                return totalReports;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error: Forbidden access!",
                    "Forbidden",
                    JOptionPane.WARNING_MESSAGE);
        }
        return -1;
    }

    public int countTotalReportByUserAndStatus(UserSession us, String status) {
        ReportStatisticsDao rsd = new ReportStatisticsDao();
        int totalReports;

        try (Connection con = DBConnection.connect();) {
            totalReports = rsd.countTotalReportByUserAndStatus(con, us.getUserId(), status);
            return totalReports;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int countTotalReportByRole(UserSession us) {
        ReportStatisticsDao rsd = new ReportStatisticsDao();
        int totalReports;

        if (us.getRole().equalsIgnoreCase("captain") || us.getRole().equalsIgnoreCase("secretary")) {
            try (Connection con = DBConnection.connect();) {
                totalReports = rsd.countTotalReportByStatus(con, us.getRole());
                return totalReports;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error: Forbidden access!",
                    "Forbidden",
                    JOptionPane.WARNING_MESSAGE);
        }
        return -1;
    }

}
