package features.layout.common.viewreport;

import config.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel for pending complaint validation fields.
 * Shown when transitioning from Pending to In Progress.
 */
public class PendingValidationPanel extends JPanel {

    private final JTextField txtPendingTitle;
    private final JComboBox<String> cmbPendingType;
    private final JTextField txtPendingOfficer;
    private final JTextArea txtPendingNotes;

    public PendingValidationPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.C_PENDING),
                new EmptyBorder(12, 0, 0, 0)));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHeader = new JLabel("Complaint Validation");
        lblHeader.setFont(UIConstants.FONT_BOLD_13);
        lblHeader.setForeground(UIConstants.C_PENDING);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblHeader);
        add(Box.createVerticalStrut(10));

        txtPendingTitle = FieldFactory.createEditableField();
        JPanel titleWrap = FieldFactory.createLabeledFieldPanel("Title (editable) *", txtPendingTitle);
        titleWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titleWrap);
        add(Box.createVerticalStrut(8));

        cmbPendingType = new JComboBox<>(AppConfig.COMPLAINT_TYPES);
        cmbPendingType.setFont(UIConstants.FONT_PLAIN_13);
        cmbPendingType.setBackground(UIConstants.C_BG_EDITABLE);
        cmbPendingType.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        cmbPendingType.setEditable(true);
        JPanel typeWrap = FieldFactory.createLabeledFieldPanel("Type / Category (editable) *", cmbPendingType);
        typeWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(typeWrap);
        add(Box.createVerticalStrut(8));

        txtPendingOfficer = FieldFactory.createEditableField();
        JPanel officerWrap = FieldFactory.createLabeledFieldPanel("Officer / Personnel Assigned", txtPendingOfficer);
        officerWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(officerWrap);
        add(Box.createVerticalStrut(8));

        txtPendingNotes = new JTextArea(3, 20);
        txtPendingNotes.setLineWrap(true);
        txtPendingNotes.setWrapStyleWord(true);
        txtPendingNotes.setFont(UIConstants.FONT_PLAIN_13);
        txtPendingNotes.setBackground(UIConstants.C_BG_EDITABLE);
        txtPendingNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane notesScroll = FieldFactory.createNonScrollingScrollPane(txtPendingNotes);
        JPanel notesWrap = FieldFactory.createLabeledFieldPanel("Validation Notes", notesScroll);
        notesWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(notesWrap);
    }

    public String getPendingTitle() {
        return txtPendingTitle.getText().trim();
    }

    public void setPendingTitle(String title) {
        txtPendingTitle.setText(title != null ? title : "");
    }

    public String getPendingType() {
        Object sel = cmbPendingType.getSelectedItem();
        return sel != null ? sel.toString().trim() : "";
    }

    public void setPendingType(String type) {
        if (type != null && !type.isBlank()) {
            cmbPendingType.setSelectedItem(type);
            if (cmbPendingType.getSelectedIndex() == -1) {
                cmbPendingType.insertItemAt(type, 0);
                cmbPendingType.setSelectedIndex(0);
            }
        } else {
            cmbPendingType.setSelectedIndex(-1);
        }
    }

    public String getPendingOfficer() {
        return txtPendingOfficer.getText().trim();
    }

    public void setPendingOfficer(String officer) {
        txtPendingOfficer.setText(officer != null ? officer : "");
    }

    public String getPendingNotes() {
        return txtPendingNotes.getText().trim();
    }

    public void setPendingNotes(String notes) {
        txtPendingNotes.setText(notes != null ? notes : "");
    }

    public void clearFields() {
        txtPendingTitle.setText("");
        cmbPendingType.setSelectedIndex(-1);
        txtPendingOfficer.setText("");
        txtPendingNotes.setText("");
    }
}