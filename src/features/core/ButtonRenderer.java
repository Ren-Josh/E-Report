package features.core;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * ButtonRenderer
 *
 * A custom TableCellRenderer used to display button-like UI components
 * inside JTable cells. It visually renders a rounded pill-shaped button
 * with centered text, styled using a fixed color and font.
 *
 * This renderer is purely visual and does not handle interaction logic.
 */
public class ButtonRenderer extends JPanel implements TableCellRenderer {

    // ============================================================
    // INSTANCE VARIABLES (UI COMPONENTS)
    // ============================================================

    /** Label displayed inside the rendered button cell. */
    private final JLabel label;

    // ============================================================
    // INSTANCE VARIABLES (STYLING)
    // ============================================================

    /** Base color used to render the button background. */
    private final Color buttonColor;

    // ============================================================
    // INSTANCE VARIABLES (STATE)
    // ============================================================

    /** Reserved hover state flag (currently unused for rendering logic). */
    private boolean isHover = false;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs a ButtonRenderer for JTable cells.
     *
     * Initializes a centered, pill-shaped button-style renderer using a label
     * and a fixed background color.
     *
     * @param text        Default text displayed in the button.
     * @param buttonColor Color used for the button background.
     */
    public ButtonRenderer(String text, Color buttonColor) {
        this.buttonColor = buttonColor;

        setLayout(new GridBagLayout());
        setOpaque(false);

        label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        add(label);
    }

    // ============================================================
    // TABLE CELL RENDERER METHOD
    // ============================================================

    /**
     * Returns the component used to render a table cell.
     *
     * Updates the label text based on the table cell value.
     *
     * @param table      JTable instance.
     * @param value      Value to be displayed in the cell.
     * @param isSelected Whether the cell is selected.
     * @param hasFocus   Whether the cell has focus.
     * @param row        Row index.
     * @param column     Column index.
     * @return The JPanel component used for rendering.
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        label.setText(value == null ? "" : value.toString());
        return this;
    }

    // ============================================================
    // CUSTOM PAINTING
    // ============================================================

    // ------------------------------------------------------------
    // METHOD-LEVEL VARIABLES
    // ------------------------------------------------------------

    /*
     * g2 : Graphics2D
     * Graphics context used for rendering the rounded button shape.
     *
     * padX : int
     * Horizontal padding for rounded rectangle positioning.
     *
     * padY : int
     * Vertical padding for rounded rectangle positioning.
     *
     * arc : int
     * Corner radius used to create pill-shaped button effect.
     */

    /**
     * Paints the rounded button background behind the label.
     *
     * Uses anti-aliased rendering to create a smooth pill-shaped button
     * appearance with consistent padding and centered alignment.
     *
     * @param g Graphics context used for rendering.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int padX = 10;
        int padY = 5;
        int arc = getHeight() - padY * 2;

        g2.setColor(buttonColor);
        g2.fillRoundRect(
                padX,
                padY,
                getWidth() - padX * 2,
                getHeight() - padY * 2,
                arc,
                arc);

        g2.dispose();
        super.paintComponent(g);
    }
}