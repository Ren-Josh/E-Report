package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Factory for creating form fields and labeled field rows.
 */
public final class FieldFactory {
    private FieldFactory() {
    }

    public static JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setFont(UIConstants.FONT_PLAIN_13);
        field.setBackground(UIConstants.C_BG_FIELD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    public static JTextField createEditableField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_PLAIN_13);
        field.setBackground(UIConstants.C_BG_EDITABLE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    public static JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_BOLD_12);
        lbl.setForeground(UIConstants.C_TEXT_MUTED);
        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    public static JPanel createLabeledFieldPanel(String labelText, JComponent field) {
        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_BOLD_12);
        lbl.setForeground(UIConstants.C_TEXT_MUTED);
        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    public static JScrollPane createNonScrollingScrollPane(JTextArea textArea) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setWheelScrollingEnabled(false);
        return scroll;
    }
}