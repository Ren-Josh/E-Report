package features.layout.common.viewreport;

import models.FollowUpRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel displaying follow-up request status inside the complaint detail view.
 * Shows a colored banner when a follow-up has been requested.
 */
public class FollowUpBadgePanel extends JPanel {

    private final JLabel lblStatus;
    private final JLabel lblDate;
    private final JLabel lblNotes;

    public FollowUpBadgePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(new Color(254, 243, 199)); // amber-50
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 158, 11), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setVisible(false);

        JLabel header = new JLabel("Follow-Up Requested");
        header.setFont(UIConstants.FONT_BOLD_13);
        header.setForeground(new Color(180, 83, 9));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(header);
        add(Box.createVerticalStrut(4));

        lblStatus = new JLabel("Status: Pending");
        lblStatus.setFont(UIConstants.FONT_PLAIN_12);
        lblStatus.setForeground(UIConstants.C_TEXT_MUTED);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblStatus);

        lblDate = new JLabel("Requested: —");
        lblDate.setFont(UIConstants.FONT_PLAIN_12);
        lblDate.setForeground(UIConstants.C_TEXT_MUTED);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblDate);

        lblNotes = new JLabel("");
        lblNotes.setFont(UIConstants.FONT_PLAIN_12);
        lblNotes.setForeground(UIConstants.C_TEXT_MUTED);
        lblNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblNotes);
    }

    public void showRequest(FollowUpRequest req) {
        if (req == null) {
            setVisible(false);
            return;
        }
        lblStatus.setText("Status: " + req.getStatus());
        lblDate.setText("Requested: " + (req.getRequestDate() != null ? req.getRequestDate().toString() : "—"));
        if (req.getNotes() != null && !req.getNotes().isBlank()) {
            lblNotes.setText("Notes: " + req.getNotes());
            lblNotes.setVisible(true);
        } else {
            lblNotes.setVisible(false);
        }

        // Color based on status
        switch (req.getStatus()) {
            case "Pending" -> {
                setBackground(new Color(254, 243, 199));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(245, 158, 11), 1, true),
                        new EmptyBorder(12, 16, 12, 16)));
            }
            case "Acknowledged" -> {
                setBackground(new Color(239, 246, 255));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(59, 130, 246), 1, true),
                        new EmptyBorder(12, 16, 12, 16)));
            }
            case "Resolved" -> {
                setBackground(new Color(236, 253, 245));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(16, 185, 129), 1, true),
                        new EmptyBorder(12, 16, 12, 16)));
            }
        }
        setVisible(true);
    }

    public void hideRequest() {
        setVisible(false);
    }
}