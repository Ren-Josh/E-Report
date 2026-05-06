package app;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import models.ComplaintDetail;
import models.Credential;
import models.UserInfo;
import models.UserSession;
import config.UIConfig;
import features.components.NavPanel;
import features.views.AllReportsView;
import features.views.ComplaintDetailView;
import features.views.ComplaintStatusUpdateView;
import features.views.DashboardView;
import features.views.ForgotPasswordView;
import features.views.HomepageView;
import features.views.LoginView;
import features.views.MyProfileView;
import features.views.MyReportsView;
import features.views.RegisterView;
import features.views.SecurityPasswordChangePanel;
import features.views.SubmitReportView;
import features.views.UserManagementView;
import services.controller.DatabaseController;

/**
 * Main application frame for the Barangay Malacañang E-Reporting System.
 * <p>
 * Acts as the central navigation controller using {@link CardLayout} to switch
 * between views. Holds global session state, user data, complaint data, and
 * cached dashboard statistics for all user roles.
 */
public class E_Report extends JFrame {
    private CardLayout cardLayout;
    private JPanel container;

    // ==================== Session / User Data ====================
    /** Currently logged-in user session (contains role, tokens, etc.). */
    private UserSession userSession;
    /** Profile information of the currently authenticated user. */
    private UserInfo userInfo;
    /** Login credentials associated with the current session. */
    private Credential credential;
    /** The complaint currently selected for detail viewing or status updates. */
    private ComplaintDetail currentComplaint;
    /**
     * Cached list of complaints fetched for the current context (dashboard,
     * reports, etc.).
     */
    private List<ComplaintDetail> complaintList = new ArrayList<>();

    // ==================== Resident Dashboard Data ====================
    /**
     * Cached stat counters for the Resident dashboard: [total, pending, inProgress,
     * resolved].
     */
    private int[] residentDashboardStats = new int[4];
    /** Cached list of recent report rows displayed on the Resident dashboard. */
    private List<Object[]> residentDashboardReports = new ArrayList<>();

    // ==================== Captain Dashboard Data ====================
    /**
     * Cached stat counters for the Captain dashboard: [total, pending, inProgress,
     * resolved].
     */
    private int[] captainDashboardStats = new int[4];
    /** Y-axis values for the Captain's line graph (e.g., complaints over time). */
    private double[] captainLineValues = new double[0];
    /** X-axis labels for the Captain's line graph. */
    private String[] captainLineLabels = new String[0];
    /**
     * Detailed tooltip or description strings paired with each line graph point.
     */
    private String[] captainLineDetails = new String[0];
    /** Title displayed above the Captain's line graph. */
    private String captainLineGraphTitle = "";
    /** Category labels for the Captain's category distribution chart. */
    private String[] captainCategoryLabels = new String[0];
    /** Category values matching {@link #captainCategoryLabels}. */
    private int[] captainCategoryValues = new int[0];
    /** Status labels for the Captain's status breakdown chart. */
    private String[] captainStatusLabels = new String[0];
    /** Background total counts used to render the status chart's full bars. */
    private int[] captainStatusBackgroundTotals = new int[0];
    /** Actual status counts to overlay on the background totals. */
    private int[] captainStatusValues = new int[0];
    /** Grand total used for percentage calculations in the Captain status chart. */
    private int captainStatusTotal = 0;
    /** Source labels for the Captain's complaint-source chart. */
    private String[] captainSourceLabels = new String[0];
    /** Source values matching {@link #captainSourceLabels}. */
    private int[] captainSourceValues = new int[0];
    /** Grand total for the Captain source chart. */
    private int captainSourceTotal = 0;

    // ==================== Secretary Dashboard Data ====================
    /**
     * Cached stat counters for the Secretary dashboard: [total, pending,
     * inProgress, resolved].
     */
    private int[] secretaryDashboardStats = new int[4];
    /** Cached list of report rows displayed on the Secretary dashboard. */
    private List<Object[]> secretaryReportDataList = new ArrayList<>();

