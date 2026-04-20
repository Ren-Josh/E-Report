package features.core.dashboardpanel.captain;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import config.UIConfig;
import features.components.UIComboBox;
import features.components.UIButton;
import features.components.UIInput;

/**
 * FilterBarPanel
 *
 * A flexible Swing panel that renders a filter bar in one of two operational
 * modes: DASHBOARD mode or SEARCH mode. The active mode is determined at
 * construction time and is immutable for the lifetime of the panel.
 *
 * In DASHBOARD mode, the panel exposes date range pickers, category, purok,
 * and status dropdowns, along with Apply and Reset action buttons. Filter
 * events are dispatched through a FilterListener callback.
 *
 * In SEARCH mode, the panel exposes a text search field, a category dropdown,
 * and date range pickers, along with Search and Clear action buttons. Search
 * events are dispatched through a SearchListener callback.
 *
 * Both modes share the same date pickers, category combo, and action button
 * references (applyButton, resetButton), but their labels and behaviors differ
 * depending on the active mode.
 *
 * Nested Types:
 * - Mode : Enum defining DASHBOARD and SEARCH operational modes.
 * - FilterListener : Callback interface for DASHBOARD mode filter events.
 * - SearchListener : Callback interface for SEARCH mode search events.
 */
public class FilterBarPanel extends JPanel {

    // -------------------------------------------------------------------------
    // DASHBOARD MODE COMPONENTS
    // -------------------------------------------------------------------------

    /** Date picker for the start of the filter date range. Used in both modes. */
    private ModernDatePicker dateFromPicker;

    /** Date picker for the end of the filter date range. Used in both modes. */
    private ModernDatePicker dateToPicker;

    /** Dropdown for selecting a report category. Used in both modes. */
    private UIComboBox<String> categoryCombo;

    /** Dropdown for selecting a purok. Used in DASHBOARD mode only. */
    private UIComboBox<String> purokCombo;

    /** Dropdown for selecting a report status. Used in DASHBOARD mode only. */
    private UIComboBox<String> statusCombo;

    // -------------------------------------------------------------------------
    // SEARCH MODE COMPONENTS
    // -------------------------------------------------------------------------

    /** Text input field for entering a search query. Used in SEARCH mode only. */
    private UIInput searchField;

    // -------------------------------------------------------------------------
    // SHARED COMPONENTS
    // -------------------------------------------------------------------------

    /**
     * Primary action button. Labeled "Apply" in DASHBOARD mode and "Search"
     * in SEARCH mode.
     */
    private UIButton applyButton;

    /**
     * Secondary action button. Labeled "Reset" in DASHBOARD mode and "Clear"
     * in SEARCH mode.
     */
    private UIButton resetButton;

    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------

    /** Fixed preferred width in pixels for all combo box components. */
    private final int comboBoxSizeWidth = 140;

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    /**
     * The operational mode this panel was initialized with. Set once at
     * construction and never changed afterward.
     */
    private final Mode currentMode;

    /** Callback listener for filter events in DASHBOARD mode. */
    private FilterListener filterListener;

    /** Callback listener for search events in SEARCH mode. */
    private SearchListener searchListener;

    // =========================================================================
    // NESTED TYPES
    // =========================================================================

    /**
     * Mode
     *
     * Defines the two operational modes of FilterBarPanel. The mode is set
     * once at construction and determines which components are initialized
     * and which listener interface is used.
     *
     * Values:
     * DASHBOARD - Renders full filters: date range, category, purok, status,
     * Apply, and Reset buttons. Events dispatched via FilterListener.
     * SEARCH - Renders search input, category dropdown, date range pickers,
     * Search, and Clear buttons. Events dispatched via SearchListener.
     */
    public enum Mode {
        DASHBOARD,
        SEARCH
    }

    /**
     * FilterListener
     *
     * Callback interface for DASHBOARD mode filter events. Implemented by any
     * class that needs to respond to Apply or Reset actions on the filter bar.
     */
    public interface FilterListener {

