package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    /**
     * This method is used to create a connection link on the Database
     * This is where the username and password of SQL shall be changed and edited to
     * change directory
     * 
     * @params none
     * @return connection if succesfully linked, null if error
     *         error sql state reference //
     *         https://dev.mysql.com/doc/connector-j/en/connector-j-reference-error-sqlstates.html
     */

    public static Connection connect() {
        final String username = "root";
        final String password = "";
        final String url = "jdbc:mysql://localhost:3306/e_report";

        String message, sqlState;

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            sqlState = e.getSQLState();
            message = e.getMessage();

            if ("3D000".equals(sqlState)) {
                System.out.println("ERROR: Database does not exist.");

            } else if ("28000".equals(sqlState)) {
                System.out.println("ERROR: Invalid credentials.");

            } else if ("08S01".equals(sqlState)) {
                System.out.println("ERROR: Database server is offline.");

            } else {
                System.out.println("ERROR: " + message);
            }

            e.printStackTrace();
            return null;
        }
    }
}
