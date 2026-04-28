package features.components.filter;

import features.components.UIComboBox;
import config.UIConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;

public class TimeFilterPanel extends JPanel {

    private UIComboBox<String> filterTypeCombo;
    private UIComboBox<Integer> yearCombo;
    private UIComboBox<Integer> startYearCombo;
    private UIComboBox<Integer> endYearCombo;
    private UIComboBox<String> monthCombo;
    private UIComboBox<String> startMonthCombo;
    private UIComboBox<String> endMonthCombo;
    private UIComboBox<Integer> weekCombo;

    private int currentYear;
    private TimeFilter currentFilter;
    private java.util.List<FilterChangeListener> listeners = new java.util.ArrayList<>();

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private static final String[] FILTER_TYPES = {
            "All Time",
            "Span of Years",
            "Single Year",
            "Span of Months",
            "Single Month",
            "Single Week"
    };

    public TimeFilterPanel() {
        this.currentYear = Calendar.getInstance().get(Calendar.YEAR);
        initialize();
        setFilterType(TimeFilter.FilterType.ALL_TIME);
    }

    public TimeFilterPanel(TimeFilter initialFilter) {
        this.currentYear = Calendar.getInstance().get(Calendar.YEAR);
        this.currentFilter = initialFilter;
        initialize();
        syncUIFromFilter(initialFilter);
    }

    private void initialize() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setOpaque(false);

        filterTypeCombo = new UIComboBox<>(FILTER_TYPES);
        UIComboBox.applyPreset(filterTypeCombo, UIConfig.COMBOBOX_WIDTH_STANDARD);
        filterTypeCombo.addActionListener(e -> onFilterTypeChanged());
        add(wrapWithLabel("Filter By:", filterTypeCombo));

        startYearCombo = createYearCombo(currentYear - 5, currentYear + 5, UIConfig.COMBOBOX_WIDTH_STANDARD);
        endYearCombo = createYearCombo(currentYear - 5, currentYear + 5, UIConfig.COMBOBOX_WIDTH_STANDARD);
        add(wrapWithLabel("Start Year:", startYearCombo));
        add(wrapWithLabel("End Year:", endYearCombo));

        yearCombo = createYearCombo(currentYear - 5, currentYear + 5, UIConfig.COMBOBOX_WIDTH_STANDARD);
        yearCombo.setSelectedItem(currentYear);
        add(wrapWithLabel("Year:", yearCombo));

        startMonthCombo = new UIComboBox<>(MONTH_NAMES);
        UIComboBox.applyPreset(startMonthCombo, UIConfig.COMBOBOX_WIDTH_MEDIUM);

        endMonthCombo = new UIComboBox<>(MONTH_NAMES);
        UIComboBox.applyPreset(endMonthCombo, UIConfig.COMBOBOX_WIDTH_MEDIUM);

        monthCombo = new UIComboBox<>(MONTH_NAMES);
        UIComboBox.applyPreset(monthCombo, UIConfig.COMBOBOX_WIDTH_MEDIUM);

        add(wrapWithLabel("Start Month:", startMonthCombo));
        add(wrapWithLabel("End Month:", endMonthCombo));
        add(wrapWithLabel("Month:", monthCombo));

        Integer[] weeks = { 1, 2, 3, 4, 5, 6 };
        weekCombo = new UIComboBox<>(weeks);
        UIComboBox.applyPreset(weekCombo, UIConfig.COMBOBOX_WIDTH_SHORT);
        add(wrapWithLabel("Week:", weekCombo));

