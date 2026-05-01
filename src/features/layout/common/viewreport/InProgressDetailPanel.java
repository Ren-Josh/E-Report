package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

public class InProgressDetailPanel extends JPanel {
    private final JTextField txtOfficer;
    private final JTextField txtAssignedDate;

    public InProgressDetailPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.C_IN_PROGRESS),
                new EmptyBorder(10, 0, 0, 0)));

        JLabel lblHeader = new JLabel("Complaint History Detail");
        lblHeader.setFont(UIConstants.FONT_BOLD_13);
        lblHeader.setForeground(UIConstants.C_IN_PROGRESS);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblHeader);
        add(Box.createVerticalStrut(10));

        txtOfficer = new JTextField();
        txtAssignedDate = new JTextField();

        JPanel officerWrap = FieldFactory.createLabeledField("Officer / Personnel Assigned", txtOfficer);
        officerWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        officerWrap.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        add(officerWrap);
        add(Box.createVerticalStrut(8));

        JPanel dateWrap = FieldFactory.createLabeledField("Date Assigned (YYYY-MM-DD)", txtAssignedDate);
        dateWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateWrap.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        add(dateWrap);
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