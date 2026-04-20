package features.core.dashboardpanel.secretary;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import features.components.GlassPanel;

/**
 * RecentReportsPanel
 *
 * A paginated table panel that displays recent report records inside a
 * dashboard UI. It supports:
 * - Custom table rendering via DashboardTable
 * - Pagination controls (Prev / Next / Jump to page)
 * - Clickable "View" action column handling
 * - External callback binding for row actions
 *
 * The panel maintains an internal dataset and displays only a subset
 * of rows per page.
 */
public class RecentReportsPanel extends GlassPanel {

    // ============================================================
    // DATA (STATE MANAGEMENT)
    // ============================================================

    /** Complete dataset containing all table rows. */
    private final List<Object[]> allData = new ArrayList<>();

    /** Current page index (0-based). */
    private int currentPage = 0;

    /** Number of rows displayed per page. */
    private int rowsPerPage = 6;

    // ============================================================
    // UI COMPONENTS (TABLE)
    // ============================================================

    /** Custom table used for displaying report data. */
    private DashboardTable table;

    /** Table model backing the dashboard table. */
    private DefaultTableModel tableModel;

    /** Title displayed in the panel header. */
    private final String title;

    // ============================================================
    // UI COMPONENTS (PAGINATION CONTROLS)
    // ============================================================

    /** Button for navigating to the previous page. */
    private JButton prevButton;

    /** Button for navigating to the next page. */
    private JButton nextButton;

    /** Label displaying current page information. */
    private JLabel pageLabel;

    /** Input field for jumping to a specific page number. */
    private JTextField jumpField;

    // ============================================================
    // CALLBACK HANDLING
    // ============================================================

    /** External callback for handling "View" button clicks per row. */
    private Consumer<Integer> viewClickCallback = null;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs a RecentReportsPanel with a title and column configuration.
     *
     * Initializes the table, pagination system, and UI layout.
     *
     * @param title       Title displayed on top of the panel.
     * @param columnNames Column headers for the report table.
     */
    public RecentReportsPanel(String title, String[] columnNames) {
        super(new BorderLayout(0, 8));

        this.title = title;

        setBorder(BorderFactory.createEmptyBorder(15, 15, 12, 15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        initializeUI(columnNames);
    }

    // ============================================================
    // INITIALIZATION METHODS
    // ============================================================

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES (initializeUI)
    // ------------------------------------------------------------

    /*
     * scrollPane : JScrollPane
     * Wrapper container that holds the dashboard table.
     */

    /**
     * Initializes the main UI components including:
     * - Header
     * - Table with model
     * - Mouse interaction handlers
     * - Pagination controls
     *
     * @param columnNames Column headers for the table model.
     */
    private void initializeUI(String[] columnNames) {

        add(buildHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new DashboardTable(columnNames);
        table.setModel(tableModel);
        table.setFillsViewportHeight(false);

        // ------------------------------------------------------------
        // MOUSE MOTION (CURSOR HANDLING)
        // ------------------------------------------------------------
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());

                table.setCursor(
                        col == 6
                                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                                : Cursor.getDefaultCursor());
            }
        });

