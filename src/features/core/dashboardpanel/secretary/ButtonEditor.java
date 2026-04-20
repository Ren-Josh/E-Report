package features.core.dashboardpanel.secretary;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * ButtonEditor
 *
 * A custom TableCellEditor used to render and handle clickable button-like
 * components inside JTable cells. It replaces the default cell editor with
 * a custom JPanel-based button UI that supports mouse press/release feedback
 * and executes a callback action when clicked.
 */
public class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

    // ============================================================
    // INSTANCE VARIABLES (UI COMPONENTS)
    // ============================================================

    /** Root cell container used as the custom rendered button. */
    private final JPanel cell;

    /** Label displayed inside the button cell. */
    private final JLabel label;

    // ============================================================
    // INSTANCE VARIABLES (STYLING)
    // ============================================================

    /** Default background color of the button. */
    private final Color baseColor;

    /** Color used when the button is pressed. */
    private final Color pressColor;

    // ============================================================
    // INSTANCE VARIABLES (BEHAVIOR)
    // ============================================================

    /** Callback executed when the button is clicked, passing row index. */
    private final Consumer<Integer> onClick;

    /** Currently edited row index in the JTable. */
    private int currentRow;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs a ButtonEditor for JTable cells.
     *
     * Creates a custom clickable button component inside a table cell with
     * mouse press/release visual feedback and a callback execution on click.
     *
     * @param checkBox    Required by TableCellEditor but not used directly.
     * @param text        Default text displayed on the button.
     * @param buttonColor Base color of the button.
     * @param onClick     Callback function executed when button is clicked.
     */
    public ButtonEditor(JCheckBox checkBox, String text, Color buttonColor, Consumer<Integer> onClick) {
        this.onClick = onClick;
        this.baseColor = buttonColor;
        this.pressColor = buttonColor.darker();

        // ------------------------------------------------------------
        // LABEL CONFIGURATION
        // ------------------------------------------------------------
        label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // ------------------------------------------------------------
        // CELL PANEL (CUSTOM BUTTON RENDERING)
        // ------------------------------------------------------------
        cell = new JPanel(new GridBagLayout()) {

            /** Current fill color used during rendering. */
            private Color currentFill = baseColor;

            {
                setOpaque(false);

                addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        currentFill = pressColor;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        currentFill = baseColor;
                        repaint();

                        fireEditingStopped();
                        onClick.accept(currentRow);
                    }
                });
            }

            // ------------------------------------------------------------
            // METHOD-LEVEL VARIABLES (paintComponent)
            // ------------------------------------------------------------

            /*
             * g2 : Graphics2D
             * Graphics context used for custom rendering.
             *
             * padX : int
             * Horizontal padding inside button shape.
             *
             * padY : int
             * Vertical padding inside button shape.
             *
             * arc : int
             * Corner radius for rounded button shape.
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

                g2.setColor(currentFill);
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
        };

        cell.add(label);
    }

    // ============================================================
    // TABLE CELL EDITOR METHODS
    // ============================================================

    /**
     * Returns the component used for editing a table cell.
     *
     * Updates the button label and stores the current row index for callback
     * execution when clicked.
     *
     * @param table      JTable instance.
     * @param value      Cell value to display.
     * @param isSelected Whether the cell is selected.
     * @param row        Row index of the cell.
     * @param column     Column index of the cell.
     * @return Component used as the cell editor.
     */
    @Override
    public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        currentRow = row;
        label.setText(value == null ? "" : value.toString());
        return cell;
    }

    /**
     * Returns the current value of the cell editor.
     *
     * @return The text displayed in the button label.
     */
    @Override
    public Object getCellEditorValue() {
        return label.getText();
    }

    /**
     * Determines whether the cell is editable.
     *
     * @param e Event triggering edit check.
     * @return Always true to allow button interaction.
     */
    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }

    /**
     * Determines whether the cell should be selected during editing.
     *
     * @param e Event triggering selection check.
     * @return Always false to prevent table selection behavior.
     */
    @Override
    public boolean shouldSelectCell(EventObject e) {
        return false;
    }
}