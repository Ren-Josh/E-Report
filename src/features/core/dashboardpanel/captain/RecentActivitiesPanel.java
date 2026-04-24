package features.core.dashboardpanel.captain;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * RecentActivitiesPanel
 *
 * A UI panel that displays a vertical list of recent activity items inside
 * a scrollable container. Each activity is rendered as a row containing:
 * - A colored indicator icon
 * - Title and description text
 * - Time and date metadata aligned to the right
 *
 * This class extends BaseCardPanel and is used to present structured
 * activity logs in a dashboard-style UI.
 */
public class RecentActivitiesPanel extends BaseCardPanel {

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs a RecentActivitiesPanel with a title and list of activities.
     *
     * Initializes the layout, creates a title header, and populates a scrollable
     * list of activity rows. Each activity is separated visually using a
     * JSeparator for readability.
     *
     * @param title      The title displayed at the top of the panel.
     * @param activities The list of ActivityItem objects to display.
     */
    public RecentActivitiesPanel(String title, List<ActivityItem> activities) {
        super(title);

        setLayout(new BorderLayout(10, 10));

        // ------------------------------------------------------------
        // TITLE LABEL
        // ------------------------------------------------------------
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));
        add(titleLabel, BorderLayout.NORTH);

        // ------------------------------------------------------------
        // LIST PANEL (CONTAINER FOR ACTIVITY ITEMS)
        // ------------------------------------------------------------
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (int i = 0; i < activities.size(); i++) {

            ActivityItem item = activities.get(i);

            JPanel activityRow = createActivityRow(item);
            listPanel.add(activityRow);

            // Add separator between items except last item
            if (i < activities.size() - 1) {
                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(224, 224, 224));
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

                listPanel.add(sep);
                listPanel.add(Box.createVerticalStrut(5));
            }
        }

        // ------------------------------------------------------------
        // SCROLL PANE WRAPPER
        // ------------------------------------------------------------
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ============================================================
    // METHOD: createActivityRow
    // ============================================================

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES
    // ------------------------------------------------------------

    /*
     * row : JPanel
     * Root container for a single activity entry using BorderLayout.
     *
     * leftPanel : JPanel
     * Holds icon + text content (title + description).
     *
     * iconPanel : JPanel
     * Small colored square indicator representing activity status/type.
     *
     * textPanel : JPanel
     * Vertical grid containing title and description labels.
     *
     * rightPanel : JPanel
     * Contains time and date labels aligned to the right side.
     *
     * titleLabel : JLabel
     * Displays activity title.
     *
     * descLabel : JLabel
     * Displays activity description.
     *
     * timeLabel : JLabel
     * Displays activity time.
     *
     * dateLabel : JLabel
     * Displays activity date.
     */

    /**
     * Creates a single activity row UI component.
     *
     * Builds a structured panel containing:
     * - Left section: icon + text (title, description)
     * - Right section: time and date
     *
     * @param item The ActivityItem containing data for the row.
     * @return A JPanel representing a formatted activity row.
     */
    private JPanel createActivityRow(ActivityItem item) {

        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 15));

        // ------------------------------------------------------------
        // LEFT SECTION (ICON + TEXT)
        // ------------------------------------------------------------
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);

        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(4, 8));
        iconPanel.setBackground(new Color(66, 133, 244));
        iconPanel.setOpaque(true);

        leftPanel.add(iconPanel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(item.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(33, 37, 41));

        JLabel descLabel = new JLabel(item.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(108, 117, 125));

        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        leftPanel.add(textPanel, BorderLayout.CENTER);

        // ------------------------------------------------------------
        // RIGHT SECTION (TIME + DATE)
        // ------------------------------------------------------------
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        rightPanel.setOpaque(false);

        JLabel timeLabel = new JLabel(item.getTime());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(108, 117, 125));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel dateLabel = new JLabel(item.getDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(108, 117, 125));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        rightPanel.add(timeLabel);
        rightPanel.add(dateLabel);

        // ------------------------------------------------------------
        // FINAL ASSEMBLY
        // ------------------------------------------------------------
        row.add(leftPanel, BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);

        return row;
    }
}