        /**
         * Called when the Apply button is clicked in DASHBOARD mode.
         *
         * @param fromDate The selected start date from the date range picker.
         * @param toDate   The selected end date from the date range picker.
         * @param category The currently selected category from the combo box.
         * @param purok    The currently selected purok from the combo box.
         * @param status   The currently selected status from the combo box.
         */
        void onApply(Date fromDate, Date toDate, String category, String purok, String status);

        /**
         * Called when the Reset button is clicked in DASHBOARD mode.
         * Signals the listener that all filters have been cleared to defaults.
         */
        void onReset();
    }

    /**
     * SearchListener
     *
     * Callback interface for SEARCH mode search events. Implemented by any
     * class that needs to respond to Search or Clear actions on the filter bar.
     */
    public interface SearchListener {

        /**
         * Called when the Search button is clicked in SEARCH mode.
         *
         * @param searchText The text entered in the search input field.
         * @param category   The currently selected category from the combo box.
         * @param fromDate   The selected start date from the date range picker.
         * @param toDate     The selected end date from the date range picker.
         */
        void onSearch(String searchText, String category, Date fromDate, Date toDate);

        /**
         * Called when the Clear button is clicked in SEARCH mode.
         * Signals the listener that the search input and filters have been reset.
         */
        void onClearSearch();
    }

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    // ========== DASHBOARD MODE CONSTRUCTORS ==================================

    /**
     * Constructs a FilterBarPanel in DASHBOARD mode with custom dropdown options.
     *
     * Sets the mode to DASHBOARD, stores the FilterListener, and initializes
     * the full dashboard filter layout with the provided dropdown data.
     *
     * @param categories Array of category options for the category combo box.
     * @param puroks     Array of purok options for the purok combo box.
     * @param statuses   Array of status options for the status combo box.
     * @param listener   The FilterListener to notify on Apply or Reset actions.
     */
    public FilterBarPanel(String[] categories, String[] puroks, String[] statuses, FilterListener listener) {
        this.currentMode = Mode.DASHBOARD;
        this.filterListener = listener;
        initializeDashboard(categories, puroks, statuses);
    }

    /**
     * Constructs a FilterBarPanel in DASHBOARD mode with default dropdown options.
     *
     * Delegates to the full DASHBOARD constructor using a predefined set of
     * categories, puroks, and statuses.
     *
     * Default categories : "Category", "Theft", "Vandalism", "Scam", "Others"
     * Default puroks : "Purok", "Purok 1" through "Purok 5"
     * Default statuses : "Status", "Submitted", "Pending", "In Progress",
     * "Resolved", "Invalid"
     *
     * @param listener The FilterListener to notify on Apply or Reset actions.
     */
    public FilterBarPanel(FilterListener listener) {
        this(
                new String[] { "Category", "Theft", "Vandalism", "Scam", "Others" },
                new String[] { "Purok", "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" },
                new String[] { "Status", "Submitted", "Pending", "In Progress", "Resolved", "Invalid" },
                listener);
    }

    // ========== SEARCH MODE CONSTRUCTORS =====================================

    /**
     * Constructs a FilterBarPanel in SEARCH mode with custom category options.
     *
     * Sets the mode to SEARCH, stores the SearchListener, and initializes
     * the search filter layout with the provided category data.
     *
     * @param categories Array of category options for the category combo box.
     * @param listener   The SearchListener to notify on Search or Clear actions.
     */
    public FilterBarPanel(String[] categories, SearchListener listener) {
        this.currentMode = Mode.SEARCH;
        this.searchListener = listener;
        initializeSearch(categories);
    }

    /**
     * Constructs a FilterBarPanel in SEARCH mode with default category options.
     *
     * Delegates to the full SEARCH constructor using a predefined set of
     * categories.
     *
     * Default categories : "Category", "Theft", "Vandalism", "Scam", "Others"
     *
     * @param listener The SearchListener to notify on Search or Clear actions.
     */
    public FilterBarPanel(SearchListener listener) {
        this(
                new String[] { "Category", "Theft", "Vandalism", "Scam", "Others" },
                listener);
    }

