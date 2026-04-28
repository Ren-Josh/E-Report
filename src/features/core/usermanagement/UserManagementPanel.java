package features.core.usermanagement;

import config.UIConfig;
import features.components.*;
import features.core.RecentReportsPanel;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementPanel extends JPanel {

    private RecentReportsPanel recentReportsPanel;

    // Filters
    private UIInput nameFilter;
    private UIComboBox<String> roleFilter;
    private UIComboBox<String> purokFilter;
    private UIComboBox<String> statusFilter;

    // Data
    private List<UserData> allUsers = new ArrayList<>();
    private List<UserData> displayedUsers = new ArrayList<>();

    private UserActionListener actionListener;
    private FilterListener filterListener;

    public interface UserActionListener {
        void onEdit(int rowIndex, UserData user);

        void onBanToggle(int rowIndex, UserData user, boolean currentlyBanned);
    }

    public interface FilterListener {
        void onFilterChanged(String name, String role, String purok, String status);
    }

    // ── Constructors ─────────────────────────────────────────────

    public UserManagementPanel() {
        initialize();
    }

    public UserManagementPanel(UserActionListener actionListener, FilterListener filterListener) {
        this();
        setUserActionListener(actionListener);
        setFilterListener(filterListener);
    }

    public void setUserActionListener(UserActionListener listener) {
        this.actionListener = listener;
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    // ── Initialization ───────────────────────────────────────────

    private void initialize() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Manage Users");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);

        // Filter bar
        JPanel filterPanel = createFilterPanel();

        // Table
        String[] columns = { "Name", "Role", "Purok", "Phone", "Status", "Action" };
        recentReportsPanel = new RecentReportsPanel("Users", columns);
        recentReportsPanel.setActionColumnIndex(5);

        JTable table = recentReportsPanel.getTable();

        // Column widths (Action width is auto-sized by RecentReportsPanel)
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(200);
        cm.getColumn(1).setPreferredWidth(120);
        cm.getColumn(2).setPreferredWidth(100);
        cm.getColumn(3).setPreferredWidth(130);
        cm.getColumn(4).setPreferredWidth(90);

        // ── Register interactive action icons with background colors ──
        recentReportsPanel.addAction(new RecentReportsPanel.TableAction(
                UIConfig.EDIT_ICON_PATH,
                UIConfig.ACCENT_BLUE, // ← blue background
                idx -> {
                    if (actionListener != null && idx >= 0 && idx < displayedUsers.size()) {
                        actionListener.onEdit(idx, displayedUsers.get(idx));
                    }
                }));

        recentReportsPanel.addAction(new RecentReportsPanel.TableAction(
                UIConfig.SUSPEND_ICON_PATH,
                new Color(220, 60, 60), // ← red background for suspend
                idx -> {
                    if (actionListener != null && idx >= 0 && idx < displayedUsers.size()) {
                        UserData user = displayedUsers.get(idx);
                        actionListener.onBanToggle(idx, user, user.isBanned());
                    }
                }));

        // Assembly
        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(titlePanel, BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(recentReportsPanel, BorderLayout.CENTER);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        row.setOpaque(false);

        // Name search
        nameFilter = new UIInput(15);
        nameFilter.setPreferredSize(new Dimension(180, UIConfig.COMBOBOX_HEIGHT));
        nameFilter.setPlaceholder("Search name...");
        nameFilter.setFont(UIConfig.SMALL);
        nameFilter.setRadius(8);
        nameFilter.setIdleBorderColor(new Color(200, 200, 200));
        row.add(createPillWrapper(nameFilter, UIConfig.ACCENT_TEAL, "Search by name"));

        // Role
        String[] roles = { "All Roles", "Resident", "Secretary", "Barangay Captain", "Admin" };
        roleFilter = new UIComboBox<>(roles);
        roleFilter.setPreferredSize(new Dimension(150, UIConfig.COMBOBOX_HEIGHT));
        UIComboBox.applyPreset(roleFilter, 150);
        row.add(createPillWrapper(roleFilter, UIConfig.ACCENT_PURPLE, "Role"));

        // Purok
        String[] puroks = { "All Puroks", "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" };
        purokFilter = new UIComboBox<>(puroks);
        purokFilter.setPreferredSize(new Dimension(130, UIConfig.COMBOBOX_HEIGHT));
        UIComboBox.applyPreset(purokFilter, 130);
        row.add(createPillWrapper(purokFilter, UIConfig.ACCENT_GREEN, "Purok"));

        // Status
        String[] statuses = { "All Statuses", "Active", "Banned" };
        statusFilter = new UIComboBox<>(statuses);
        statusFilter.setPreferredSize(new Dimension(130, UIConfig.COMBOBOX_HEIGHT));
        UIComboBox.applyPreset(statusFilter, 130);
        row.add(createPillWrapper(statusFilter, UIConfig.ACCENT_ORANGE, "Status"));

        // Buttons
        row.add(Box.createHorizontalStrut(10));

        UIButton applyBtn = new UIButton(
                "Apply",
                new Color(25, 118, 210),
                new Dimension(80, UIConfig.COMBOBOX_HEIGHT),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.PRIMARY);
        applyBtn.addActionListener(e -> applyFilters());
        row.add(applyBtn);

        UIButton resetBtn = new UIButton(
                "Reset",
                Color.WHITE,
                new Dimension(80, UIConfig.COMBOBOX_HEIGHT),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.OUTLINED);
        resetBtn.addActionListener(e -> resetFilters());
        row.add(resetBtn);

        panel.add(row);
        return panel;
    }

    private JPanel createPillWrapper(JComponent comp, Color indicatorColor, String tooltip) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        UIConfig.RADIUS_PILL, UIConfig.RADIUS_PILL);
                g2.setColor(UIConfig.BORDER_MEDIUM);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        UIConfig.RADIUS_PILL, UIConfig.RADIUS_PILL);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);

        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(4, UIConfig.COMBOBOX_HEIGHT - 4));
        indicator.setBackground(indicatorColor);
        indicator.setOpaque(true);
        indicator.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        wrapper.add(indicator, BorderLayout.WEST);
        wrapper.add(comp, BorderLayout.CENTER);
        wrapper.setToolTipText(tooltip);
        return wrapper;
    }

    // ── Filtering ────────────────────────────────────────────────

    private void applyFilters() {
        refreshDisplayedData();
        if (filterListener != null) {
            filterListener.onFilterChanged(
                    nameFilter.getValue(),
                    (String) roleFilter.getSelectedItem(),
                    (String) purokFilter.getSelectedItem(),
                    (String) statusFilter.getSelectedItem());
        }
    }

    private void resetFilters() {
        nameFilter.setText("");
        roleFilter.setSelectedIndex(0);
        purokFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        refreshDisplayedData();
        if (filterListener != null) {
            filterListener.onFilterChanged("", "All Roles", "All Puroks", "All Statuses");
        }
    }

    public void clearFilters() {
        resetFilters();
    }

    // ── Public Data API ──────────────────────────────────────────

    public void setData(List<UserData> users) {
        this.allUsers = new ArrayList<>(users);
        refreshDisplayedData();
    }

    public List<UserData> getData() {
        return new ArrayList<>(allUsers);
    }

    public void addUser(UserData user) {
        allUsers.add(user);
        refreshDisplayedData();
    }

    public void updateUser(UserData updated) {
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId() == updated.getId()) {
                allUsers.set(i, updated);
                break;
            }
        }
        refreshDisplayedData();
    }

    public void removeUser(int userId) {
        allUsers.removeIf(u -> u.getId() == userId);
        refreshDisplayedData();
    }

    public void clearData() {
        allUsers.clear();
        refreshDisplayedData();
    }

    public void setItemsPerPage(int itemsPerPage) {
        recentReportsPanel.setRowsPerPage(itemsPerPage);
    }

    public void refreshDisplayedData() {
        String nameQuery = nameFilter.getValue().trim().toLowerCase();
        String role = (String) roleFilter.getSelectedItem();
        String purok = (String) purokFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();

        displayedUsers = allUsers.stream().filter(u -> {
            boolean matchesName = nameQuery.isEmpty()
                    || u.getName().toLowerCase().contains(nameQuery);
            boolean matchesRole = role == null
                    || role.startsWith("All ")
                    || role.equalsIgnoreCase(u.getRole());
            boolean matchesPurok = purok == null
                    || purok.startsWith("All ")
                    || purok.equalsIgnoreCase(u.getPurok());
            boolean matchesStatus = status == null
                    || status.startsWith("All ")
                    || status.equalsIgnoreCase(u.getStatus());
            return matchesName && matchesRole && matchesPurok && matchesStatus;
        }).collect(Collectors.toList());

        recentReportsPanel.clearReports();
        for (UserData u : displayedUsers) {
            recentReportsPanel.addReport(new Object[] {
                    u.getName(),
                    u.getRole(),
                    u.getPurok(),
                    u.getPhone(),
                    u.getStatus(),
                    u
            });
        }
        recentReportsPanel.commitReports();
    }
}