package features.viewing;

import config.UIConfig;
import config.database.DBConnection;
import daos.GetComplaintDao;
import features.ui.DashboardFormUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import services.controller.ComplaintServiceController;

public class FullComplaintDisplayView extends JPanel {

    private final Runnable backCallback;
    private final String currentRole;
    private final boolean canComment;
    private final boolean canUpdateStatus;
    private final JLabel subjectValue;
    private final JLabel statusValue;
    private final JLabel typeValue;
    private final JLabel dateValue;
    private final JLabel streetValue;
    private final JLabel purokValue;
    private final JLabel coordinatesValue;
    private final JTextArea detailsArea;
    private final JLabel imageLabel;
    private final JLabel imageMetaLabel;
    private ImageIcon fullScreenImageIcon;
    private final JComboBox<String> statusCombo;
    private final JTextArea staffCommentArea;
    private final JTextArea historyArea;
    private int currentComplaintId = -1;

    public FullComplaintDisplayView(String currentRole, ComplaintDetail complaint, Runnable backCallback) {
        this.backCallback = backCallback;
        this.currentRole = currentRole != null ? currentRole : "Resident";
        String normalizedRole = this.currentRole.toLowerCase();
        this.canComment = normalizedRole.contains("resident")
                || normalizedRole.contains("secretary");
        this.canUpdateStatus = normalizedRole.contains("secretary")
                || normalizedRole.contains("captain");

        setOpaque(false);
        setLayout(new BorderLayout(18, 18));

        JPanel background = new JPanel(new BorderLayout(18, 18));
        background.setOpaque(false);
        background.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        leftColumn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        subjectValue = createValueLabel();
        statusValue = createValueLabel();
        typeValue = createValueLabel();
        dateValue = createValueLabel();
        streetValue = createValueLabel();
        purokValue = createValueLabel();
        coordinatesValue = createValueLabel();

        leftColumn.add(createSectionHeader("Complaint Overview"));
        leftColumn.add(createInfoRow("Subject", subjectValue));
        leftColumn.add(createInfoRow("Status", statusValue));
        leftColumn.add(createInfoRow("Type", typeValue));

        leftColumn.add(Box.createRigidArea(new Dimension(0, 16)));
        leftColumn.add(createSectionHeader("Location & Timing"));
        leftColumn.add(createInfoRow("Date/Time", dateValue));
        leftColumn.add(createInfoRow("Street", streetValue));
        leftColumn.add(createInfoRow("Purok", purokValue));
        leftColumn.add(createInfoRow("Coordinates", coordinatesValue));

        leftColumn.add(Box.createRigidArea(new Dimension(0, 24)));
        leftColumn.add(createSectionHeader("Description"));
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(UIConfig.BODY);
        detailsArea.setBackground(UIConfig.BG_LIGHT);
        detailsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        detailsArea.setPreferredSize(new Dimension(0, 180));
        detailsArea.setMinimumSize(new Dimension(0, 180));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        detailsScroll.setPreferredSize(new Dimension(0, 180));
        leftColumn.add(detailsScroll);

        JPanel rightColumn = new JPanel(new GridBagLayout());
        rightColumn.setOpaque(false);
        rightColumn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JPanel imageCard = new JPanel(new BorderLayout());
        imageCard.setOpaque(true);
        imageCard.setBackground(new Color(248, 250, 253));
        imageCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        imageCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JLabel previewTitle = new JLabel("Evidence Photo");
        previewTitle.setFont(UIConfig.INPUT_TITLE);
        previewTitle.setForeground(UIConfig.TEXT_PRIMARY);
        imageCard.add(previewTitle, BorderLayout.NORTH);

        imageLabel = new JLabel("No image available", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(0, 280));
        imageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(UIConfig.BG_LIGHT);
        imageLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225), 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        imageCard.add(imageLabel, BorderLayout.CENTER);

        imageMetaLabel = new JLabel("", SwingConstants.CENTER);
        imageMetaLabel.setFont(UIConfig.CAPTION);
        imageMetaLabel.setForeground(UIConfig.TEXT_SECONDARY);
        imageMetaLabel.setBorder(BorderFactory.createEmptyBorder(12, 8, 0, 8));

        JButton fullImageButton = new JButton("View Full Image");
        fullImageButton.setFont(UIConfig.BTN_SECONDARY_FONT);
        fullImageButton.setBackground(new Color(40, 120, 240));
        fullImageButton.setForeground(Color.WHITE);
        fullImageButton.setFocusable(false);
        fullImageButton.addActionListener(e -> showFullScreenImage());
        JPanel imageFooter = new JPanel(new BorderLayout());
        imageFooter.setOpaque(false);
        imageFooter.add(imageMetaLabel, BorderLayout.CENTER);
        imageFooter.add(fullImageButton, BorderLayout.EAST);
        imageCard.add(imageFooter, BorderLayout.SOUTH);

        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.weightx = 1.0;
        rightGbc.weighty = 0.40;
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.anchor = GridBagConstraints.NORTH;
        rightGbc.insets = new Insets(0, 0, 16, 0);
        rightColumn.add(imageCard, rightGbc);