    // =========================================================================
    // INITIALIZATION METHODS
    // =========================================================================

    // -------------------------------------------------------------------------
    // initializeDashboard — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * No additional local variables beyond the shared instance fields being
     * assigned. All components (dateFromPicker, dateToPicker, categoryCombo,
     * purokCombo, statusCombo) are directly assigned to their instance fields
     * during initialization.
     */

    /**
     * Initializes the panel layout and components for DASHBOARD mode.
     *
     * Arranges a date range picker pair, three combo boxes (category, purok,
     * status), and an Apply/Reset button panel in a left-aligned FlowLayout.
     * Each component is wrapped in a labeled panel via createLabeledComponent.
     *
     * @param categories Array of category options for the category combo box.
     * @param puroks     Array of purok options for the purok combo box.
     * @param statuses   Array of status options for the status combo box.
     */
    private void initializeDashboard(String[] categories, String[] puroks, String[] statuses) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        setOpaque(false);

        dateFromPicker = new ModernDatePicker();
        add(createLabeledComponent("From:", dateFromPicker));

        dateToPicker = new ModernDatePicker(new Date());
        add(createLabeledComponent("To:", dateToPicker));

        categoryCombo = new UIComboBox<>(categories);
        categoryCombo.setPreferredSize(new Dimension(comboBoxSizeWidth, 32));
        add(createLabeledComponent("Category:", categoryCombo));

        purokCombo = new UIComboBox<>(puroks);
        purokCombo.setPreferredSize(new Dimension(comboBoxSizeWidth, 32));
        add(createLabeledComponent("Purok:", purokCombo));

        statusCombo = new UIComboBox<>(statuses);
        statusCombo.setPreferredSize(new Dimension(comboBoxSizeWidth, 32));
        add(createLabeledComponent("Status:", statusCombo));

