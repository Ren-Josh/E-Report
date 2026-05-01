package features.layout.common.viewreport;

import app.E_Report;
import config.UIConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UpdateStatusPanel extends JPanel {
    private final JComboBox<String> cmbStatus;
    private final JTextArea txtProcessNotes;
    private final JLabel lblCurrentStatus;
    private final JLabel lblMeta;

    private final PendingValidationPanel pendingPanel;
    private final InProgressDetailPanel inProgressPanel;
    private final ResolutionDetailPanel resolutionPanel;

    private Runnable onStatusChangedCallback;

    public UpdateStatusPanel(E_Report app) {
        setOpaque(true);
        setBackground(new Color(255, 255, 255, 252));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246, 90), 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Update Complaint Status");
        title.setFont(UIConstants.FONT_BOLD_16);
        title.setForeground(UIConfig.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header);
        add(Box.createVerticalStrut(12));

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
        cmbStatus.addActionListener(e -> {
            onInternalStatusChanged();
            if (onStatusChangedCallback != null)
                onStatusChangedCallback.run();
        });
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
        JScrollPane notesScroll = new JScrollPane(txtProcessNotes);
        notesScroll.setBorder(null);
        notesScroll.setOpaque(false);
        notesScroll.getViewport().setOpaque(false);
        notesScroll.setWheelScrollingEnabled(false);
        notesWrap.add(lblNotes, BorderLayout.NORTH);
        notesWrap.add(notesScroll, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        formGrid.add(notesWrap, gbc);

        add(formGrid);
        add(Box.createVerticalStrut(10));

        pendingPanel = new PendingValidationPanel();
        pendingPanel.setVisible(false);
        pendingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(pendingPanel);

        inProgressPanel = new InProgressDetailPanel();
        inProgressPanel.setVisible(false);
        inProgressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(inProgressPanel);

        resolutionPanel = new ResolutionDetailPanel();
        resolutionPanel.setVisible(false);
        resolutionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(resolutionPanel);

        add(Box.createVerticalStrut(10));

        lblMeta = new JLabel();
        lblMeta.setFont(UIConstants.FONT_PLAIN_11);
        lblMeta.setForeground(UIConstants.C_TEXT_MUTED);
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateMeta(app);
        add(lblMeta);
    }

    private void updateMeta(E_Report app) {
        lblMeta.setText("Updated by: " + app.getCurrentUserFullName() + "  •  " +
                new java.sql.Date(System.currentTimeMillis()));
    }

    public void setCurrentStatus(String status) {
        lblCurrentStatus.setText(status != null ? status : "—");
    }

    public void populateStatusDropdown(String currentStatus) {
        cmbStatus.removeAllItems();
        String current = currentStatus != null ? currentStatus : "Pending";
        switch (current) {
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

    public void reset(E_Report app, String currentStatus) {
        txtProcessNotes.setText("");
        resolutionPanel.setOIC(app.getCurrentUserFullName());
        inProgressPanel.setOfficer(app.getCurrentUserFullName());
        pendingPanel.setOfficer(app.getCurrentUserFullName());
        resolutionPanel.setResolutionDate(new java.sql.Date(System.currentTimeMillis()).toString());
        inProgressPanel.setAssignedDate(new java.sql.Date(System.currentTimeMillis()).toString());

        pendingPanel.setVisible(false);
        inProgressPanel.setVisible(false);
        resolutionPanel.setVisible(false);

        populateStatusDropdown(currentStatus);
    }

    public void prefillPending(String rawSubject, String rawType) {
        pendingPanel.setTitle(rawSubject);
        pendingPanel.setType(rawType);
    }

    private void onInternalStatusChanged() {
        String status = (String) cmbStatus.getSelectedItem();
        boolean isResolved = "Resolved".equals(status);
        boolean isInProgress = "In Progress".equals(status);

        pendingPanel.setVisible(false);
        resolutionPanel.setVisible(isResolved);
        inProgressPanel.setVisible(isInProgress);
    }

    public void showPendingPanel(boolean visible) {
        pendingPanel.setVisible(visible);
    }

    public void setOnStatusChangedCallback(Runnable callback) {
        this.onStatusChangedCallback = callback;
    }

    public String getSelectedStatus() {
        Object sel = cmbStatus.getSelectedItem();
        return sel != null ? sel.toString() : null;
    }

    public String getProcessNotes() {
        return txtProcessNotes.getText().trim();
    }

    public void setProcessNotes(String notes) {
        txtProcessNotes.setText(notes != null ? notes : "");
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