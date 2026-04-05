package config;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * This class is used to create all the Tables needed when it does not still
 * exists to avoid error on Database
 * 
 * all methods list
 * createUserInfoTable(Connection con)
 * createCredentialTable(Connection con);
 * createComplaintTable(Connection con);
 * createComplaintActionTable(Connection con);
 * createComplaintHistoryTable(Connection con);
 * 
 * @params Connection con
 * @return none
 * 
 */

public class TBCreate {

    public static void createTables(Connection con) {
        createUserInfoTable(con);
        createCredentialTable(con);
        createComplaintTable(con);
        createComplaintActionTable(con);
        createComplaintHistoryTable(con);
    }

    public static void createCredentialTable(Connection con) {
        Statement statement = null;
        String query = null, checkQuery = null;
        ResultSet rs = null;

        try {
            statement = con.createStatement();
            checkQuery = "SHOW TABLES LIKE 'Credential';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("Credential table already exists!");
            } else {
                query = "CREATE TABLE IF NOT EXISTS Credential("
                        + "Cred_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                        + "UI_ID INT NOT NULL,"
                        + "username VARCHAR(50) UNIQUE NOT NULL,"
                        + "password VARCHAR(50) NOT NULL,"
                        + "role VARCHAR(20) NOT NULL,"
                        + "is_verified BOOLEAN NOT NULL,"
                        + "date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                        + "FOREIGN KEY (UI_ID) REFERENCES User_Info(UI_ID)"
                        + " );";
                statement.executeUpdate(query);
                System.out.println("Table Credential has been created!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
                if (query != null)
                    query = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static void createUserInfoTable(Connection con) {
        Statement statement = null;
        String query = null, checkQuery = null;
        ResultSet rs = null;

        try {
            statement = con.createStatement();
            checkQuery = "SHOW TABLES LIKE 'User_Info';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("User_Info table already exists!");
            } else {
                query = "CREATE TABLE IF NOT EXISTS User_Info("
                        + "UI_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                        + "first_name VARCHAR(50) NOT NULL,"
                        + "middle_name VARCHAR(50) NOT NULL,"
                        + "last_name VARCHAR(50) NOT NULL,"
                        + "sex VARCHAR(10) NOT NULL,"
                        + "contact_number VARCHAR(11) UNIQUE NOT NULL,"
                        + "email_address VARCHAR(50) UNIQUE NOT NULL,"
                        + "house_number TINYINT NOT NULL,"
                        + "street VARCHAR(50) NOT NULL,"
                        + "purok VARCHAR(50) NOT NULL"
                        + " );";
                ;
                statement.executeUpdate(query);
                System.out.println("Table User_Info has been created!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
                if (query != null)
                    query = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createComplaintTable(Connection con) {
        Statement statement = null;
        String query = null, checkQuery;
        ResultSet rs = null;

        try {
            statement = con.createStatement();
            checkQuery = "SHOW TABLES LIKE 'Complaint_Detail';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("Complaint_Detail table already exists!");
            } else {
                query = "CREATE TABLE IF NOT EXISTS Complaint_Detail ("
                        + "CD_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                        + "current_status VARCHAR(20) NOT NULL,"
                        + "subject VARCHAR(50) NOT NULL,"
                        + "type VARCHAR(50) NOT NULL,"
                        + "date_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                        + "street VARCHAR(50) NOT NULL,"
                        + "purok VARCHAR(50) NOT NULL,"
                        + "longitude DECIMAL(11,8) NOT NULL,"
                        + "latitude DECIMAL(10,8) NOT NULL,"
                        + "persons_involved TEXT NOT NULL,"
                        + "details TEXT NOT NULL,"
                        + "photo_attachment TEXT NOT NULL,"
                        + "date_time_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL"
                        + " );";
                statement.executeUpdate(query);
                System.out.println("Table Complaint_Detail has been created!");
            }

            checkQuery = "SHOW TABLES LIKE 'Complaint';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("Complaint table already exists!");
            } else {
                query = "CREATE TABLE IF NOT EXISTS Complaint("
                        + "C_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                        + "CD_ID INT NOT NULL,"
                        + "UI_ID INT NOT NULL,"
                        + "FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID),"
                        + "FOREIGN KEY (UI_ID) REFERENCES User_Info(UI_ID)"
                        + ");";
                statement.executeUpdate(query);
                System.out.println("Table Complaint has been created!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
                if (query != null)
                    query = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static void createComplaintActionTable(Connection con) {
        Statement statement = null;
        String query = null, checkQuery;
        ResultSet rs = null;

        try {
            statement = con.createStatement();
            checkQuery = "SHOW TABLES LIKE 'Complaint_Action';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("Complaint_Action table already exists!");
            } else {
                query = "CREATE TABLE IF NOT EXISTS Complaint_Action("
                        + "CA_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                        + "CD_ID INT NOT NULL,"
                        + "action_taken TEXT NOT NULL,"
                        + "recommendation TEXT NOT NULL,"
                        + "oic VARCHAR(50) NOT NULL,"
                        + "date_time_assigned TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                        + "resolution_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                        + "FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID)"
                        + " );";
                statement.executeUpdate(query);
                System.out.println("Table Complaint_Action has been created!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
                if (query != null)
                    query = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createComplaintHistoryTable(Connection con) {
        Statement statement = null;
        String query = null, checkQuery = null;
        ResultSet rs = null;

        try {
            statement = con.createStatement();
            checkQuery = "SHOW TABLES LIKE 'Complaint_History_Detail';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("Complaint_History_Detail table already exists!");
            } else {
                query = "CREATE TABLE IF NOT EXISTS Complaint_History_Detail ("
                        + "CHD_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                        + "status VARCHAR(50) NOT NULL,"
                        + "process TEXT NOT NULL,"
                        + "date_time_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                        + "updated_by VARCHAR(100) NOT NULL"
                        + ");";
                statement.executeUpdate(query);
                System.out.println("Table Complaint_History_Detail has been created!");
            }

            checkQuery = "SHOW TABLES LIKE 'Complaint_History';";
            rs = statement.executeQuery(checkQuery);

            if (rs.next()) {
                System.out.println("Complaint_History table already exists!");
            } else {
                query = "CREATE TABLE IF NOT EXISTS Complaint_History("
                        + "CH_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                        + "CD_ID INT NOT NULL,"
                        + "CHD_ID INT NOT NULL,"
                        + "FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID),"
                        + "FOREIGN KEY (CHD_ID) REFERENCES Complaint_History_Detail(CHD_ID)"
                        + ");";
                statement.executeUpdate(query);
                System.out.println("Table Complaint_History has been created!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
                if (query != null)
                    query = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
