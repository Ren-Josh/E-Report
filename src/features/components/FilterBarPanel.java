package features.components;

import features.components.filter.TimeFilter;
import features.components.filter.TimeFilterPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;
import config.UIConfig;

public class FilterBarPanel extends JPanel {

    private TimeFilterPanel timeFilterPanel;
    private UIComboBox<String> categoryCombo;
    private UIComboBox<String> purokCombo;
    private UIComboBox<String> statusCombo;
    private UIInput searchField;
    private UIButton applyButton;
    private JButton moreFiltersButton;
    private JPanel advancedFiltersRow;
    private JPanel activeFiltersChips;

    private final Mode currentMode;
    private boolean advancedFiltersVisible = false;
    private FilterListener filterListener;
    private SearchListener searchListener;

    private static final Color BAR_BG = UIConfig.BG_LIGHT;
    private static final Color TIME_COLOR = UIConfig.ACCENT_BLUE;
    private static final Color CATEGORY_COLOR = UIConfig.ACCENT_PURPLE;
    private static final Color PUROK_COLOR = UIConfig.ACCENT_GREEN;
    private static final Color STATUS_COLOR = UIConfig.ACCENT_ORANGE;
    private static final Color SEARCH_COLOR = UIConfig.ACCENT_TEAL;
    private static final Color FIELD_BG = Color.WHITE;
    private static final Color FIELD_BORDER = UIConfig.BORDER_MEDIUM;
    private static final Color MORE_BTN_COLOR = UIConfig.ACCENT_SLATE;

    private static final int PILL_RADIUS = UIConfig.RADIUS_PILL;
    private static final int FIELD_HEIGHT = UIConfig.COMBOBOX_HEIGHT;
    private static final int H_GAP = UIConfig.XS;

    public enum Mode {
        DASHBOARD, SEARCH
    }

    public interface FilterListener {
        void onApply(Date fromDate, Date toDate, String category, String purok, String status);

        void onReset();
    }

    public interface SearchListener {
        void onSearch(String searchText, String category, Date fromDate, Date toDate);

        void onClearSearch();
    }

    public FilterBarPanel(String[] categories, String[] puroks, String[] statuses, FilterListener listener) {
        this.currentMode = Mode.DASHBOARD;
        this.filterListener = listener;
        initializeDashboard(categories, puroks, statuses);
    }

    public FilterBarPanel(FilterListener listener) {
        this(new String[] { "All Categories", "Theft", "Vandalism", "Scam", "Others" },
                new String[] { "All Puroks", "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" },
                new String[] { "All Statuses", "Submitted", "Pending", "In Progress", "Resolved", "Invalid" },
                listener);
    }

    public FilterBarPanel(String[] categories, SearchListener listener) {
        this.currentMode = Mode.SEARCH;
        this.searchListener = listener;
        initializeSearch(categories);
    }

    public FilterBarPanel(SearchListener listener) {
        this(new String[] { "All Categories", "Theft", "Vandalism", "Scam", "Others" }, listener);
    }

