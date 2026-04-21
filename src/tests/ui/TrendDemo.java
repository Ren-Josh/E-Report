package tests.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrendDemo extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JLabel lblTrend;
    private JLabel lblRange;

    public TrendDemo() {
        setTitle("Complaint Trends (Type + Time)");
        setSize(650, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== HEADER =====
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        lblTrend = new JLabel("Trend:");
        lblRange = new JLabel("Range:");

        lblTrend.setFont(new Font("Arial", Font.BOLD, 16));
        lblRange.setFont(new Font("Arial", Font.PLAIN, 14));

        topPanel.add(lblTrend);
        topPanel.add(lblRange);

        add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[] { "Time", "Type", "Total" }, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        setupDatabase();
        loadData();
    }

    // ================= DATABASE SETUP =================
    private Connection connect() throws Exception {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/complaint_db", "root", "");
    }

    private void setupDatabase() {
        try (Connection con = connect();
                Statement stmt = con.createStatement()) {

            // Create table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS Complaint (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            type VARCHAR(50),
                            status VARCHAR(50),
                            created_at DATETIME
                        )
                    """);

            // Clear old data (so demo is consistent)
            stmt.execute("DELETE FROM Complaint");

            // Insert sample data
            stmt.execute("""
                        INSERT INTO Complaint (type, status, created_at) VALUES
                        ('Noise', 'Pending', '2024-01-02'),
                        ('Noise', 'Resolved', '2024-01-03'),
                        ('Garbage', 'Resolved', '2024-01-05'),
                        ('Water', 'Pending', '2024-02-01'),
                        ('Electric', 'Resolved', '2024-02-03'),
                        ('Noise', 'Pending', '2024-02-10'),
                        ('Garbage', 'Pending', '2024-03-01'),
                        ('Garbage', 'Resolved', '2024-03-02'),
                        ('Noise', 'Resolved', '2024-03-03'),
                        ('Water', 'Pending', '2024-03-04'),
                        ('Electric', 'Resolved', '2024-03-05'),
                        ('Noise', 'Pending', '2024-03-06'),
                        ('Noise', 'Resolved', '2024-04-01'),
                        ('Garbage', 'Resolved', '2024-04-02'),
                        ('Water', 'Pending', '2024-04-03'),
                        ('Electric', 'Resolved', '2024-04-04')
                    """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD TREND =================
    private void loadData() {
        try (Connection con = connect()) {

            String trendType = "MONTH"; // YEAR, MONTH, DAY
            String start = "2024-01-01";
            String end = "2024-12-31";

            lblTrend.setText("Trend: " + getTrendName(trendType) + " (by Type)");
            lblRange.setText("Range: " + formatRange(start, end));

            loadTrendWithType(con, trendType, start, end);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TREND QUERY =================
    private void loadTrendWithType(Connection con, String groupBy, String start, String end) {

        String groupPart = switch (groupBy) {
            case "YEAR" -> "YEAR(created_at)";
            case "MONTH" -> "MONTH(created_at)";
            case "DAY" -> "DAY(created_at)";
            default -> "DATE(created_at)";
        };

        String sql = "SELECT " + groupPart + " AS label, type, COUNT(*) AS total " +
                "FROM Complaint " +
                "WHERE created_at BETWEEN ? AND ? " +
                "GROUP BY label, type " +
                "ORDER BY label, type";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();

            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[] {
                        formatLabel(groupBy, rs.getInt("label")),
                        rs.getString("type"),
                        rs.getInt("total")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FORMAT LABEL =================
    private String formatLabel(String type, int value) {
        if ("MONTH".equals(type)) {
            return new java.text.DateFormatSymbols().getMonths()[value - 1];
        }
        return String.valueOf(value);
    }

    // ================= HELPERS =================
    private String getTrendName(String type) {
        return switch (type) {
            case "YEAR" -> "Yearly Trend";
            case "MONTH" -> "Monthly Trend";
            case "DAY" -> "Daily Trend";
            default -> "Custom Trend";
        };
    }

    private String formatRange(String start, String end) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy");

            Date d1 = input.parse(start);
            Date d2 = input.parse(end);

            return output.format(d1) + " - " + output.format(d2);
        } catch (Exception e) {
            return start + " - " + end;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrendDemo().setVisible(true));
    }
}