package config;

public class AppConfig {
        // Centralized Database Credentials
        public static final String DB_URL = "jdbc:mysql://localhost:3306/e_report";
        public static final String DB_USERNAME = "root";
        public static final String DB_PASSWORD = "";

        // Database Driver
        public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

        // ==================== SMTP / Email Configuration ====================
        public static final String SMTP_HOST = "smtp.gmail.com";
        public static final int SMTP_PORT = 587;
        public static final String SMTP_USERNAME = "renjosh.neust@gmail.com"; // CHANGE THIS
        public static final String SMTP_PASSWORD = "fkrk alxl eaqq xbxp"; // CHANGE THIS
        public static final String SMTP_FROM_NAME = "Barangay E-Reporting System";
        public static final boolean SMTP_USE_TLS = true;
        //

        public static final String[] REPORT_BACKGROUND_CANDIDATES = {
                        "desktop.png",
                        "images/desktop.png",
                        "desktop.jpg",
                        "images/desktop.jpg",
                        "images/desktop2.png",
                        "images/desktop2.jpg",
                        "images/desktop2.jpeg",
                        "images/desktop2.PNG",
                        "images/desktop2.JPG",
                        "images/desktop2.JPEG"
        };

        // ==================== COMPLAINT TYPES / CATEGORIES ====================
        public static final String[] COMPLAINT_TYPES = {
                        "All Category", "Environment", "Infrastructure", "Health", "Public Safety", "Sanitation",
                        "Community Relations", "Utilities", "Garbage", "Transportation"
        };

        public static final String[] REPORT_PUROK_OPTIONS = {
                        "All Purok",
                        "Purok 1",
                        "Purok 2",
                        "Purok 3",
                        "Purok 4",
                        "Purok 5"
        };
        // Default map center for the e-report service coverage
        public static final double REPORT_DEFAULT_MAP_LATITUDE = 15.479997;
        public static final double REPORT_DEFAULT_MAP_LONGITUDE = 121.086568;
        public static final int REPORT_DEFAULT_MAP_ZOOM = 16;
        public static final int REPORT_SERVICE_AREA_RADIUS_METERS = 1000;

        // UI Placeholders — must match the first item of their combo arrays
        public static final String REPORT_CATEGORY_PLACEHOLDER = "Select Category";
        public static final String REPORT_PUROK_PLACEHOLDER = "Select Purok";
        public static final String REPORT_LOCATION_PLACEHOLDER = "Street, Landmark";

        // Roles
        public static final String ROLE_RESIDENT = "Resident";
        public static final String ROLE_CAPTAIN = "Captain";
        public static final String ROLE_SECRETARY = "Secretary";

        // Connection Timeout (in seconds)
        public static final int DB_TIMEOUT = 5;

        // Database Creation Query
        public static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS e_report;";

        // Table Creation Queries
        public static final String CREATE_USER_INFO_TABLE = """
                        CREATE TABLE IF NOT EXISTS User_Info(
                            UI_ID INT AUTO_INCREMENT PRIMARY KEY,
                            first_name VARCHAR(50) NOT NULL,
                            middle_name VARCHAR(50) NOT NULL,
                            last_name VARCHAR(50) NOT NULL,
                            sex VARCHAR(10) NOT NULL,
                            contact_number VARCHAR(11) UNIQUE NOT NULL,
                            email_address VARCHAR(50) UNIQUE NOT NULL,
                            house_number VARCHAR(10) NOT NULL,
                            purok VARCHAR(50) NOT NULL
                        );
                        """;

        public static final String CREATE_CREDENTIAL_TABLE = """
                        CREATE TABLE IF NOT EXISTS Credential(
                            Cred_ID INT AUTO_INCREMENT PRIMARY KEY,
                            UI_ID INT NOT NULL,
                            username VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin UNIQUE NOT NULL,
                            password VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                            role VARCHAR(20) NOT NULL,
                            is_verified BOOLEAN,
                            date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            FOREIGN KEY (UI_ID) REFERENCES User_Info(UI_ID)
                        );
                        """;

        public static final String CREATE_COMPLAINT_DETAIL_TABLE = """
                        CREATE TABLE IF NOT EXISTS Complaint_Detail(
                            CD_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                            UI_ID INT NOT NULL,
                            current_status VARCHAR(20) NOT NULL,
                            subject VARCHAR(50),
                            type VARCHAR(50),
                            date_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            street VARCHAR(50) NOT NULL,
                            purok VARCHAR(50) NOT NULL,
                            longitude DECIMAL(11,8) NOT NULL,
                            latitude DECIMAL(10,8) NOT NULL,
                            persons_involved TEXT NOT NULL,
                            details TEXT NOT NULL,
                            photo_attachment MEDIUMBLOB,
                            date_time_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            FOREIGN KEY (UI_ID) REFERENCES User_Info(UI_ID)
                        );
                        """;

        public static final String CREATE_COMPLAINT_ACTION_TABLE = """
                        CREATE TABLE IF NOT EXISTS Complaint_Action(
                            CA_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                            CD_ID INT NOT NULL,
                            action_taken TEXT NOT NULL,
                            recommendation TEXT NOT NULL,
                            oic VARCHAR(50) NOT NULL,
                            date_time_assigned TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            resolution_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID)
                        );
                        """;

        public static final String CREATE_COMPLAINT_HISTORY_DETAIL_TABLE = """
                        CREATE TABLE IF NOT EXISTS Complaint_History_Detail(
                            CHD_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                            CD_ID INT NOT NULL,
                            status VARCHAR(50) NOT NULL,
                            process TEXT NOT NULL,
                            date_time_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            updated_by INT NOT NULL,
                            FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID),
                            FOREIGN KEY (updated_by) REFERENCES User_Info(UI_ID)
                        );
                        """;

        public static final String CREATE_FOLLOW_UP_REQUEST_TABLE = """
                        CREATE TABLE IF NOT EXISTS Follow_Up_Request(
                            FUR_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                            CD_ID INT NOT NULL,
                            UI_ID INT NOT NULL,
                            request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            status VARCHAR(20) DEFAULT 'Pending' NOT NULL,
                            notes TEXT,
                            FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID),
                            FOREIGN KEY (UI_ID) REFERENCES User_Info(UI_ID)
                        );
                        """;

        // DB table names
        public static final String TABLE_USER_INFO = "User_Info";
        public static final String TABLE_CREDENTIAL = "Credential";
        public static final String TABLE_COMPLAINT_DETAIL = "Complaint_Detail";
        public static final String TABLE_COMPLAINT_HISTORY_DETAIL = "Complaint_History_Detail";
        public static final String TABLE_COMPLAINT_ACTION = "Complaint_Action";
        public static final String TABLE_FOLLOW_UP_REQUEST = "Follow_Up_Request";
}