    // ==================== Global Counters ====================
    /** Total number of reports submitted by the currently logged-in user. */
    private int totalReportByUser;
    /** Overall total number of reports in the system. */
    private int totalReport;
    /** Total number of reports filtered by a specific date. */
    private int totalReportByDate;
    /** Total number of reports filtered by a specific status. */
    private int totalReportByStatus;
    /** Total number of reports filtered by a specific role. */
    private int totalReportByRole;

    /**
     * Default route to return to after completing auxiliary flows
     * (e.g., after changing password from the security screen).
     */
    private String returnRoute = "dashboard";

    /**
     * Menu bar for easy navigation of frequently used panels
     */
    private JMenuBar jmbMenu;
    private JMenu jmNav;
    private JMenuItem jmiDashboard, jmiView, jmiSubmit, jmiProfile, jmiLogout, jmiExit;

    // ==================== Reusable Panels ====================
    /**
     * Single shared instance of the security password change panel.
     * Kept reusable so state (fields, validation messages) can be reset via
     * {@link SecurityPasswordChangePanel#preparePanel()} instead of rebuilding.
     */
    private SecurityPasswordChangePanel securityPasswordChangePanel;

    /**
     * Initializes the database, sets up the main JFrame properties,
     * constructs the CardLayout container, and displays the homepage.
     */
    public E_Report() {
        // Initialize database tables and connections before any view is shown.
        DatabaseController.initializeDatabase();

        setTitle("Barangay Malacañang E-Reporting System");
        setIconImage(new ImageIcon(getClass().getResource("/assets/images/barangay_logo.png")).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(UIConfig.WIDTH, UIConfig.HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on screen.

        // Set up CardLayout so views can be swapped by name.
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        // The homepage is the only view added to the CardLayout container;
        // all other views are instantiated on-the-fly during navigation.
        container.add(new HomepageView(this), "home");

        // Pre-build the reusable password panel once; it will be re-prepared
        // (fields cleared, focus reset) each time it is shown.
        securityPasswordChangePanel = new SecurityPasswordChangePanel(this);

        add(container);

        // Menu bar
        jmbMenu = new JMenuBar();

        // Navigation menu
        jmNav = new JMenu("Navigation");
        jmNav.setMnemonic('N'); // Alt+N shortcut

        // Menu items
        jmiDashboard = new JMenuItem("Dashboard");
        jmiView = new JMenuItem("View Complaints");
        jmiSubmit = new JMenuItem("Submit Complaint");
        jmiProfile = new JMenuItem("Profile");
        jmiLogout = new JMenuItem("Logout");
        jmiExit = new JMenuItem("Exit");

        // Add separator before logout/exit
        jmNav.add(jmiDashboard);
        jmNav.add(jmiProfile);
        jmNav.add(jmiView);
        jmNav.add(jmiSubmit);
        jmNav.addSeparator();
        jmNav.add(jmiLogout);
        jmNav.add(jmiExit);

        jmiDashboard.setAccelerator(KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
        jmiProfile.setAccelerator(KeyStroke.getKeyStroke('P', InputEvent.CTRL_DOWN_MASK));
        jmiView.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
        jmiSubmit.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
        jmiLogout.setAccelerator(KeyStroke.getKeyStroke('L', InputEvent.CTRL_DOWN_MASK));
        jmiExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));

        // Attach to menu bar and frame
        jmbMenu.add(jmNav);

        // Start the application at the homepage.
        cardLayout.show(container, "home");
        setVisible(true);

        jmiDashboard.addActionListener(e -> {
            navigate("Dashboard");
        });

        jmiProfile.addActionListener(e -> {
            navigate("Profile");
        });

        jmiView.addActionListener(e -> {
            navigate("MyReport");
        });

        jmiSubmit.addActionListener(e -> {
            navigate("SubmitReport");
        });

        jmiLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                logout();
            }
        });

        jmiExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    /**
     * Navigates to the requested route by replacing the content pane with the
     * corresponding view. Also synchronizes the navigation side-panel's selected
     * state so the active menu item matches the current route.
     *
     * @param route the named route to navigate to (e.g., "login", "dashboard")
     */
    public void navigate(String route) {
        // Clear the current content pane before attaching a new view.
        getContentPane().removeAll();

        // Route to the appropriate view. Each case constructs a fresh panel
        // (except "securitypassword", which reuses the cached instance).
        switch (route.toLowerCase()) {
            case "login" -> add(new LoginView(this));
            case "home" -> add(new HomepageView(this));
            case "register" -> add(new RegisterView(this));
            case "dashboard" -> add(new DashboardView(this));
            case "profile" -> add(new MyProfileView(this));
            case "myreport" -> add(new MyReportsView(this));
            case "reports" -> add(new AllReportsView(this));
            // case "usermanagement" -> add(new UserManagementView(this));
            case "submitreport" -> add(new SubmitReportView(this));
            case "updatestatus" -> add(new ComplaintStatusUpdateView(this));
            case "complaintdetail" -> add(new ComplaintDetailView(this));
            case "forgotpassword" -> add(new ForgotPasswordView(this));
            case "securitypassword" -> {
                // Reset internal field states before showing.
                securityPasswordChangePanel.preparePanel();
                add(securityPasswordChangePanel);
            }
        }

        // After adding the new view, traverse its component tree to find the
        // embedded NavPanel and highlight the menu item matching this route.
        Component[] children = getContentPane().getComponents();
        for (Component c : children) {
            NavPanel nav = extractNavPanel(c);
            if (nav != null) {
                nav.syncSelectionToRoute(route);
            }
        }

        // Re-layout and redraw the frame to reflect the new content.
        revalidate();
        repaint();
    }

    /**
     * Will add menu
     */
    public void addMenu() {
        setJMenuBar(jmbMenu);
    }

    /**
     * Attempts to extract the {@link NavPanel} from a view component.
     * <p>
     * Because each view encapsulates its own layout, this method uses
     * pattern-matching (instanceof) to call the view-specific getter.
     *
     * @param c the top-level component of the current view
     * @return the embedded NavPanel if the view is recognized; otherwise null
     */
    private NavPanel extractNavPanel(Component c) {
        if (c instanceof MyProfileView v)
            return v.getNavPanel();
        if (c instanceof MyReportsView v)
            return v.getNavPanel();
        if (c instanceof SubmitReportView v)
            return v.getNavPanel();
        if (c instanceof UserManagementView v)
            return v.getNavPanel();
        if (c instanceof DashboardView v)
            return v.getNavPanel();
        if (c instanceof AllReportsView v)
            return v.getNavPanel();

        return null;
    }

    /**
     * Clears all session and dashboard data, then returns the user to the homepage.
     * Typically invoked from a logout button.
     */
    public void logout() {
        clearSessionData();
        setJMenuBar(null);
        navigate("home");
    }

    /**
     * Resets all user-specific and complaint-specific fields to their default
     * states.
     */
    private void clearSessionData() {
        this.userSession = null;
        this.userInfo = null;
        this.credential = null;
        this.currentComplaint = null;
        this.complaintList.clear();
        this.residentDashboardStats = new int[4];
        this.residentDashboardReports = new ArrayList<>();
        this.captainDashboardStats = new int[4];
        this.captainLineValues = new double[0];
        this.captainLineLabels = new String[0];
        this.captainLineDetails = new String[0];
        this.captainLineGraphTitle = "";
        this.captainCategoryLabels = new String[0];
        this.captainCategoryValues = new int[0];
        this.captainStatusLabels = new String[0];
        this.captainStatusBackgroundTotals = new int[0];
        this.captainStatusValues = new int[0];
        this.captainStatusTotal = 0;
        this.captainSourceLabels = new String[0];
        this.captainSourceValues = new int[0];
        this.captainSourceTotal = 0;
        this.secretaryDashboardStats = new int[4];
        this.secretaryReportDataList = new ArrayList<>();
        this.totalReportByUser = 0;
        this.totalReport = 0;
        this.totalReportByDate = 0;
        this.totalReportByStatus = 0;
        this.totalReportByRole = 0;
        clearDashboardData();

    }

    /**
     * Resets every dashboard cache (Resident, Captain, Secretary) to empty values.
     * Called during logout or when switching users to prevent stale data leaks.
     */
    private void clearDashboardData() {
        this.residentDashboardStats = new int[4];
        this.residentDashboardReports.clear();

        this.captainDashboardStats = new int[4];
        this.captainLineValues = new double[0];
        this.captainLineLabels = new String[0];

        this.secretaryDashboardStats = new int[4];
        this.secretaryReportDataList.clear();
    }

    // ==================== Session / User Encapsulation ====================

    /**
     * Stores the active user session.
     *
     * @param us the session object returned after successful authentication
     */
    public void setUserSession(UserSession us) {
        this.userSession = us;
    }

    /**
     * @return the current user session, or null if not logged in
     */
    public UserSession getUserSession() {
        return userSession;
    }

    /**
     * Stores the profile details of the authenticated user.
     *
     * @param ui the UserInfo model containing names, contact, address, etc.
     */
    public void setUserInfo(UserInfo ui) {
        this.userInfo = ui;
    }

    /**
     * @return the current user's profile information
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Stores the credential record (username, password hash, security questions)
     * for the authenticated user.
     *
     * @param cred the Credential model
     */
    public void setCredential(Credential cred) {
        this.credential = cred;
    }

    /**
     * @return the current user's credential object
     */
    public Credential getCredential() {
        return credential;
    }

    /**
     * Convenience helper so panels never build the full name themselves.
     * Concatenates first and last name, trimming any extra whitespace.
     *
     * @return the user's full name, or null if both names are missing
     */
    public String getCurrentUserFullName() {
        StringBuilder sb = new StringBuilder();
        if (userInfo.getFName() != null)
            sb.append(userInfo.getFName()).append(" ");
        if (userInfo.getLName() != null)
            sb.append(userInfo.getLName());
        String name = sb.toString().trim();
        return name.isEmpty() ? null : name;
    }

    /**
     * @return the role string of the current session (e.g., "Resident", "Captain"),
     *         or null if no session exists
     */
    public String getCurrentUserRole() {
        if (userSession == null)
            return null;
        String role = userSession.getRole();
        return role == null ? null : role;
    }

    // ==================== Complaint Encapsulation ====================

    /**
     * Sets the complaint currently selected for detail viewing or editing.
     *
     * @param cd the ComplaintDetail to cache
     */
    public void setCurrentComplaint(ComplaintDetail cd) {
        this.currentComplaint = cd;
    }

    /**
     * @return the cached complaint selected by the user
     */
    public ComplaintDetail getCurrentComplaint() {
        return currentComplaint;
    }

    /**
     * Replaces the internal complaint list with a defensive copy.
     *
     * @param list the new list of complaints; null is treated as empty
     */
    public void setComplaintList(List<ComplaintDetail> list) {
        this.complaintList = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * @return a defensive copy of the cached complaint list
     */
    public List<ComplaintDetail> getComplaintList() {
        return new ArrayList<>(complaintList);
    }

    // ==================== Resident Dashboard ====================

    /**
     * Caches the four primary stat counters for the Resident dashboard.
     *
     * @param total      total reports
     * @param pending    reports with pending status
     * @param inProgress reports currently being processed
     * @param resolved   reports marked as resolved
     */
    public void setResidentDashboardStats(int total, int pending, int inProgress, int resolved) {
        this.residentDashboardStats = new int[] { total, pending, inProgress, resolved };
    }

    /**
     * @return a clone of the Resident dashboard stats array
     */
    public int[] getResidentDashboardStats() {
        return residentDashboardStats.clone();
    }

    /**
     * Caches the recent reports table data for the Resident dashboard.
     *
     * @param reports list of Object arrays representing table rows
     */
    public void setResidentDashboardReports(List<Object[]> reports) {
        this.residentDashboardReports = reports != null ? new ArrayList<>(reports) : new ArrayList<>();
    }

    /**
     * @return a defensive copy of the Resident dashboard report rows
     */
    public List<Object[]> getResidentDashboardReports() {
        return new ArrayList<>(residentDashboardReports);
    }

    // ==================== Captain Dashboard ====================

    /**
     * Caches the four primary stat counters for the Captain dashboard.
     *
     * @param total      total reports
     * @param pending    reports with pending status
     * @param inProgress reports currently being processed
     * @param resolved   reports marked as resolved
     */
    public void setCaptainDashboardStats(int total, int pending, int inProgress, int resolved) {
        this.captainDashboardStats = new int[] { total, pending, inProgress, resolved };
    }

    /**
     * @return a clone of the Captain dashboard stats array
     */
    public int[] getCaptainDashboardStats() {
        return captainDashboardStats.clone();
    }

    /**
     * Caches all data required to render the Captain's line graph.
     *
     * @param values  numeric data points (Y-axis)
     * @param labels  category or date labels (X-axis)
     * @param details hover descriptions for each point
     * @param title   graph heading
     */
    public void setCaptainLineGraphData(double[] values, String[] labels, String[] details, String title) {
        this.captainLineValues = values != null ? values.clone() : new double[0];
        this.captainLineLabels = labels != null ? labels.clone() : new String[0];
        this.captainLineDetails = details != null ? details.clone() : new String[0];
        this.captainLineGraphTitle = title != null ? title : "";
    }

    /**
     * @return a clone of the Captain line graph values array
     */
    public double[] getCaptainLineValues() {
        return captainLineValues.clone();
    }

    /**
     * @return a clone of the Captain line graph labels array
     */
    public String[] getCaptainLineLabels() {
        return captainLineLabels.clone();
    }

    /**
     * @return a clone of the Captain line graph detail strings array
     */
    public String[] getCaptainLineDetails() {
        return captainLineDetails.clone();
    }

    /**
     * @return the title string for the Captain line graph
     */
    public String getCaptainLineGraphTitle() {
        return captainLineGraphTitle;
    }

    /**
     * Caches category distribution data for the Captain's category chart.
     *
     * @param labels category names
     * @param values complaint counts per category
     */
    public void setCaptainCategoryData(String[] labels, int[] values) {
        this.captainCategoryLabels = labels != null ? labels.clone() : new String[0];
        this.captainCategoryValues = values != null ? values.clone() : new int[0];
    }

    /**
     * @return a clone of the Captain category labels array
     */
    public String[] getCaptainCategoryLabels() {
        return captainCategoryLabels.clone();
    }

    /**
     * @return a clone of the Captain category values array
     */
    public int[] getCaptainCategoryValues() {
        return captainCategoryValues.clone();
    }

    /**
     * Caches status breakdown data for the Captain's status chart.
     *
     * @param labels   status names
     * @param bgTotals background bar totals for proportional rendering
     * @param values   actual counts per status
     * @param total    grand total for percentage calculation
     */
    public void setCaptainStatusData(String[] labels, int[] bgTotals, int[] values, int total) {
        this.captainStatusLabels = labels != null ? labels.clone() : new String[0];
        this.captainStatusBackgroundTotals = bgTotals != null ? bgTotals.clone() : new int[0];
        this.captainStatusValues = values != null ? values.clone() : new int[0];
        this.captainStatusTotal = total;
    }

    /**
     * @return a clone of the Captain status labels array
     */
    public String[] getCaptainStatusLabels() {
        return captainStatusLabels.clone();
    }

    /**
     * @return a clone of the Captain status background totals array
     */
    public int[] getCaptainStatusBackgroundTotals() {
        return captainStatusBackgroundTotals.clone();
    }

    /**
     * @return a clone of the Captain status values array
     */
    public int[] getCaptainStatusValues() {
        return captainStatusValues.clone();
    }

    /**
     * @return the grand total used in the Captain status chart
     */
    public int getCaptainStatusTotal() {
        return captainStatusTotal;
    }

    /**
     * Caches complaint-source distribution data for the Captain's source chart.
     *
     * @param labels source names
     * @param values complaint counts per source
     * @param total  grand total for percentage calculation
     */
    public void setCaptainSourceData(String[] labels, int[] values, int total) {
        this.captainSourceLabels = labels != null ? labels.clone() : new String[0];
        this.captainSourceValues = values != null ? values.clone() : new int[0];
        this.captainSourceTotal = total;
    }

    /**
     * @return a clone of the Captain source labels array
     */
    public String[] getCaptainSourceLabels() {
        return captainSourceLabels.clone();
    }

    /**
     * @return a clone of the Captain source values array
     */
    public int[] getCaptainSourceValues() {
        return captainSourceValues.clone();
    }

    /**
     * @return the grand total used in the Captain source chart
     */
    public int getCaptainSourceTotal() {
        return captainSourceTotal;
    }

    // ==================== Secretary Dashboard ====================

    /**
     * Caches the four primary stat counters for the Secretary dashboard.
     *
     * @param total      total reports
     * @param pending    reports with pending status
     * @param inProgress reports currently being processed
     * @param resolved   reports marked as resolved
     */
    public void setSecretaryDashboardStats(int total, int pending, int inProgress, int resolved) {
        this.secretaryDashboardStats = new int[] { total, pending, inProgress, resolved };
    }

    /**
     * @return a clone of the Secretary dashboard stats array
     */
    public int[] getSecretaryDashboardStats() {
        return secretaryDashboardStats.clone();
    }

    /**
     * Caches the reports table data for the Secretary dashboard.
     *
     * @param reports list of Object arrays representing table rows
     */
    public void setSecretaryReportDataList(List<Object[]> reports) {
        this.secretaryReportDataList = reports != null ? new ArrayList<>(reports) : new ArrayList<>();
    }

    /**
     * @return a defensive copy of the Secretary dashboard report rows
     */
    public List<Object[]> getSecretaryReportDataList() {
        return new ArrayList<>(secretaryReportDataList);
    }

    // ==================== Global Stats ====================

    /**
     * Stores the total report count submitted by the current user.
     *
     * @param val the count
     */
    public void setTotalReportByUser(int val) {
        this.totalReportByUser = val;
    }

    /**
     * @return total reports submitted by the current user
     */
    public int getTotalReportByUser() {
        return totalReportByUser;
    }

    /**
     * Stores the overall total report count in the system.
     *
     * @param val the count
     */
    public void setTotalReport(int val) {
        this.totalReport = val;
    }

    /**
     * @return overall total reports in the system
     */
    public int getTotalReport() {
        return totalReport;
    }

    /**
     * Stores the total report count for a specific date filter.
     *
     * @param val the count
     */
    public void setTotalReportByDate(int val) {
        this.totalReportByDate = val;
    }

    /**
     * @return total reports matching the active date filter
     */
    public int getTotalReportByDate() {
        return totalReportByDate;
    }

    /**
     * Stores the total report count for a specific status filter.
     *
     * @param val the count
     */
    public void setTotalReportByStatus(int val) {
        this.totalReportByStatus = val;
    }

    /**
     * @return total reports matching the active status filter
     */
    public int getTotalReportByStatus() {
        return totalReportByStatus;
    }

    /**
     * Stores the total report count for a specific role filter.
     *
     * @param val the count
     */
    public void setTotalReportByRole(int val) {
        this.totalReportByRole = val;
    }

    /**
     * @return total reports matching the active role filter
     */
    public int getTotalReportByRole() {
        return totalReportByRole;
    }

    /**
     * Sets the route name to return to after auxiliary flows complete.
     *
     * @param route the route identifier (e.g., "dashboard")
     */
    public void setReturnRoute(String route) {
        this.returnRoute = route;
    }

    /**
     * @return the stored return route
     */
    public String getReturnRoute() {
        return returnRoute;
    }

    /**
     * Application entry point. Ensures the GUI is constructed on the
     * Event Dispatch Thread for thread safety.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(E_Report::new);
    }
}