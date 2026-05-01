package features.core.dashboardpanel.captain.panels;

import javax.swing.*;

import config.UIConfig;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.Calendar;

/**
 * ModernDatePicker
 *
 * A custom Swing date picker component that combines a formatted text field
 * with a popup calendar interface for date selection.
 *
 * This component allows users to:
 * - Manually input a date with strict validation
 * - Open a calendar popup for visual selection
 * - Maintain synchronization between text input and internal date state
 *
 * The component enforces a fixed date format (MM/dd/yyyy) and provides
 * visual feedback when invalid input is detected.
 */
public class ModernDatePicker extends JPanel {

    // -------------------------------------------------------------------------
    // COMPONENTS
    // -------------------------------------------------------------------------

    /** Text field used for displaying and entering formatted date values. */
    private JFormattedTextField dateField;

    /** Button used to trigger the calendar popup. */
    private JButton calendarButton;

    /** Popup menu that holds the calendar panel. */
    private JPopupMenu popup;

    // -------------------------------------------------------------------------
    // DATE / FORMAT VARIABLES
    // -------------------------------------------------------------------------

    /** Formatter used for parsing and displaying date values. */
    private SimpleDateFormat dateFormat;

    /** The currently selected and validated date. */
    private Date currentDate;

    /** Calendar instance representing the currently displayed month. */
    private Calendar displayedMonth;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Constructs a ModernDatePicker initialized with the current system date.
     */
    public ModernDatePicker() {
        this(new Date());
    }

    /**
     * Constructs a ModernDatePicker with a specified initial date.
     *
     * Initializes:
     * - currentDate (fallback to current date if null)
     * - dateFormat (MM/dd/yyyy)
     * - displayedMonth (aligned with currentDate)
     *
     * @param initialDate The initial date to display; defaults to current date if
     *                    null.
     */
    public ModernDatePicker(Date initialDate) {
        this.currentDate = initialDate != null ? initialDate : new Date();
        this.dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        this.displayedMonth = Calendar.getInstance();
        this.displayedMonth.setTime(currentDate);

        initialize();
    }

    // =========================================================================
    // INITIALIZATION METHODS
    // =========================================================================

    // -------------------------------------------------------------------------
    // initialize — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * No additional local variables. All UI components are directly
     * assigned to instance fields.
     */