        // ------------------------------------------------------------
        // MOUSE CLICK HANDLING
        // ------------------------------------------------------------
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                table.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());

                if (col == 6 && row >= 0) {
                    onViewClicked(row);
                }
            }
        });

        // ------------------------------------------------------------
        // CELL ALIGNMENT
        // ------------------------------------------------------------
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
        add(buildPaginationBar(), BorderLayout.SOUTH);
    }

    // ============================================================
    // CALLBACK METHODS
    // ============================================================

    /**
     * Sets the callback executed when a "View" action is triggered.
     *
     * @param callback Consumer receiving the row index.
     */
    public void setOnViewClicked(Consumer<Integer> callback) {
        this.viewClickCallback = callback;
    }

    /**
     * Handles "View" click events from table rows.
     *
     * Converts visible row index to absolute dataset index.
     *
     * @param visibleRow Row index from the current page view.
     */
    private void onViewClicked(int visibleRow) {
        if (viewClickCallback != null) {
            viewClickCallback.accept(getAbsoluteRowIndex(visibleRow));
        }
    }

    // ============================================================
    // HEADER UI
    // ============================================================

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES (buildHeader)
    // ------------------------------------------------------------

    /*
     * header : JPanel
     * Container holding the title label.
     *
     * titleLabel : JLabel
     * Displays the panel title text.
     */

    /**
     * Builds the header section of the panel.
     *
     * @return JPanel containing the title label.
     */
    private JPanel buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));

        header.add(titleLabel, BorderLayout.WEST);

        return header;
    }

    // ============================================================
    // PAGINATION BAR (PART 1 ENDS HERE)
    // ============================================================

    /**
     * Builds the pagination control bar including:
     * - Page label
     * - Jump-to-page input
     * - Navigation buttons
     *
     * @return JPanel containing pagination controls.
     */
    private JPanel buildPaginationBar() {

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(new Color(220, 220, 225));

        bar.add(separator, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        controls.setOpaque(false);

        pageLabel = new JLabel();
        pageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        pageLabel.setForeground(new Color(120, 120, 130));

        // NOTE: remaining pagination logic continues in part 2
        // (jump field, buttons, handlers, and updates)

        controls.add(pageLabel);
        bar.add(controls, BorderLayout.CENTER);

        updatePaginationControls();

        return bar;
    }

    // ============================================================
    // NAVIGATION BUTTON CREATION
    // ============================================================

    /**
     * Creates a styled navigation button used in pagination controls.
     *
     * The button supports custom painting for hover, press, and disabled states
     * using rounded rectangle rendering.
     *
     * @param text The label displayed on the button.
     * @return A customized JButton instance.
     */
    private JButton buildNavButton(String text) {

        JButton btn = new JButton(text) {

            // ------------------------------------------------------------
            // METHOD-LEVEL VARIABLES (paintComponent)
            // ------------------------------------------------------------

            /*
             * g2 : Graphics2D
             * Graphics context used for custom button rendering.
             */

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(new Color(240, 240, 243));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(100, 150, 255, 230));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(110, 160, 255, 240));
                } else {
                    g2.setColor(new Color(90, 140, 255, 250));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(72, 28));

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addPropertyChangeListener("enabled",
                evt -> btn.setForeground(
                        btn.isEnabled()
                                ? Color.WHITE
                                : new Color(180, 180, 185)));

        return btn;
    }

    // ============================================================
    // PAGINATION INPUT HANDLING
    // ============================================================

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES (handleJump)
    // ------------------------------------------------------------

    /*
     * text : String
     * Raw input from jumpField.
     *
     * requested : int
     * 1-based page number entered by user.
     *
     * target : int
     * Converted 0-based page index.
     *
     * t : Timer
     * Temporary UI reset timer for invalid input feedback.
     */

    /**
     * Handles jump-to-page input validation and navigation.
     *
     * Accepts user input, validates page range, and updates the table view.
     * Provides visual feedback for invalid inputs.
     */
    private void handleJump() {

        String text = jumpField.getText().trim();

        try {
            int requested = Integer.parseInt(text);
            int target = requested - 1;

            if (target < 0 || target >= getTotalPages()) {

                jumpField.setBackground(new Color(255, 220, 220));

                Timer t = new Timer(350, e -> jumpField.setBackground(new Color(248, 247, 252)));

                t.setRepeats(false);
                t.start();
                return;
            }

            goToPage(target);

            jumpField.setText("pg #");
            jumpField.setForeground(new Color(180, 175, 195));

        } catch (NumberFormatException ex) {

            jumpField.setBackground(new Color(255, 220, 220));

            Timer t = new Timer(350, e -> jumpField.setBackground(new Color(248, 247, 252)));

            t.setRepeats(false);
            t.start();
        }
    }

    // ============================================================
    // PAGINATION LOGIC
    // ============================================================

    /**
     * Calculates total number of pages based on dataset size.
     *
     * @return Total number of pages (minimum 1).
     */
    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) allData.size() / rowsPerPage));
    }

    /**
     * Navigates to a specific page index.
     *
     * Ensures the page index stays within valid bounds.
     *
     * @param page Target page index.
     */
    private void goToPage(int page) {
        currentPage = Math.max(0, Math.min(page, getTotalPages() - 1));
        refreshPage();
    }

    /**
     * Refreshes the table view to display only the rows for the current page.
     *
     * Clears the model and repopulates it using a slice of allData.
     */
    private void refreshPage() {

        tableModel.setRowCount(0);

        int from = currentPage * rowsPerPage;
        int to = Math.min(from + rowsPerPage, allData.size());

        for (int i = from; i < to; i++) {
            tableModel.addRow(allData.get(i));
        }

        updatePaginationControls();
    }

    /**
     * Updates pagination UI elements such as page label and button states.
     */
    private void updatePaginationControls() {

        if (pageLabel == null)
            return;

        int total = getTotalPages();

        pageLabel.setText("Page " + (currentPage + 1) + " of " + total);

        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < total - 1);
    }

    // ============================================================
    // PUBLIC API METHODS
    // ============================================================

    /**
     * Adds a report entry to the dataset and refreshes the view.
     *
     * @param reportData Row data to be added.
     */
    public void addReport(Object[] reportData) {
        allData.add(reportData);
        refreshPage();
    }

    /**
     * Commits batch-added reports and resets view to first page.
     */
    public void commitReports() {
        currentPage = 0;
        refreshPage();
    }

    /**
     * Clears all report data and resets pagination.
     */
    public void clearReports() {
        allData.clear();
        currentPage = 0;
        refreshPage();
    }

    /**
     * Returns the underlying DashboardTable instance.
     *
     * @return The table component.
     */
    public DashboardTable getTable() {
        return table;
    }

    /**
     * Sets the number of rows displayed per page.
     *
     * @param rows Number of rows per page (minimum 1).
     */
    public void setRowsPerPage(int rows) {
        rowsPerPage = Math.max(1, rows);
        currentPage = 0;
        refreshPage();
    }

    /**
     * Converts a visible row index into its absolute dataset index.
     *
     * @param visibleRow Row index on current page.
     * @return Absolute index in full dataset.
     */
    public int getAbsoluteRowIndex(int visibleRow) {
        return currentPage * rowsPerPage + visibleRow;
    }

    /**
     * Assigns a custom button renderer to a specific table column.
     *
     * @param columnIndex Column index to apply renderer.
     * @param buttonText  Text displayed on button.
     * @param buttonColor Background color of button.
     */
    public void setButtonColumn(int columnIndex, String buttonText, Color buttonColor) {
        table.getColumnModel()
                .getColumn(columnIndex)
                .setCellRenderer(new ButtonRenderer(buttonText, buttonColor));
    }
}