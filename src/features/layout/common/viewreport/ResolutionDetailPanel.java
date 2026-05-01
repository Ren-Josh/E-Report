package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel for Resolution status fields.
 */
public class ResolutionDetailPanel extends JPanel {

    private final JTextField txtActionTaken;
    private final JTextField txtRecommendation;
    private final JTextField txtOIC;
    private final JTextField txtResolutionDate;

    public ResolutionDetailPanel() {
        setLayout(new GridLayout(5, 1, 6, 6));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.C_RESOLUTION_BORDER),
                new EmptyBorder(10, 0, 0, 0)));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHeader = new JLabel("Resolution Actioned");
        lblHeader.setFont(UIConstants.FONT_BOLD_13);
        lblHeader.setForeground(UIConstants.C_RESOLVED);
        add(lblHeader);

        txtActionTaken = FieldFactory.createEditableField();
        add(FieldFactory.createLabeledFieldPanel("Action Taken *", txtActionTaken));

        txtRecommendation = FieldFactory.createEditableField();
        add(FieldFactory.createLabeledFieldPanel("Recommendation", txtRecommendation));

        txtOIC = FieldFactory.createEditableField();
        add(FieldFactory.createLabeledFieldPanel("Officer in Charge", txtOIC));

        txtResolutionDate = FieldFactory.createEditableField();
        add(FieldFactory.createLabeledFieldPanel("Resolution Date (YYYY-MM-DD)", txtResolutionDate));
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