    /**
     * Initializes the UI components and layout of the date picker.
     *
     * Responsibilities:
     * - Configure layout and transparency
     * - Initialize formatted text field with strict validation
     * - Attach focus listener for validation on focus loss
     * - Create custom-rendered calendar button
     * - Register action listener to open calendar popup
     */
    private void initialize() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        // Formatted text field for date input
        dateFormat.setLenient(false);
        dateField = new JFormattedTextField(dateFormat);
        dateField.setValue(currentDate);
        dateField.setFont(UIConfig.BODY);
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 6)));
        dateField.setPreferredSize(new Dimension(100, 32));
        dateField.setHorizontalAlignment(SwingConstants.CENTER);

        // Validation on focus loss
        dateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateDate();
            }
        });

        // Calendar popup button - custom drawn
        calendarButton = new JButton() {

            // -----------------------------------------------------------------
            // paintComponent — METHOD-LEVEL VARIABLES
            // -----------------------------------------------------------------

            /*
             * g2 : Graphics2D
             * Used for anti-aliased rendering of background and border.
             *
             * cx, cy : int
             * Center coordinates used to draw the calendar icon.
             */

            /**
             * Custom rendering of the calendar button.
             *
             * Draws:
             * - Rounded background with hover/pressed states
             * - Border
             * - Minimalist calendar icon using lines
             *
             * @param g Graphics context used for painting
             */
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background
                g2.setColor(getModel().isPressed() ? new Color(230, 230, 230)
                        : getModel().isRollover() ? new Color(240, 240, 240)
                                : new Color(245, 245, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Border
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                g2.dispose();

                // Draw calendar icon
                g.setColor(new Color(80, 80, 80));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g.drawRect(cx - 6, cy - 4, 12, 10);
                g.drawLine(cx - 6, cy - 4, cx + 6, cy - 4);
                g.drawLine(cx - 3, cy - 4, cx - 3, cy - 7);
                g.drawLine(cx + 3, cy - 4, cx + 3, cy - 7);
            }
        };

        calendarButton.setPreferredSize(new Dimension(32, 32));
        calendarButton.setContentAreaFilled(false);
        calendarButton.setBorderPainted(false);
        calendarButton.setFocusPainted(false);
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarButton.setToolTipText("Select date");

        calendarButton.addActionListener(e -> showCalendarPopup());

        add(dateField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);

        setPreferredSize(new Dimension(140, 32));
    }

    // =========================================================================
    // VALIDATION METHODS
    // =========================================================================

    // -------------------------------------------------------------------------
    // validateDate — METHOD-LEVEL VARIABLES
    // -------------------------------------------------------------------------

    /*
     * date : Date
     * Parsed date value from the text field.
     */

    /**
     * Validates the current value of the date field.
     *
     * If parsing succeeds:
     * - Updates currentDate
     * - Updates displayedMonth
     * - Applies normal border styling
     *
     * If parsing fails:
     * - Applies error border styling (red)
     */
    private void validateDate() {
        try {
            Date date = dateFormat.parse(dateField.getText());
            dateField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 6)));
            currentDate = date;
            displayedMonth.setTime(date);
        } catch (ParseException ex) {
            dateField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 60, 60), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 6)));
        }
    }

    // =========================================================================
    // POPUP METHODS
    // =========================================================================

    /**
     * Displays or hides the calendar popup.
     *
     * Behavior:
     * - Validates current input before opening
     * - Toggles visibility if popup is already open
     * - Creates a new popup with calendar panel if not initialized
     */
    private void showCalendarPopup() {
        validateDate();

        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
            return;
        }

        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        popup.add(createCalendarPanel());
        popup.show(this, 0, getHeight());
    }

    /**
     * Creates the full calendar panel UI.
     *
     * Variables:
     * panel, header, daysContainer, headersPanel, footer : JPanel
     * prevMonth, nextMonth, todayBtn : JButton
     * monthLabel : JLabel
     *
     * @return JPanel containing calendar layout
     */
    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setPreferredSize(new Dimension(280, 300));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JButton prevMonth = createNavButton("<=");
        JButton nextMonth = createNavButton("=>");

        JLabel monthLabel = new JLabel(getMonthYearString(displayedMonth), SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        monthLabel.setForeground(new Color(50, 50, 50));

        prevMonth.addActionListener(e -> {
            displayedMonth.add(Calendar.MONTH, -1);
            monthLabel.setText(getMonthYearString(displayedMonth));
            refreshDaysPanel(panel);
        });

        nextMonth.addActionListener(e -> {
            displayedMonth.add(Calendar.MONTH, 1);
            monthLabel.setText(getMonthYearString(displayedMonth));
            refreshDaysPanel(panel);
        });

        header.add(prevMonth, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextMonth, BorderLayout.EAST);

        JPanel daysContainer = new JPanel(new BorderLayout());
        daysContainer.setOpaque(false);

        JPanel headersPanel = new JPanel(new GridLayout(1, 7, 2, 2));
        headersPanel.setOpaque(false);

        String[] dayNames = { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" };
        for (String day : dayNames) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(120, 120, 120));
            headersPanel.add(label);
        }

        JPanel daysPanel = createDaysGrid();
        daysPanel.setName("daysGrid");

        daysContainer.add(headersPanel, BorderLayout.NORTH);
        daysContainer.add(daysPanel, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(daysContainer, BorderLayout.CENTER);

        JButton todayBtn = new JButton("Today");
        todayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        todayBtn.setForeground(new Color(25, 118, 210));
        todayBtn.setContentAreaFilled(false);
        todayBtn.setBorderPainted(false);
        todayBtn.setFocusPainted(false);
        todayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        todayBtn.addActionListener(e -> {
            Date today = new Date();
            dateField.setValue(today);
            currentDate = today;
            displayedMonth.setTime(today);
            popup.setVisible(false);
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(todayBtn);

        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the grid of day buttons for the current month.
     *
     * @return JPanel containing day cells
     */
    private JPanel createDaysGrid() {
        JPanel daysPanel = new JPanel(new GridLayout(6, 7, 3, 3));
        daysPanel.setOpaque(false);
        daysPanel.setName("daysGrid");

        Calendar cal = (Calendar) displayedMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(currentDate);

        for (int i = 0; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;

            JPanel dayCell = new JPanel(new GridBagLayout());
            dayCell.setOpaque(false);

            JButton dayBtn = new JButton(String.valueOf(currentDay));
            dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayBtn.setFocusPainted(false);
            dayBtn.setBorderPainted(false);
            dayBtn.setContentAreaFilled(false);
            dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dayBtn.setPreferredSize(new Dimension(30, 30));

            boolean isToday = today.get(Calendar.YEAR) == displayedMonth.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == displayedMonth.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == day;

            boolean isSelected = selectedCal.get(Calendar.YEAR) == displayedMonth.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.MONTH) == displayedMonth.get(Calendar.MONTH) &&
                    selectedCal.get(Calendar.DAY_OF_MONTH) == day;

            if (isSelected) {
                dayBtn.setBackground(new Color(25, 118, 210));
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setOpaque(true);
            } else if (isToday) {
                dayBtn.setForeground(new Color(25, 118, 210));
                dayBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }

            dayBtn.addActionListener(e -> {
                Calendar newDate = (Calendar) displayedMonth.clone();
                newDate.set(Calendar.DAY_OF_MONTH, currentDay);
                dateField.setValue(newDate.getTime());
                currentDate = newDate.getTime();
                popup.setVisible(false);
            });

            dayCell.add(dayBtn);
            daysPanel.add(dayCell);
        }

        return daysPanel;
    }

    /**
     * Refreshes the calendar grid when navigating months.
     *
     * @param calendarPanel the parent calendar panel
     */
    private void refreshDaysPanel(JPanel calendarPanel) {
        BorderLayout layout = (BorderLayout) calendarPanel.getLayout();
        JPanel daysContainer = (JPanel) layout.getLayoutComponent(BorderLayout.CENTER);

        for (Component c : daysContainer.getComponents()) {
            if ("daysGrid".equals(c.getName())) {
                daysContainer.remove(c);
                break;
            }
        }

        daysContainer.add(createDaysGrid(), BorderLayout.CENTER);
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    /**
     * Creates a navigation button for month switching.
     *
     * @param text button label
     * @return JButton configured navigation button
     */
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setForeground(new Color(80, 80, 80));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Formats the displayed month and year.
     *
     * @param cal Calendar instance
     * @return formatted string (e.g., "April 2026")
     */
    private String getMonthYearString(Calendar cal) {
        String[] months = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };
        return months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
    }

    /**
     * Returns the currently selected date.
     *
     * @return Date object
     */
    public Date getDate() {
        validateDate();
        return currentDate;
    }

    /**
     * Sets a new date value.
     *
     * @param date new date to set
     */
    public void setDate(Date date) {
        if (date != null) {
            dateField.setValue(date);
            currentDate = date;
            displayedMonth.setTime(date);
        }
    }

    /**
     * Returns the formatted date string.
     *
     * @return date as String
     */
    public String getDateString() {
        return dateField.getText();
    }
}