        statusCombo = new JComboBox<>(new String[] { "Pending", "In Progress", "Resolved", "On Hold", "Closed" });
        statusCombo.setFont(UIConfig.BODY);
        statusCombo.setEnabled(canUpdateStatus);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        staffCommentArea = new JTextArea(6, 1);
        staffCommentArea.setFont(UIConfig.BODY);
        staffCommentArea.setLineWrap(true);
        staffCommentArea.setWrapStyleWord(true);
        staffCommentArea.setEditable(canComment);
        staffCommentArea.setBackground(canComment ? UIConfig.BG_LIGHT : new Color(245, 245, 245));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setFont(UIConfig.CAPTION);
        historyArea.setBackground(UIConfig.BG_LIGHT);

        JPanel updateCard = new JPanel();
        updateCard.setLayout(new BoxLayout(updateCard, BoxLayout.Y_AXIS));
        updateCard.setOpaque(true);
        updateCard.setBackground(new Color(255, 255, 255, 235));
        updateCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 238, 244), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel progressTitle = new JLabel(canComment ? "Update / Comment" : "Report Progress");
        progressTitle.setFont(UIConfig.H3);
        progressTitle.setForeground(UIConfig.TEXT_PRIMARY);
        updateCard.add(progressTitle);
        updateCard.add(Box.createRigidArea(new Dimension(0, 12)));

        updateCard.add(createSectionHeader("Update Details"));

        JPanel statusRow = new JPanel(new BorderLayout(8, 8));
        statusRow.setOpaque(false);
        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(UIConfig.INPUT_TITLE);
        statusLabel.setForeground(UIConfig.TEXT_SECONDARY);
        statusRow.add(statusLabel, BorderLayout.WEST);
        statusRow.add(statusCombo, BorderLayout.CENTER);
        updateCard.add(statusRow);
        updateCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel commentLabel = new JLabel(canComment ? "Comment" : "Recent activity notes are internal.");
        commentLabel.setFont(UIConfig.INPUT_TITLE);
        commentLabel.setForeground(UIConfig.TEXT_SECONDARY);
        updateCard.add(commentLabel);
        updateCard.add(Box.createRigidArea(new Dimension(0, 6)));
        JScrollPane commentScroll = new JScrollPane(staffCommentArea);
        commentScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        commentScroll.setPreferredSize(new Dimension(0, 140));
        updateCard.add(commentScroll);

        if (canComment) {
            JButton saveButton = new JButton(canUpdateStatus ? "Save Update" : "Add Comment");
            saveButton.setFont(UIConfig.BTN_SECONDARY_FONT);
            saveButton.setBackground(UIConfig.PRIMARY);
            saveButton.setForeground(Color.WHITE);
            saveButton.setFocusable(false);
            saveButton.setAlignmentX(RIGHT_ALIGNMENT);
            saveButton.addActionListener(e -> saveStatusUpdate());
            updateCard.add(Box.createRigidArea(new Dimension(0, 12)));
            updateCard.add(saveButton);
        }

        updateCard.add(Box.createRigidArea(new Dimension(0, 18)));
        updateCard.add(createSectionHeader("Timeline"));
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        historyScroll.setPreferredSize(new Dimension(0, 220));
        updateCard.add(historyScroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        JButton backButton = new JButton("Back");
        backButton.setFont(UIConfig.BTN_SECONDARY_FONT);
        backButton.setBackground(UIConfig.SECONDARY);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusable(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        backButton.setPreferredSize(UIConfig.BTN_SECONDARY);
        backButton.addActionListener(e -> {
            if (backCallback != null) {
                backCallback.run();
            }
        });
        actions.add(backButton);

        rightGbc.gridy = 1;
        rightGbc.weighty = 0.50;
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.insets = new Insets(0, 0, 16, 0);
        rightColumn.add(updateCard, rightGbc);

        rightGbc.gridy = 2;
        rightGbc.weighty = 0;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.anchor = GridBagConstraints.SOUTHEAST;
        rightGbc.insets = new Insets(0, 0, 0, 0);
        rightColumn.add(actions, rightGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.70;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new java.awt.Insets(0, 0, 0, 12);
        content.add(leftColumn, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.30;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        content.add(rightColumn, gbc);

        card.add(content, BorderLayout.CENTER);
        background.add(card, BorderLayout.CENTER);
        add(background, BorderLayout.CENTER);

        DashboardFormUtils.applyPoppinsFontRecursively(this);
        loadComplaint(complaint);
    }

    private void showFullScreenImage() {
        if (fullScreenImageIcon == null) {
            JOptionPane.showMessageDialog(this,
                    "No image available for fullscreen viewing.",
                    "No Image",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Evidence Photo",
                Dialog.ModalityType.APPLICATION_MODAL);
        JLabel imageView = new JLabel(fullScreenImageIcon);
        imageView.setHorizontalAlignment(SwingConstants.CENTER);
        imageView.setVerticalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(imageView);
        scrollPane.setPreferredSize(new Dimension(950, 750));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createSectionHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        JLabel label = new JLabel(title);
        label.setFont(UIConfig.INPUT_TITLE);
        label.setForeground(UIConfig.TEXT_PRIMARY);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(220, 225, 232));

        header.add(label, BorderLayout.NORTH);
        header.add(separator, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createInfoRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConfig.INPUT_TITLE);
        lbl.setForeground(UIConfig.TEXT_SECONDARY);

        valueLabel.setFont(UIConfig.BODY);
        valueLabel.setForeground(UIConfig.TEXT_PRIMARY);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        row.add(lbl);
        row.add(Box.createRigidArea(new Dimension(0, 6)));
        row.add(valueLabel);
        row.add(Box.createRigidArea(new Dimension(0, 12)));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return row;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("");
        label.setFont(UIConfig.BODY);
        label.setForeground(UIConfig.TEXT_PRIMARY);
        return label;
    }

    private void loadHistory(int complaintId) {
        List<ComplaintHistoryDetail> history;
        try (Connection con = DBConnection.connect()) {
            history = new GetComplaintDao().getComplaintHistory(con, complaintId);
        } catch (SQLException e) {
            historyArea.setText("Unable to load history.");
            e.printStackTrace();
            return;
        }

        if (history == null || history.isEmpty()) {
            historyArea.setText(canUpdateStatus ? "No staff updates yet." : "No progress updates available.");
            return;
        }

        StringBuilder timeline = new StringBuilder();
        for (ComplaintHistoryDetail entry : history) {
            timeline.append("• ");
            timeline.append(formatTimestamp(entry.getDateTimeUpdated()));
            timeline.append(" — ");
            timeline.append(safeString(entry.getStatus()));
            timeline.append(" (").append(safeString(entry.getUpdatedBy())).append(")\n");
            timeline.append(safeString(entry.getProcess())).append("\n\n");
        }
        historyArea.setText(timeline.toString().trim());
    }

    private void saveStatusUpdate() {
        if (!canComment) {
            return;
        }

        String selectedStatus = statusCombo.getSelectedItem() != null ? statusCombo.getSelectedItem().toString() : "";
        String note = staffCommentArea.getText().trim();
        if (selectedStatus.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Please choose a status before saving.",
                    "Validation Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (note.isBlank()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "No comment was entered. Continue saving status update without a note?",
                    "Confirm Save",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ComplaintServiceController controller = new ComplaintServiceController();
        boolean saved = controller.updateComplaintStatus(this.currentComplaintId, selectedStatus, note,
                this.currentRole);
        if (saved) {
            JOptionPane.showMessageDialog(this,
                    "Status update successfully saved.",
                    "Update Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            statusValue.setText(selectedStatus);
            loadHistory(this.currentComplaintId);
            staffCommentArea.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unable to save the update. Please try again.",
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "Unknown time";
        }
        return ts.toString();
    }

    public void loadComplaint(ComplaintDetail cd) {
        if (cd == null) {
            JOptionPane.showMessageDialog(this,
                    "Complaint data is not available.",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.currentComplaintId = cd.getComplaintId();
        subjectValue.setText(safeString(cd.getSubject()));
        statusValue.setText(safeString(cd.getCurrentStatus()));
        typeValue.setText(safeString(cd.getType()));
        dateValue.setText(cd.getDateTime() != null ? cd.getDateTime().toString() : "N/A");
        streetValue.setText(safeString(cd.getStreet()));
        purokValue.setText(safeString(cd.getPurok()));
        coordinatesValue.setText(String.format("Lat %.6f, Long %.6f", cd.getLatitude(), cd.getLongitude()));
        detailsArea.setText(safeString(cd.getDetails()));
        statusCombo.setSelectedItem(safeString(cd.getCurrentStatus()));
        if (!canComment) {
            staffCommentArea.setText("");
        }
        loadHistory(cd.getComplaintId());

        byte[] photoBytes = cd.getPhotoAttachmentBytes();
        if (photoBytes != null && photoBytes.length > 0) {
            fullScreenImageIcon = new ImageIcon(photoBytes);
            Image image = fullScreenImageIcon.getImage().getScaledInstance(420, 420, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(image));
            imageLabel.setText("");
        } else {
            fullScreenImageIcon = null;
            imageLabel.setIcon(null);
            imageLabel.setText("No image available");
        }

        StringBuilder metaText = new StringBuilder();
        if (cd.getPhotoName() != null && !cd.getPhotoName().isBlank()) {
            metaText.append(cd.getPhotoName());
        }
        if (cd.getPhotoType() != null && !cd.getPhotoType().isBlank()) {
            if (metaText.length() > 0) {
                metaText.append(" • ");
            }
            metaText.append(cd.getPhotoType());
        }
        if (cd.getPhotoSize() != null) {
            if (metaText.length() > 0) {
                metaText.append(" • ");
            }
            metaText.append(cd.getPhotoSize()).append(" bytes");
        }
        imageMetaLabel.setText(metaText.length() > 0 ? metaText.toString() : "");
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }
}