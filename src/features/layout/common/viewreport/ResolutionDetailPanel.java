package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

public class ResolutionDetailPanel extends JPanel {
    private final JTextField txtActionTaken;
    private final JTextField txtRecommendation;
    private final JTextField txtOIC;
    private final JTextField txtResolutionDate;

    public ResolutionDetailPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.C_RESOLVED),
                new EmptyBorder(10, 0, 0, 0)));

        JLabel lblHeader = new JLabel("Resolution Actioned");
        lblHeader.setFont(UIConstants.FONT_BOLD_13);
        lblHeader.setForeground(UIConstants.C_RESOLVED);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblHeader);
        add(Box.createVerticalStrut(10));

        txtActionTaken = new JTextField();
        txtRecommendation = new JTextField();
        txtOIC = new JTextField();
        txtResolutionDate = new JTextField();

        JPanel actionWrap = FieldFactory.createLabeledField("Action Taken *", txtActionTaken);
        actionWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionWrap.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        add(actionWrap);
        add(Box.createVerticalStrut(6));

        JPanel recWrap = FieldFactory.createLabeledField("Recommendation", txtRecommendation);
        recWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        recWrap.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        add(recWrap);
        add(Box.createVerticalStrut(6));

        JPanel oicWrap = FieldFactory.createLabeledField("Officer in Charge", txtOIC);
        oicWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        oicWrap.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        add(oicWrap);
        add(Box.createVerticalStrut(6));

        JPanel dateWrap = FieldFactory.createLabeledField("Resolution Date (YYYY-MM-DD)", txtResolutionDate);
        dateWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateWrap.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        add(dateWrap);
    }

    public String getActionTaken() {
        return txtActionTaken.getText().trim();
    }

    public void setActionTaken(String v) {
        txtActionTaken.setText(v != null ? v : "");
    }

    public String getRecommendation() {
        return txtRecommendation.getText().trim();
    }

    public void setRecommendation(String v) {
        txtRecommendation.setText(v != null ? v : "");
    }

    public String getOIC() {
        return txtOIC.getText().trim();
    }

    public void setOIC(String v) {
        txtOIC.setText(v != null ? v : "");
    }

    public String getResolutionDate() {
        return txtResolutionDate.getText().trim();
    }

    public void setResolutionDate(String v) {
        txtResolutionDate.setText(v != null ? v : "");
    }
}