        add(createButtonPanel(true));
    }

    // -------------------------------------------------------------------------
    // initializeSearch — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * No additional local variables beyond the shared instance fields being
     * assigned. searchField, categoryCombo, dateFromPicker, and dateToPicker
     * are directly assigned to their instance fields during initialization.
     */

    /**
     * Initializes the panel layout and components for SEARCH mode.
     *
     * Arranges a text search field, category combo box, date range picker pair,
     * and a Search/Clear button panel in a left-aligned FlowLayout. Each
     * component is wrapped in a labeled panel via createLabeledComponent.
     * The search field is pre-configured with a placeholder text and a preferred
     * width of 200px.
     *
     * @param categories Array of category options for the category combo box.
     */
    private void initializeSearch(String[] categories) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        setOpaque(false);

        searchField = new UIInput(20);
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.setPlaceholder("Search reports...");
        add(createLabeledComponent("Search:", searchField));

        categoryCombo = new UIComboBox<>(categories);
        categoryCombo.setPreferredSize(new Dimension(comboBoxSizeWidth, 32));
        add(createLabeledComponent("Category:", categoryCombo));

        dateFromPicker = new ModernDatePicker();
        add(createLabeledComponent("From:", dateFromPicker));

        dateToPicker = new ModernDatePicker(new Date());
        add(createLabeledComponent("To:", dateToPicker));

        add(createButtonPanel(false));
    }

    // -------------------------------------------------------------------------
    // createButtonPanel — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * buttonPanel : JPanel
     * Wrapper panel using FlowLayout (LEFT-aligned, 8px horizontal gap)
     * with an empty border of 18px top, 10px left padding to vertically
     * align buttons with their sibling labeled components.
     * Holds applyButton and resetButton.
     */

    /**
     * Creates and returns a button panel containing the primary and secondary
     * action buttons, configured for the specified mode.
     *
     * In DASHBOARD mode (isDashboardMode = true):
     * - applyButton is labeled "Apply" with a blue PRIMARY style.
     * - resetButton is labeled "Reset" with a white OUTLINED style.
     * - Buttons dispatch to notifyApply() and notifyReset() respectively.
     *
     * In SEARCH mode (isDashboardMode = false):
     * - applyButton is labeled "Search" with a blue PRIMARY style.
     * - resetButton is labeled "Clear" with a white OUTLINED style.
     * - Buttons dispatch to notifySearch() and notifyClearSearch() respectively.
     *
     * Both buttons are sized at 90x32 pixels and use UIConfig.BODY font.
     *
     * @param isDashboardMode true to configure for DASHBOARD mode;
     *                        false to configure for SEARCH mode.
     * @return A JPanel containing the configured action buttons.
     */
    private JPanel createButtonPanel(boolean isDashboardMode) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(18, 10, 0, 0));

        if (isDashboardMode) {
            applyButton = new UIButton(
                    "Apply",
                    new Color(25, 118, 210),
                    new Dimension(90, 32),
                    UIConfig.BODY,
                    8,
                    UIButton.ButtonType.PRIMARY);

            resetButton = new UIButton(
                    "Reset",
                    Color.WHITE,
                    new Dimension(90, 32),
                    UIConfig.BODY,
                    8,
                    UIButton.ButtonType.OUTLINED);

            applyButton.addActionListener(e -> notifyApply());
            resetButton.addActionListener(e -> notifyReset());

            buttonPanel.add(applyButton);
            buttonPanel.add(resetButton);
        } else {
            applyButton = new UIButton(
                    "Search",
                    new Color(25, 118, 210),
                    new Dimension(90, 32),
                    UIConfig.BODY,
                    8,
                    UIButton.ButtonType.PRIMARY);

            resetButton = new UIButton(
                    "Clear",
                    Color.WHITE,
                    new Dimension(90, 32),
                    UIConfig.BODY,
                    8,
                    UIButton.ButtonType.OUTLINED);

            applyButton.addActionListener(e -> notifySearch());
            resetButton.addActionListener(e -> notifyClearSearch());

            buttonPanel.add(applyButton);
            buttonPanel.add(resetButton);
        }

        return buttonPanel;
    }

    // -------------------------------------------------------------------------
    // createLabeledComponent — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * panel : JPanel
     * Wrapper panel using BorderLayout with 3px horizontal and vertical
     * gaps. Holds the label (NORTH) and the provided component (CENTER).
     *
     * label : JLabel
     * Displays the provided labelText above the component using
     * UIConfig.BODY font in muted gray color (100, 100, 100).
     */

    /**
     * Creates and returns a labeled wrapper panel containing a descriptive
     * label above the given component.
     *
     * The label is placed in the NORTH position and the component in the
     * CENTER position of a BorderLayout panel, producing a vertically stacked
     * label-above-input layout.
     *
     * @param labelText The text to display as the label above the component.
     * @param component The Swing component to place below the label.
     * @return A JPanel with the label stacked above the component.
     */
    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UIConfig.BODY);
        label.setForeground(new Color(100, 100, 100));

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // NOTIFICATION METHODS
    // =========================================================================

    // -------------------------------------------------------------------------
    // notifyApply — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * fromDate : Date
     * The currently selected start date retrieved from dateFromPicker.
     *
     * toDate : Date
     * The currently selected end date retrieved from dateToPicker.
     */

    /**
     * Collects the current filter values and dispatches them to the
     * FilterListener via onApply if a listener is registered.
     *
     * Reads the selected dates from both date pickers and the selected items
     * from the category, purok, and status combo boxes.
     */
    private void notifyApply() {
        if (filterListener != null) {
            Date fromDate = dateFromPicker.getDate();
            Date toDate = dateToPicker.getDate();

            filterListener.onApply(
                    fromDate,
                    toDate,
                    (String) categoryCombo.getSelectedItem(),
                    (String) purokCombo.getSelectedItem(),
                    (String) statusCombo.getSelectedItem());
        }
    }

    // -------------------------------------------------------------------------
    // notifyReset — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * today : Date
     * A new Date instance representing the current system date and time,
     * used to reset both date pickers back to today.
     */

    /**
     * Resets all DASHBOARD mode filter components to their default state and
     * notifies the FilterListener via onReset if a listener is registered.
     *
     * Both date pickers are reset to today's date. All three combo boxes are
     * reset to their first item (index 0).
     */
    private void notifyReset() {
        Date today = new Date();
        dateFromPicker.setDate(today);
        dateToPicker.setDate(today);

        categoryCombo.setSelectedIndex(0);
        purokCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);

        if (filterListener != null) {
            filterListener.onReset();
        }
    }

    /**
     * Collects the current search values and dispatches them to the
     * SearchListener via onSearch if a listener is registered.
     *
     * Reads the search text from the search field, the selected category from
     * the category combo box, and the selected dates from both date pickers.
     */
    private void notifySearch() {
        if (searchListener != null) {
            searchListener.onSearch(
                    searchField.getValue(),
                    (String) categoryCombo.getSelectedItem(),
                    dateFromPicker.getDate(),
                    dateToPicker.getDate());
        }
    }

    // -------------------------------------------------------------------------
    // notifyClearSearch — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * today : Date
     * A new Date instance representing the current system date and time,
     * used to reset both date pickers back to today.
     */

    /**
     * Resets all SEARCH mode filter components to their default state and
     * notifies the SearchListener via onClearSearch if a listener is registered.
     *
     * The search field is cleared, both date pickers are reset to today's date,
     * and the category combo box is reset to its first item (index 0).
     */
    private void notifyClearSearch() {
        searchField.setText("");

        Date today = new Date();
        dateFromPicker.setDate(today);
        dateToPicker.setDate(today);

        categoryCombo.setSelectedIndex(0);

        if (searchListener != null) {
            searchListener.onClearSearch();
        }
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /**
     * Returns the operational mode this panel was constructed with.
     *
     * @return The current Mode (DASHBOARD or SEARCH).
     */
    public Mode getCurrentMode() {
        return currentMode;
    }

    /**
     * Returns the date picker for the start of the filter date range.
     *
     * @return The ModernDatePicker instance for the "From" date.
     */
    public ModernDatePicker getDateFromPicker() {
        return dateFromPicker;
    }

    /**
     * Returns the date picker for the end of the filter date range.
     *
     * @return The ModernDatePicker instance for the "To" date.
     */
    public ModernDatePicker getDateToPicker() {
        return dateToPicker;
    }

    /**
     * Returns the category combo box.
     *
     * @return The UIComboBox instance for category selection.
     */
    public UIComboBox<String> getCategoryCombo() {
        return categoryCombo;
    }

    /**
     * Returns the purok combo box. Available in DASHBOARD mode only.
     *
     * @return The UIComboBox instance for purok selection, or null if in
     *         SEARCH mode.
     */
    public UIComboBox<String> getPurokCombo() {
        return purokCombo;
    }

    /**
     * Returns the status combo box. Available in DASHBOARD mode only.
     *
     * @return The UIComboBox instance for status selection, or null if in
     *         SEARCH mode.
     */
    public UIComboBox<String> getStatusCombo() {
        return statusCombo;
    }

    /**
     * Returns the search input field. Available in SEARCH mode only.
     *
     * @return The UIInput instance for the search field, or null if in
     *         DASHBOARD mode.
     */
    public UIInput getSearchField() {
        return searchField;
    }

    /**
     * Returns the currently selected start date as a formatted string.
     *
     * @return A String representation of the "From" date from the date picker.
     */
    public String getFromDateString() {
        return dateFromPicker.getDateString();
    }

    /**
     * Returns the currently selected end date as a formatted string.
     *
     * @return A String representation of the "To" date from the date picker.
     */
    public String getToDateString() {
        return dateToPicker.getDateString();
    }

    /**
     * Returns the current text value from the search input field.
     * Returns an empty string if the search field has not been initialized
     * (i.e., the panel is in DASHBOARD mode).
     *
     * @return The search text string, or an empty string if searchField is null.
     */
    public String getSearchText() {
        return searchField != null ? searchField.getValue() : "";
    }
}