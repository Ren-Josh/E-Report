package features.layout.common.viewreport;

import config.UIConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Header panel displaying the complaint status badge, title,
 * action buttons (Update, Cancel, Reject, Save), and follow-up indicator.
 */
public class HeaderPanel extends JPanel {

    private final JLabel lblStatusBadge;
    private final JLabel lblFollowUpBadge;
    private final JLabel lblTitle;
    private final JButton btnUpdate;
    private final JButton btnCancel;
    private final JButton btnReject;
    private final JButton btnSave;
    private final JButton btnFollowUp;

    public HeaderPanel() {
        setLayout(new BorderLayout(16, 0));
        setOpaque(false);
        setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        // Left side: badge + follow-up badge + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        lblStatusBadge = new JLabel("PENDING", SwingConstants.CENTER);
        lblStatusBadge.setFont(UIConstants.FONT_BOLD_11);
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(UIConstants.C_PENDING);
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        lblFollowUpBadge = new JLabel("FOLLOW UP REQUESTED", SwingConstants.CENTER);
        lblFollowUpBadge.setFont(UIConstants.FONT_BOLD_11);
        lblFollowUpBadge.setForeground(Color.WHITE);
        lblFollowUpBadge.setOpaque(true);
        lblFollowUpBadge.setBackground(new Color(249, 115, 22)); // orange
        lblFollowUpBadge.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        lblFollowUpBadge.setVisible(false);

        lblTitle = new JLabel("Report #000 – Category");
        lblTitle.setFont(UIConstants.FONT_BOLD_22);
        lblTitle.setForeground(UIConfig.TEXT_PRIMARY);

        left.add(lblStatusBadge);
        left.add(lblFollowUpBadge);
        left.add(lblTitle);

        // Right side: action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        btnCancel = ButtonFactory.createGhostButton("Cancel");
        btnCancel.setVisible(false);

        btnReject = ButtonFactory.createDangerButton("Reject");
        btnReject.setVisible(false);

        btnSave = ButtonFactory.createPrimaryButton("Save Changes", UIConstants.C_RESOLVED);
        btnSave.setVisible(false);

        btnUpdate = ButtonFactory.createPrimaryButton("Update Status", UIConfig.PRIMARY);

        btnFollowUp = ButtonFactory.createSecondaryButton("Request Follow Up");

        right.add(btnCancel);
        right.add(btnReject);
        right.add(btnSave);
        right.add(btnFollowUp);
        right.add(btnUpdate);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    public void setStatus(String status) {
        lblStatusBadge.setText(status.toUpperCase());
        lblStatusBadge.setBackground(StatusColorUtil.getStatusColor(status));
    }

    public void setTitle(int complaintId, String type) {
        lblTitle.setText("Report #" + String.format("%03d", complaintId) + " – " + safe(type));
    }

    public void setUpdateMode(boolean editing) {
        btnUpdate.setVisible(!editing);
        btnCancel.setVisible(editing);
        btnSave.setVisible(editing);
    }

    public void setRejectVisible(boolean visible) {
        btnReject.setVisible(visible);
    }

    public void setUpdateVisible(boolean visible) {
        btnUpdate.setVisible(visible);
    }

    public void setFollowUpVisible(boolean visible) {
        btnFollowUp.setVisible(visible);
    }

    public void setFollowUpBadgeVisible(boolean visible) {
        lblFollowUpBadge.setVisible(visible);
    }

    public JButton getUpdateButton() {
        return btnUpdate;
    }

    public JButton getCancelButton() {
        return btnCancel;
    }

    public JButton getRejectButton() {
        return btnReject;
    }

    public JButton getSaveButton() {
        return btnSave;
    }

    public JButton getFollowUpButton() {
        return btnFollowUp;
    }

    private String safe(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }
}