    private void initializeDashboard(String[] categories, String[] puroks, String[] statuses) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, UIConfig.SM + 2, 0));

        JPanel mainBar = new JPanel();
        mainBar.setLayout(new BoxLayout(mainBar, BoxLayout.Y_AXIS));
        mainBar.setOpaque(true);
        mainBar.setBackground(BAR_BG);
        mainBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(UIConfig.SM + 2, UIConfig.XS, UIConfig.SM + 2, UIConfig.XS)));

        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT + 15));
        topRow.setBorder(BorderFactory.createEmptyBorder(UIConfig.XS, 0, UIConfig.XS, 0));

        JPanel leftWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, H_GAP, 0));
        leftWrapper.setOpaque(false);
        leftWrapper.setMaximumSize(new Dimension(800, FIELD_HEIGHT + 10));

        timeFilterPanel = new TimeFilterPanel();
        timeFilterPanel.setFilterType(TimeFilter.FilterType.ALL_TIME);
        addColorIndicator(leftWrapper, timeFilterPanel, TIME_COLOR, "Time Period");

        categoryCombo = new UIComboBox<>(categories);
        UIComboBox.applyPreset(categoryCombo, UIConfig.COMBOBOX_WIDTH_LONG);
        addColorIndicator(leftWrapper, categoryCombo, CATEGORY_COLOR, "Category");

        moreFiltersButton = createMoreFiltersButton();
        moreFiltersButton.addActionListener(e -> toggleAdvancedFilters());
        leftWrapper.add(Box.createHorizontalStrut(6));
        leftWrapper.add(moreFiltersButton);

        topRow.add(leftWrapper);
        topRow.add(Box.createHorizontalGlue());

        JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, H_GAP, 3));
        rightWrapper.setOpaque(false);

        applyButton = createSolidPillButton("Apply", TIME_COLOR);
        applyButton.addActionListener(e -> {
            notifyApply();
            updateActiveChips();
        });
        rightWrapper.add(applyButton);

        UIButton resetBtn = createTextButton("Reset");
        resetBtn.addActionListener(e -> notifyReset());
        rightWrapper.add(resetBtn);

        topRow.add(rightWrapper);
        mainBar.add(topRow);

        advancedFiltersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, H_GAP, 0));
        advancedFiltersRow.setOpaque(true);
        advancedFiltersRow.setBackground(BAR_BG);
        advancedFiltersRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConfig.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(UIConfig.SM, 0, UIConfig.XS, 0)));
        advancedFiltersRow.setVisible(false);
        advancedFiltersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT + 20));

        purokCombo = new UIComboBox<>(puroks);
        UIComboBox.applyPreset(purokCombo, UIConfig.COMBOBOX_WIDTH_STANDARD);
        addColorIndicator(advancedFiltersRow, purokCombo, PUROK_COLOR, "Purok");

        statusCombo = new UIComboBox<>(statuses);
        UIComboBox.applyPreset(statusCombo, UIConfig.COMBOBOX_WIDTH_LONG);
        addColorIndicator(advancedFiltersRow, statusCombo, STATUS_COLOR, "Status");

        mainBar.add(advancedFiltersRow);

        activeFiltersChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        activeFiltersChips.setOpaque(false);
        activeFiltersChips.setVisible(false);
        activeFiltersChips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        mainBar.add(activeFiltersChips);

        add(mainBar, BorderLayout.CENTER);
    }

    private void initializeSearch(String[] categories) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(UIConfig.SM + 2, UIConfig.MD - 4, UIConfig.SM + 2, UIConfig.MD - 4));

        JPanel mainBar = new JPanel(new BorderLayout(0, 0));
        mainBar.setOpaque(true);
        mainBar.setBackground(BAR_BG);
        mainBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(UIConfig.SM + 2, UIConfig.MD - 4, UIConfig.SM + 2, UIConfig.MD - 4)));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, H_GAP, 0));
        row.setOpaque(false);

        searchField = new UIInput(25);
        searchField.setPreferredSize(new Dimension(220, FIELD_HEIGHT));
        searchField.setPlaceholder("Search reports...");
        styleInputAsPill(searchField, SEARCH_COLOR);
        addColorIndicator(row, searchField, SEARCH_COLOR, "Search");

        timeFilterPanel = new TimeFilterPanel();
        timeFilterPanel.setFilterType(TimeFilter.FilterType.ALL_TIME);
        addColorIndicator(row, timeFilterPanel, TIME_COLOR, "Period");

        categoryCombo = new UIComboBox<>(categories);
        UIComboBox.applyPreset(categoryCombo, UIConfig.COMBOBOX_WIDTH_LONG);
        addColorIndicator(row, categoryCombo, CATEGORY_COLOR, "Category");

        mainBar.add(row, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        applyButton = createSolidPillButton("Search", SEARCH_COLOR);
        applyButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        applyButton.addActionListener(e -> notifySearch());
        rightPanel.add(applyButton);
        rightPanel.add(Box.createHorizontalStrut(H_GAP));

        JButton clearBtn = createTextButton("Clear");
        clearBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
        clearBtn.addActionListener(e -> notifyClearSearch());
        rightPanel.add(clearBtn);

        mainBar.add(rightPanel, BorderLayout.EAST);
        add(mainBar, BorderLayout.CENTER);
    }

    private void addColorIndicator(JPanel container, JComponent component, Color color, String tooltip) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, PILL_RADIUS, PILL_RADIUS);
                g2.setColor(FIELD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, PILL_RADIUS, PILL_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);

        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(4, FIELD_HEIGHT - 4));
        indicator.setBackground(color);
        indicator.setOpaque(true);
        indicator.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        wrapper.add(indicator, BorderLayout.WEST);
        wrapper.add(component, BorderLayout.CENTER);
        wrapper.setToolTipText(tooltip);
        container.add(wrapper);
    }

    private void styleInputAsPill(UIInput input, Color accentColor) {
        input.setOpaque(true);
        input.setFont(UIConfig.SMALL);
    }

    private JButton createMoreFiltersButton() {
        JButton btn = new JButton("More ▼") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(200, 205, 215));
                } else if (getModel().isRollover()) {
                    g2.setColor(UIConfig.BG_HOVER);
                } else {
                    g2.setColor(new Color(235, 238, 242));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), PILL_RADIUS, PILL_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(UIConfig.SMALL);
        btn.setForeground(MORE_BTN_COLOR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setPreferredSize(new Dimension(80, FIELD_HEIGHT));
        return btn;
    }

    private UIButton createTextButton(String text) {
        UIButton btn = new UIButton(text, null, new Dimension(80, FIELD_HEIGHT),
                UIConfig.SMALL, PILL_RADIUS, UIButton.ButtonType.OUTLINED);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(MORE_BTN_COLOR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(83, FIELD_HEIGHT));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(new Color(80, 90, 110));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(MORE_BTN_COLOR);
            }
        });
        return btn;
    }

    private UIButton createSolidPillButton(String text, Color bgColor) {
        UIButton btn = new UIButton(text, bgColor, new Dimension(80, FIELD_HEIGHT),
                UIConfig.SMALL, PILL_RADIUS, UIButton.ButtonType.PRIMARY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private void updateActiveChips() {
        activeFiltersChips.removeAll();

        String timeDesc = timeFilterPanel.getFilterDescription();
        boolean isAllTime = timeDesc != null &&
                (timeDesc.equalsIgnoreCase("All Time") || timeDesc.equalsIgnoreCase("All Time Period"));

        if (!isAllTime && !timeDesc.equals(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))) {
            activeFiltersChips.add(createFilterChip(timeDesc, TIME_COLOR, () -> {
                timeFilterPanel.reset();
                timeFilterPanel.setFilterType(TimeFilter.FilterType.ALL_TIME);
                updateActiveChips();
                notifyApply();
            }));
        }

        String cat = (String) categoryCombo.getSelectedItem();
        if (cat != null && !cat.startsWith("All ")) {
            activeFiltersChips.add(createFilterChip(cat, CATEGORY_COLOR, () -> {
                categoryCombo.setSelectedIndex(0);
            }));
        }

        if (advancedFiltersVisible) {
            String purok = (String) purokCombo.getSelectedItem();
            if (purok != null && !purok.startsWith("All ")) {
                activeFiltersChips.add(createFilterChip(purok, PUROK_COLOR, () -> {
                    purokCombo.setSelectedIndex(0);
                }));
            }

            String status = (String) statusCombo.getSelectedItem();
            if (status != null && !status.startsWith("All ")) {
                activeFiltersChips.add(createFilterChip(status, STATUS_COLOR, () -> {
                    statusCombo.setSelectedIndex(0);
                }));
            }
        }

        activeFiltersChips.setVisible(activeFiltersChips.getComponentCount() > 0);
        activeFiltersChips.revalidate();
        activeFiltersChips.repaint();
    }

    private JPanel createFilterChip(String text, Color color, Runnable onRemove) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chip.setOpaque(false);

        JPanel rounded = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rounded.setOpaque(false);
        rounded.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 8));

        JLabel label = new JLabel(text);
        label.setFont(UIConfig.SMALL.deriveFont(11f));
        label.setForeground(Color.WHITE);
        rounded.add(label);

        JLabel remove = new JLabel("×");
        remove.setFont(UIConfig.SMALL_BOLD.deriveFont(13f));
        remove.setForeground(new Color(255, 255, 255, 180));
        remove.setCursor(new Cursor(Cursor.HAND_CURSOR));
        remove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onRemove.run();
                updateActiveChips();
                notifyApply();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                remove.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                remove.setForeground(new Color(255, 255, 255, 180));
            }
        });
        rounded.add(remove);

        chip.add(rounded);
        return chip;
    }

    private void toggleAdvancedFilters() {
        advancedFiltersVisible = !advancedFiltersVisible;
        moreFiltersButton.setText(advancedFiltersVisible ? "Less ▲" : "More ▼");
        advancedFiltersRow.setVisible(advancedFiltersVisible);

        advancedFiltersRow.revalidate();
        Container mainBar = advancedFiltersRow.getParent();
        if (mainBar != null) {
            mainBar.revalidate();
            mainBar.repaint();
        }
        this.revalidate();
        this.repaint();
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private void notifyApply() {
        if (filterListener != null) {
            filterListener.onApply(
                    timeFilterPanel.getStartDate(),
                    timeFilterPanel.getEndDate(),
                    (String) categoryCombo.getSelectedItem(),
                    (String) purokCombo.getSelectedItem(),
                    (String) statusCombo.getSelectedItem());
        }
    }

    private void notifyReset() {
        timeFilterPanel.reset();
        timeFilterPanel.setFilterType(TimeFilter.FilterType.ALL_TIME);
        categoryCombo.setSelectedIndex(0);
        if (purokCombo != null)
            purokCombo.setSelectedIndex(0);
        if (statusCombo != null)
            statusCombo.setSelectedIndex(0);
        if (advancedFiltersVisible)
            toggleAdvancedFilters();
        updateActiveChips();
        if (filterListener != null)
            filterListener.onReset();
    }

    private void notifySearch() {
        if (searchListener != null) {
            searchListener.onSearch(
                    searchField.getValue(),
                    (String) categoryCombo.getSelectedItem(),
                    timeFilterPanel.getStartDate(),
                    timeFilterPanel.getEndDate());
        }
    }

    private void notifyClearSearch() {
        searchField.setText("");
        timeFilterPanel.reset();
        timeFilterPanel.setFilterType(TimeFilter.FilterType.ALL_TIME);
        categoryCombo.setSelectedIndex(0);
        if (searchListener != null)
            searchListener.onClearSearch();
    }

    public TimeFilterPanel getDateFromPicker() {
        return timeFilterPanel;
    }

    public TimeFilterPanel getDateToPicker() {
        return timeFilterPanel;
    }

    public UIComboBox<String> getCategoryCombo() {
        return categoryCombo;
    }

    public UIComboBox<String> getPurokCombo() {
        return purokCombo;
    }

    public UIComboBox<String> getStatusCombo() {
        return statusCombo;
    }

    public UIInput getSearchField() {
        return searchField;
    }

    public String getFromDateString() {
        Date d = timeFilterPanel.getStartDate();
        return d != null ? new java.text.SimpleDateFormat("MM/dd/yyyy").format(d) : "";
    }

    public String getToDateString() {
        Date d = timeFilterPanel.getEndDate();
        return d != null ? new java.text.SimpleDateFormat("MM/dd/yyyy").format(d) : "";
    }

    public String getSearchText() {
        return searchField != null ? searchField.getValue() : "";
    }

    public TimeFilter getTimeFilter() {
        return timeFilterPanel.getTimeFilter();
    }

    public String getTimeFilterDescription() {
        return timeFilterPanel.getFilterDescription();
    }

    public Mode getCurrentMode() {
        return currentMode;
    }
}