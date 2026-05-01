package config;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class UIConfig {

        // =========================
        // PATHS
        // =========================
        public static final String LOGO_PATH = "src/assets/images/barangay_logo.png";
        public static final String BACKGROUND_PATH = "src/assets/images/background1.png";

        // =========================
        // WINDOW
        // =========================
        public static final int WIDTH = 1280;
        public static final int HEIGHT = 720;

        // =========================
        // LOGO
        // =========================
        public static final int LOGO_SIZE = 280;

        // =========================
        // ICONS
        // =========================
        public static final String LOGOUT_ICON_PATH = "src/assets/icons/logout_icon.png";
        public static final String USER_ICON_PATH = "src/assets/icons/circle_user_icon.png";
        public static final String LOCK_ICON_PATH = "src/assets/icons/lock_icon.png";
        public static final String EYE_ICON_PATH = "src/assets/icons/eye_icon.png";
        public static final String EYE_OFF_ICON_PATH = "src/assets/icons/eye_off_icon.png";
        public static final String SUSPEND_ICON_PATH = "src/assets/icons/suspend_icon.png";
        public static final String EDIT_ICON_PATH = "src/assets/icons/edit_icon.png";
        public static final String SECURITY_LOCK_ICON_PATH = "src/assets/icons/lock_icon.png";
        public static final int SECURITY_LOCK_ICON_SIZE = 48;

        public static final String[] STAT_ICON_PATHS = {
                        "src/assets/icons/total_report_icon.png",
                        "src/assets/icons/pending_icon.png",
                        "src/assets/icons/in_progress_icon.png",
                        "src/assets/icons/resolved_icon.png"
        };
        public static final String[] STAT_LABEL_PATHS = {
                        "Total Reports", "Pending", "In Progress", "Resolved"
        };

        public static String[] NAV_RESIDENT_ICON_PATHS = {
                        "src/assets/icons/dashboard_icon.png",
                        "src/assets/icons/reports_icon.png",
                        "src/assets/icons/submit_icon.png",
        };

        public static String[] NAV_RESIDENT_TARGET = {
                        "dashboard",
                        "myreport",
                        "submitreport",
        };

        public static String[] NAV_SECRETARY_ICON_PATHS = {
                        "src/assets/icons/dashboard_icon.png",
                        "src/assets/icons/reports_icon.png",
                        "src/assets/icons/all_reports_icon.png",
                        "src/assets/icons/submit_icon.png",
                        "src/assets/icons/users_icon.png",
        };

        public static String[] NAV_SECRETARY_TARGET = {
                        "dashboard",
                        "myreport",
                        "reports",
                        "submitreport",
                        "usermanagement"
        };

        public static String[] NAV_CAPTAIN_ICON_PATHS = {
                        "src/assets/icons/dashboard_icon.png",
                        "src/assets/icons/reports_icon.png",
                        "src/assets/icons/all_reports_icon.png",
                        "src/assets/icons/submit_icon.png",
        };

        public static String[] NAV_CAPTAIN_TARGET = {
                        "dashboard",
                        "myreport",
                        "reports",
                        "submitreport"
        };

        public static String[] NAV_RESIDENT_ICON_LABELS = {
                        "Dashboard", "My Reports", "Submit Report"
        };

        public static String[] NAV_SECRETARY_ICON_LABELS = {
                        "Dashboard", "My Reports", "Reports", "Submit Report",
                        "Users"
        };

        public static String[] NAV_CAPTAIN_ICON_LABELS = {
                        "Dashboard", "My Reports", "All Reports", "Submit Report",
        };

        // =========================
        // FONTS
        // =========================
        public static final Font H1 = new Font("Segoe UI", Font.BOLD, 56);
        public static final Font H2 = new Font("Segoe UI", Font.BOLD, 32);
        public static final Font H3 = new Font("Segoe UI", Font.BOLD, 24);

        public static final Font BODY_LARGE = new Font("Segoe UI", Font.PLAIN, 22);
        public static final Font BODY = new Font("Arial", Font.PLAIN, 16);
        public static final Font CAPTION = new Font("Segoe UI", Font.PLAIN, 13);

        // NEW: Small font tokens for filters, chips, and compact UI
        public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
        public static final Font SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);

        public static final Font INPUT_TITLE = new Font("Segoe UI", Font.BOLD, 16);

        // =========================
        // BUTTON SIZES
        // =========================
        public static final Dimension BTN_PRIMARY = new Dimension(220, 65);
        public static final Dimension BTN_SECONDARY = new Dimension(180, 55);
        public static final Dimension BTN_SMALL = new Dimension(140, 45);

        public static final Font BTN_PRIMARY_FONT = new Font("Segoe UI", Font.BOLD, 22);
        public static final Font BTN_SECONDARY_FONT = new Font("Segoe UI", Font.BOLD, 18);
        public static final Font BTN_SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 16);

        public static final int RADIUS_PRIMARY = 45;
        public static final int RADIUS_SECONDARY = 35;
        public static final int RADIUS_SMALL = 25;

        // NEW: Pill radius used by filter bars and chips
        public static final int RADIUS_PILL = 20;

        // =========================
        // SPACING
        // =========================
        public static final int XS = 5;
        public static final int SM = 10;
        public static final int MD = 20;
        public static final int LG = 40;
        public static final int XL = 55;

        // =========================
        // COLORS
        // =========================
        public static final Color PRIMARY = new Color(25, 87, 191);
        public static final Color SECONDARY = new Color(53, 131, 234);
        public static final Color SUCCESS = new Color(60, 191, 42);

        public static final Color TEXT_PRIMARY = new Color(25, 25, 25);
        public static final Color TEXT_SECONDARY = new Color(60, 60, 60);

        public static final Color BG_LIGHT = new Color(245, 247, 250);

        public static final Color[] STAT_COLORS = {
                        new Color(100, 150, 255, 220),
                        new Color(255, 200, 100, 220),
                        new Color(200, 100, 255, 220),
                        new Color(100, 200, 100, 220)
        };

        // NEW: Neutral text shades used by cards and filters
        public static final Color TEXT_DARK = new Color(15, 23, 42);
        public static final Color TEXT_MUTED = new Color(100, 116, 139);

        // NEW: Border colors for consistent field / card / divider styling
        public static final Color BORDER_LIGHT = new Color(210, 215, 225);
        public static final Color BORDER_MEDIUM = new Color(220, 224, 230);
        public static final Color BORDER_CARD = new Color(226, 232, 240);
        public static final Color BG_HOVER = new Color(235, 238, 242);
        public static final Color BG_SUBTLE = new Color(220, 225, 235);

        // NEW: Accent colors for filter indicators, badges, and chips
        public static final Color ACCENT_BLUE = new Color(25, 118, 210);
        public static final Color ACCENT_PURPLE = new Color(156, 39, 176);
        public static final Color ACCENT_GREEN = new Color(46, 125, 50);
        public static final Color ACCENT_ORANGE = new Color(230, 81, 0);
        public static final Color ACCENT_TEAL = new Color(0, 150, 136);
        public static final Color ACCENT_SLATE = new Color(100, 110, 130);

        // =========================
        // COMPONENT SIZES
        // =========================
        // NEW: Standard field heights
        public static final int FIELD_HEIGHT_SM = 30;
        public static final int FIELD_HEIGHT_MD = 32;
        public static final int FIELD_HEIGHT_LG = 42;

        // =========================
        // SHARED FIELD STYLING (Input, PasswordInput, ComboBox)
        // =========================
        public static final int FIELD_RADIUS = 12;
        public static final Color FIELD_BORDER_IDLE = BORDER_MEDIUM;
        public static final Color FIELD_BORDER_HOVER = new Color(180, 180, 180);
        public static final Color FIELD_BG_HOVER = new Color(250, 250, 250);
        public static final Color FIELD_BG_READONLY = new Color(248, 250, 252);
        public static final float FIELD_STROKE_WIDTH = 1.3f;
        public static final Color FIELD_INVALID = new Color(220, 60, 60);
        public static final Color FIELD_VALID = new Color(0, 170, 80);
        public static final Color FIELD_PLACEHOLDER = TEXT_MUTED;

        // =========================
        // OUTLINED BUTTON CONFIG
        // =========================
        public static final Color OUTLINE_PRIMARY = PRIMARY;
        public static final Color OUTLINE_TEXT = PRIMARY;
        public static final Color OUTLINE_BG = new Color(0, 0, 0, 0);
        public static final int OUTLINE_THICKNESS = 1;

        // =========================
        // DISABLED STATE
        // =========================
        public static final Color DISABLED_BG = new Color(200, 200, 200);
        public static final Color DISABLED_TEXT = new Color(150, 150, 150);

        // =========================
        // ELEVATION (SHADOW)
        // =========================
        public static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);
        public static final int SHADOW_OFFSET_X = 3;
        public static final int SHADOW_OFFSET_Y = 4;
        public static final int SHADOW_BLUR = 8;

        public static final int ACTIVITY_COMPACT_THRESHOLD = 80;
        public static final int ACTIVITY_SCROLLBAR_PAD = 0;
        public static final int ACTIVITY_RIGHT_COL_WIDTH = 150;
        public static final int ACTIVITY_RIGHT_COL_WIDTH_COMPACT = 60;

        public static final Font ACTIVITY_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
        public static final Font ACTIVITY_TITLE_FONT_COMPACT = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font ACTIVITY_ITEM_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 13);
        public static final Font ACTIVITY_ITEM_TITLE_FONT_COMPACT = new Font("Segoe UI", Font.BOLD, 11);
        public static final Font ACTIVITY_DESC_FONT = new Font("Segoe UI", Font.PLAIN, 12);
        public static final Font ACTIVITY_DESC_FONT_COMPACT = new Font("Segoe UI", Font.PLAIN, 10);
        public static final Font ACTIVITY_META_FONT = new Font("Segoe UI", Font.PLAIN, 11);
        public static final Font ACTIVITY_META_FONT_COMPACT = new Font("Segoe UI", Font.PLAIN, 10);

        public static final int ACTIVITY_ICON_WIDTH = 4;
        public static final int ACTIVITY_ICON_WIDTH_COMPACT = 3;

        // =========================
        // COMBOBOX PRESET
        // =========================
        public static final Font COMBOBOX_FONT = SMALL;
        public static final int COMBOBOX_HEIGHT = FIELD_HEIGHT_MD;
        public static final int COMBOBOX_WIDTH_SHORT = 70;
        public static final int COMBOBOX_WIDTH_MEDIUM = 100;
        public static final int COMBOBOX_WIDTH_STANDARD = 140;
        public static final int COMBOBOX_WIDTH_LONG = 150;
        public static final Color COMBOBOX_FG = TEXT_SECONDARY;
        public static final Border COMBOBOX_BORDER = BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_MEDIUM, 1, true),
                        BorderFactory.createEmptyBorder(2, 8, 2, 4));

}