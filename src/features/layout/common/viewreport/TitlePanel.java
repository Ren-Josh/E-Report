package features.layout.common.viewreport;

import config.UIConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TitlePanel extends JPanel {

    private final JLabel lblStatusBadge;
    private final JLabel lblFollowUpBadge;
    private final JLabel lblTitle;
    private final JButton btnUpdate;
    private final JButton btnCancel;
    private final JButton btnReject;
    private final JButton btnSave;
    private final JButton btnFollowUp;

    private final JPanel rightPanel;

    public TitlePanel() {
        setLayout(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 16, 8, 16));

        setPreferredSize(new Dimension(0, 48));

        // ── Left: badges + title ──
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
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
        lblFollowUpBadge.setBackground(new Color(249, 115, 22));
        lblFollowUpBadge.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        lblFollowUpBadge.setVisible(false);

        lblTitle = new JLabel("Report #000 – Category");
        lblTitle.setFont(UIConstants.FONT_BOLD_22);
        lblTitle.setForeground(UIConfig.TEXT_PRIMARY);

        left.add(lblStatusBadge);
        left.add(lblFollowUpBadge);
        left.add(lblTitle);

        // ── Right: action buttons ──
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightPanel.setOpaque(false);

        btnCancel = ButtonFactory.createGhostButton("Cancel");
        btnCancel.setVisible(false);

        btnReject = ButtonFactory.createDangerButton("Reject");
        btnReject.setVisible(false);

        btnSave = ButtonFactory.createPrimaryButton("Save Changes", UIConstants.C_RESOLVED);
        btnSave.setVisible(false);

        btnUpdate = ButtonFactory.createPrimaryButton("Update Status", UIConfig.PRIMARY);

        btnFollowUp = ButtonFactory.createSecondaryButton("Request Follow Up");

        rightPanel.add(btnFollowUp);
        rightPanel.add(btnCancel);
        rightPanel.add(btnReject);
        rightPanel.add(btnSave);
        rightPanel.add(btnUpdate);

        add(left, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
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
        refresh();
    }

    public void setRejectVisible(boolean visible) {
        btnReject.setVisible(visible);
        refresh();
    }

    public void setUpdateVisible(boolean visible) {
        btnUpdate.setVisible(visible);
        refresh();
    }

    public void setFollowUpVisible(boolean visible) {
        btnFollowUp.setVisible(visible);
        refresh();
    }

    public void setFollowUpBadgeVisible(boolean visible) {
        lblFollowUpBadge.setVisible(visible);
        refresh();
    }

    /**
     * Revalidates the button bar, this panel, and every parent up to the top level.
     */
    private void refresh() {
        rightPanel.revalidate();
        rightPanel.repaint();
        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> {
            // FIXED: Guard against running layout on a component not yet showing
            if (!isShowing())
                return;
            Container c = getParent();
            while (c != null) {
                c.revalidate();
                c.repaint();
                c = c.getParent();
            }
        });
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