package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel containing the status update form.
 * Orchestrates the status dropdown, notes, and conditional sub-panels
 * (PendingValidation, InProgressDetail, ResolutionDetail).
 */
public class UpdateStatusPanel extends JPanel {

    private final JComboBox<String> cmbStatus;
    private final JTextArea txtProcessNotes;
    private final JLabel lblCurrentStatus;
    private final JLabel lblMeta;

    private final PendingValidationPanel pendingPanel;
    private final InProgressDetailPanel inProgressPanel;
    private final ResolutionDetailPanel resolutionPanel;

    public UpdateStatusPanel(String currentUserFullName) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(new Color(255, 255, 255, 252));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_UPDATE_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Update Complaint Status");
        title.setFont(UIConstants.FONT_BOLD_16);
        title.setForeground(config.UIConfig.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header);
        add(Box.createVerticalStrut(12));

        // Current status row
        JPanel currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        currentRow.setOpaque(false);
        currentRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
        JLabel lblCurrentPrefix = new JLabel("Current Status:");
        lblCurrentPrefix.setFont(UIConstants.FONT_PLAIN_12);
        lblCurrentStatus = new JLabel("—");
        lblCurrentStatus.setFont(UIConstants.FONT_BOLD_12);
        lblCurrentStatus.setForeground(UIConstants.C_IN_PROGRESS);
        currentRow.add(lblCurrentPrefix);
        currentRow.add(lblCurrentStatus);
        add(currentRow);
        add(Box.createVerticalStrut(12));

        // Form grid: Status + Notes
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        formGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        formGrid.setMaximumSize(new Dimension(Short.MAX_VALUE, 140));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JPanel statusWrap = new JPanel(new BorderLayout(0, 4));
        statusWrap.setOpaque(false);
        JLabel lblStatus = new JLabel("New Status *");
        lblStatus.setFont(UIConstants.FONT_BOLD_12);
        lblStatus.setForeground(UIConstants.C_TEXT_MUTED);
        cmbStatus = new JComboBox<>();
        cmbStatus.setFont(UIConstants.FONT_PLAIN_13);
        cmbStatus.setBackground(Color.WHITE);
        cmbStatus.setBorder(BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true));
        cmbStatus.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));
        statusWrap.add(lblStatus, BorderLayout.NORTH);
        statusWrap.add(cmbStatus, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formGrid.add(statusWrap, gbc);

        JPanel notesWrap = new JPanel(new BorderLayout(0, 4));
        notesWrap.setOpaque(false);
        JLabel lblNotes = new JLabel("Process / Notes");
        lblNotes.setFont(UIConstants.FONT_BOLD_12);
        lblNotes.setForeground(UIConstants.C_TEXT_MUTED);
        txtProcessNotes = new JTextArea(3, 20);
        txtProcessNotes.setLineWrap(true);
        txtProcessNotes.setWrapStyleWord(true);
        txtProcessNotes.setFont(UIConstants.FONT_PLAIN_13);
        txtProcessNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane notesScroll = FieldFactory.createNonScrollingScrollPane(txtProcessNotes);
        notesWrap.add(lblNotes, BorderLayout.NORTH);
        notesWrap.add(notesScroll, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        formGrid.add(notesWrap, gbc);

        add(formGrid);
        add(Box.createVerticalStrut(10));

        // Conditional sub-panels
        pendingPanel = new PendingValidationPanel();
        pendingPanel.setVisible(false);
        add(pendingPanel);

        inProgressPanel = new InProgressDetailPanel();
        inProgressPanel.setVisible(false);
        add(inProgressPanel);

        resolutionPanel = new ResolutionDetailPanel();
        resolutionPanel.setVisible(false);
        add(resolutionPanel);

        add(Box.createVerticalStrut(10));

        // Meta
        lblMeta = new JLabel("Updated by: " + currentUserFullName + "  •  " +
                new java.sql.Date(System.currentTimeMillis()));
        lblMeta.setFont(UIConstants.FONT_PLAIN_11);
        lblMeta.setForeground(UIConstants.C_TEXT_MUTED);
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblMeta);

        // Wire events
        cmbStatus.addActionListener(e -> onStatusChanged());
    }

    private void onStatusChanged() {
        String status = (String) cmbStatus.getSelectedItem();
        pendingPanel.setVisible(false);
        resolutionPanel.setVisible("Resolved".equals(status));
        inProgressPanel.setVisible("In Progress".equals(status));
        revalidate();
        repaint();
    }

    public void setCurrentStatus(String status) {
        lblCurrentStatus.setText(status != null ? status : "—");
    }

    public void populateStatusDropdown(String currentStatus) {
        cmbStatus.removeAllItems();
        String cur = currentStatus != null ? currentStatus : "Pending";

        switch (cur) {
            case "Pending" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Transferred");
                cmbStatus.addItem("Resolved");
            }
            case "In Progress" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Resolved");
                cmbStatus.addItem("Transferred");
            }
            case "Transferred" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Resolved");
            }
            case "Resolved" -> cmbStatus.addItem("Transferred");
            case "Rejected" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Transferred");
            }
            default -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Resolved");
                cmbStatus.addItem("Transferred");
            }
        }
    }

    public void reset(String currentUserFullName) {
        txtProcessNotes.setText("");
        pendingPanel.clearFields();
        inProgressPanel.setOfficer("");
        inProgressPanel.setAssignedDate("");
        resolutionPanel.setActionTaken("");
        resolutionPanel.setRecommendation("");
        resolutionPanel.setOIC("");
        resolutionPanel.setResolutionDate("");
        pendingPanel.setVisible(false);
        inProgressPanel.setVisible(false);
        resolutionPanel.setVisible(false);
        lblMeta.setText("Updated by: " + currentUserFullName + "  •  " +
                new java.sql.Date(System.currentTimeMillis()));
    }

    public void prefillForPending(String rawSubject, String rawType, String officer, String date) {
        pendingPanel.setPendingTitle(rawSubject != null ? rawSubject : "");
        pendingPanel.setPendingType(rawType);
        pendingPanel.setPendingOfficer(officer != null ? officer : "");
        inProgressPanel.setOfficer(officer != null ? officer : "");
        inProgressPanel.setAssignedDate(date != null ? date : "");
        resolutionPanel.setOIC(officer != null ? officer : "");
        resolutionPanel.setResolutionDate(date != null ? date : "");
    }

    public void showPendingPanel(boolean visible) {
        pendingPanel.setVisible(visible);
    }

    public String getSelectedStatus() {
        Object sel = cmbStatus.getSelectedItem();
        return sel != null ? sel.toString() : "";
    }

    public String getProcessNotes() {
        return txtProcessNotes.getText().trim();
    }

    public PendingValidationPanel getPendingPanel() {
        return pendingPanel;
    }

    public InProgressDetailPanel getInProgressPanel() {
        return inProgressPanel;
    }

    public ResolutionDetailPanel getResolutionPanel() {
        return resolutionPanel;
    }
}