        addChangeListeners();
        updateVisibility();
    }

    private UIComboBox<Integer> createYearCombo(int startYear, int endYear, int width) {
        java.util.List<Integer> years = new java.util.ArrayList<>();
        for (int y = startYear; y <= endYear; y++)
            years.add(y);

        UIComboBox<Integer> combo = new UIComboBox<>(years.toArray(new Integer[0]));
        UIComboBox.applyPreset(combo, width);
        return combo;
    }

    private JPanel wrapWithLabel(String text, JComponent component) {
        component.setToolTipText(text);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private void addChangeListeners() {
        ActionListener updateListener = e -> updateCurrentFilter();
        filterTypeCombo.addActionListener(updateListener);
        yearCombo.addActionListener(updateListener);
        startYearCombo.addActionListener(updateListener);
        endYearCombo.addActionListener(updateListener);
        monthCombo.addActionListener(updateListener);
        startMonthCombo.addActionListener(updateListener);
        endMonthCombo.addActionListener(updateListener);
        weekCombo.addActionListener(updateListener);
    }

    private void onFilterTypeChanged() {
        updateVisibility();
        updateCurrentFilter();
    }

    private void updateVisibility() {
        int typeIndex = filterTypeCombo.getSelectedIndex();

        startYearCombo.getParent().setVisible(false);
        endYearCombo.getParent().setVisible(false);
        yearCombo.getParent().setVisible(false);
        startMonthCombo.getParent().setVisible(false);
        endMonthCombo.getParent().setVisible(false);
        monthCombo.getParent().setVisible(false);
        weekCombo.getParent().setVisible(false);

        switch (typeIndex) {
            case 0 -> {
            }
            case 1 -> {
                startYearCombo.getParent().setVisible(true);
                endYearCombo.getParent().setVisible(true);
            }
            case 2 -> yearCombo.getParent().setVisible(true);
            case 3 -> {
                yearCombo.getParent().setVisible(true);
                startMonthCombo.getParent().setVisible(true);
                endMonthCombo.getParent().setVisible(true);
            }
            case 4 -> {
                yearCombo.getParent().setVisible(true);
                monthCombo.getParent().setVisible(true);
            }
            case 5 -> {
                yearCombo.getParent().setVisible(true);
                monthCombo.getParent().setVisible(true);
                weekCombo.getParent().setVisible(true);
            }
        }

        revalidate();
        repaint();
    }

    private void updateCurrentFilter() {
        int typeIndex = filterTypeCombo.getSelectedIndex();
        TimeFilter newFilter = null;

        try {
            switch (typeIndex) {
                case 0 -> newFilter = TimeFilter.forAllTime();
                case 1 -> {
                    int sYear = (Integer) startYearCombo.getSelectedItem();
                    int eYear = (Integer) endYearCombo.getSelectedItem();
                    if (sYear > eYear) {
                        int temp = sYear;
                        sYear = eYear;
                        eYear = temp;
                    }
                    newFilter = TimeFilter.forYearSpan(sYear, eYear);
                }
                case 2 -> newFilter = TimeFilter.forSingleYear((Integer) yearCombo.getSelectedItem());
                case 3 -> {
                    int year = (Integer) yearCombo.getSelectedItem();
                    int sMonth = startMonthCombo.getSelectedIndex();
                    int eMonth = endMonthCombo.getSelectedIndex();
                    if (sMonth > eMonth) {
                        int temp = sMonth;
                        sMonth = eMonth;
                        eMonth = temp;
                    }
                    newFilter = TimeFilter.forMonthSpan(year, sMonth, eMonth);
                }
                case 4 -> newFilter = TimeFilter.forSingleMonth(
                        (Integer) yearCombo.getSelectedItem(),
                        monthCombo.getSelectedIndex());
                case 5 -> newFilter = TimeFilter.forSingleWeek(
                        (Integer) yearCombo.getSelectedItem(),
                        monthCombo.getSelectedIndex(),
                        (Integer) weekCombo.getSelectedItem());
            }
        } catch (Exception ex) {
            newFilter = TimeFilter.forAllTime();
        }

        if (newFilter != null && !newFilter.equals(currentFilter)) {
            currentFilter = newFilter;
            notifyListeners();
        }
    }

    private void syncUIFromFilter(TimeFilter filter) {
        if (filter == null)
            return;

        filterTypeCombo.setSelectedIndex(filter.getFilterType().ordinal());

        switch (filter.getFilterType()) {
            case ALL_TIME -> {
            }
            case SPAN_OF_YEARS -> {
                startYearCombo.setSelectedItem(filter.getStartYear());
                endYearCombo.setSelectedItem(filter.getEndYear());
            }
            case SINGLE_YEAR -> yearCombo.setSelectedItem(filter.getYear());
            case SPAN_OF_MONTHS -> {
                yearCombo.setSelectedItem(filter.getYear());
                startMonthCombo.setSelectedIndex(filter.getStartMonth());
                endMonthCombo.setSelectedIndex(filter.getEndMonth());
            }
            case SINGLE_MONTH -> {
                yearCombo.setSelectedItem(filter.getYear());
                monthCombo.setSelectedIndex(filter.getMonth());
            }
            case SINGLE_WEEK -> {
                yearCombo.setSelectedItem(filter.getYear());
                monthCombo.setSelectedIndex(filter.getMonth());
                weekCombo.setSelectedItem(filter.getWeekOfMonth());
            }
        }

        updateVisibility();
    }

    public interface FilterChangeListener {
        void onFilterChanged(TimeFilter newFilter);
    }

    public void addFilterChangeListener(FilterChangeListener listener) {
        listeners.add(listener);
    }

    public void removeFilterChangeListener(FilterChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (FilterChangeListener listener : listeners) {
            listener.onFilterChanged(currentFilter);
        }
    }

    public TimeFilter getTimeFilter() {
        return currentFilter;
    }

    public void setFilterType(TimeFilter.FilterType type) {
        filterTypeCombo.setSelectedIndex(type.ordinal());
    }

    public Date getStartDate() {
        if (currentFilter == null)
            updateCurrentFilter();
        return currentFilter != null ? currentFilter.getStartDate() : null;
    }

    public Date getEndDate() {
        if (currentFilter == null)
            updateCurrentFilter();
        return currentFilter != null ? currentFilter.getEndDate() : null;
    }

    public String getFilterDescription() {
        return currentFilter != null ? currentFilter.getDescription() : "All Time";
    }

    public void reset() {
        filterTypeCombo.setSelectedIndex(0);
        updateCurrentFilter();
    }
}