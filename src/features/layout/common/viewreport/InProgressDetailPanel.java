package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel for In Progress status detail fields.
 */
public class InProgressDetailPanel extends JPanel {

    private final JTextField txtOfficer;
    private final JTextField txtAssignedDate;

    public InProgressDetailPanel() {
        setLayout(new GridLayout(3, 1, 8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.C_IN_PROGRESS),
                new EmptyBorder(10, 0, 0, 0)));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHeader = new JLabel("Complaint History Detail");
        lblHeader.setFont(UIConstants.FONT_BOLD_13);
        lblHeader.setForeground(UIConstants.C_IN_PROGRESS);
        add(lblHeader);

        txtOfficer = FieldFactory.createEditableField();
        add(FieldFactory.createLabeledFieldPanel("Officer / Personnel Assigned", txtOfficer));

        txtAssignedDate = FieldFactory.createEditableField();
        add(FieldFactory.createLabeledFieldPanel("Date Assigned (YYYY-MM-DD)", txtAssignedDate));
    }

    public String getOfficer() {
        return txtOfficer.getText().trim();
    }

    public void setOfficer(String officer) {
        txtOfficer.setText(officer != null ? officer : "");
    }

    public String getAssignedDate() {
        return txtAssignedDate.getText().trim();
    }

    public void setAssignedDate(String date) {
        txtAssignedDate.setText(date != null ? date : "");
    }
}