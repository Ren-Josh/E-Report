package features.core.dashboardpanel.secretary.panels;

import javax.swing.*;
import java.awt.*;
import features.components.GlassPanel;

/**
 * InfoPanel
 *
 * A reusable UI panel that displays a titled list of informational items
 * inside a glass-style container. It supports dynamic item addition with
 * optional text color customization and provides methods to clear content.
 *
 * The panel is composed of:
 * - A शीर्ष/heading label at the top
 * - A vertically stacked list of items in the center
 */
public class InfoPanel extends GlassPanel {

    // ============================================================
    // INSTANCE VARIABLES (UI COMPONENTS)
    // ============================================================

    /** Title text displayed at the top of the panel. */
    private String title;

    /** Container panel that holds all dynamically added item labels. */
    private JPanel itemsPanel;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs an InfoPanel with a specified title.
     *
     * Initializes the layout, sets padding, and builds the title header
     * and item container panel.
     *
     * @param title The title displayed at the top of the panel.
     */
    public InfoPanel(String title) {
        super(new BorderLayout());
        this.title = title;

        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initializeUI();
    }

    // ============================================================
    // INITIALIZATION METHODS
    // ============================================================

    /**
     * Initializes the UI components of the panel.
     *
     * Creates the title label and the vertical items container panel,
     * then adds them to the main layout.
     */
    private void initializeUI() {

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        add(titleLabel, BorderLayout.NORTH);

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);

        add(itemsPanel, BorderLayout.CENTER);
    }

    // ============================================================
    // ITEM MANAGEMENT METHODS
    // ============================================================

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES (addItem - default color)
    // ------------------------------------------------------------

    /*
     * itemLabel : JLabel
     * Label representing a single bullet item entry in the list.
     */

    /**
     * Adds a new item to the panel using default text color.
     *
     * The item is displayed as a bullet point inside the vertical list.
     *
     * @param itemText The text content of the item.
     */
    public void addItem(String itemText) {

        JLabel itemLabel = new JLabel("<html>• " + itemText + "</html>");
        itemLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        itemLabel.setForeground(new Color(60, 60, 60));
        itemLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        itemsPanel.add(itemLabel);
        itemsPanel.revalidate();
    }

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES (addItem - custom color)
    // ------------------------------------------------------------

    /*
     * itemLabel : JLabel
     * Label representing a single bullet item entry with custom color.
     */

    /**
     * Adds a new item to the panel using a custom text color.
     *
     * @param itemText  The text content of the item.
     * @param textColor The color used for the item text.
     */
    public void addItem(String itemText, Color textColor) {

        JLabel itemLabel = new JLabel("<html>• " + itemText + "</html>");
        itemLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        itemLabel.setForeground(textColor);
        itemLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        itemsPanel.add(itemLabel);
        itemsPanel.revalidate();
    }

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES (clearItems)
    // ------------------------------------------------------------

    /*
     * No local variables used.
     */

    /**
     * Removes all items from the panel and refreshes the UI.
     */
    public void clearItems() {
        itemsPanel.removeAll();
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }
}