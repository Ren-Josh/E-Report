package config;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBCreate {

    /**
     * This method is used to create Database when it does not still exists to avoid
     * error on DB Connection
     * 
     * @params none
     * @return none
     */

    public static void createDatabase() {
        final String url = "jdbc:mysql://localhost:3306";
        final String username = "root";
        final String password = "";
        Statement statement = null;
        Connection con = null;
        String query, checkQuery;
        ResultSet rs = null;

        try {
            con = DriverManager.getConnection(url, username, password);

            statement = con.createStatement();
            checkQuery = "SHOW DATABASES LIKE 'e_report';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("Database e_report already exists!");
            } else {
                query = "CREATE DATABASE IF NOT EXISTS e_report;";
                statement.executeUpdate(query);
                System.out.println("Database E_REPORT has been created!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
