package features.layout.common;

import app.E_Report;
import config.UIConfig;
import features.components.UIButton;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.UserSession;
import services.controller.ComplaintStatusController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel for updating the status of a complaint.
 * Displays complaint details, allows status selection with validation,
 * captures process notes, shows history log, and saves atomically.
 */
public class ComplaintStatusUpdatePanel extends JPanel {

    private final E_Report app;
    private final ComplaintStatusController controller;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    // Complaint being edited
    private int currentCdId = -1;
    private String currentStatus;

    // UI Components
    private JLabel lblComplaintId;
    private JLabel lblSubject;
    private JLabel lblType;
    private JLabel lblCurrentStatus;
    private JLabel lblPurok;
    private JLabel lblDateSubmitted;
    private JComboBox<String> cmbNewStatus;
    private JTextArea txtProcessNotes;
    private JLabel lblUpdatedBy;
    private JButton btnSave;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    // Colors
    private static final Color BG_CARD = new Color(255, 255, 255, 230);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color STATUS_PENDING = new Color(255, 193, 7);
    private static final Color STATUS_IN_PROGRESS = new Color(66, 133, 244);
    private static final Color STATUS_RESOLVED = new Color(52, 168, 83);
    private static final Color STATUS_REJECTED = new Color(239, 68, 68);

    public ComplaintStatusUpdatePanel(E_Report app) {
        this.app = app;
        this.controller = new ComplaintStatusController();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Main content wrapper
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0);

        // === Complaint Info Card ===
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        contentPanel.add(createComplaintInfoCard(), gbc);

        // === Status Update Card ===
        gbc.gridy = 1;
        contentPanel.add(createStatusUpdateCard(), gbc);

        // === History Card ===
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        contentPanel.add(createHistoryCard(), gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ==================== COMPLAINT INFO CARD ====================

    private JPanel createComplaintInfoCard() {
        JPanel card = createCardPanel("Complaint Information");

        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 10));
        grid.setOpaque(false);

        lblComplaintId = createInfoLabel("—");
        lblSubject = createInfoLabel("—");
        lblType = createInfoLabel("—");
        lblCurrentStatus = createInfoLabel("—");
        lblPurok = createInfoLabel("—");
        lblDateSubmitted = createInfoLabel("—");

        grid.add(createInfoRow("Complaint ID:", lblComplaintId));
        grid.add(createInfoRow("Subject:", lblSubject));
        grid.add(createInfoRow("Type:", lblType));
        grid.add(createInfoRow("Current Status:", lblCurrentStatus));
        grid.add(createInfoRow("Purok:", lblPurok));
        grid.add(createInfoRow("Date Submitted:", lblDateSubmitted));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // ==================== STATUS UPDATE CARD ====================

    private JPanel createStatusUpdateCard() {
        JPanel card = createCardPanel("Update Status");

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // New Status
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        formPanel.add(createFormLabel("New Status:"), gbc);

        cmbNewStatus = new JComboBox<>();
        cmbNewStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbNewStatus.setPreferredSize(new Dimension(200, 35));
        cmbNewStatus.addActionListener(e -> onStatusSelectionChanged());
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        formPanel.add(cmbNewStatus, gbc);

        // Updated By (read-only)
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        formPanel.add(createFormLabel("Updated By:"), gbc);

        lblUpdatedBy = createInfoLabel(app.getCurrentUserFullName());
        lblUpdatedBy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        formPanel.add(lblUpdatedBy, gbc);

        // Process Notes
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(createFormLabel("Process / Notes:"), gbc);

        txtProcessNotes = new JTextArea(4, 30);
        txtProcessNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProcessNotes.setLineWrap(true);
        txtProcessNotes.setWrapStyleWord(true);
        txtProcessNotes.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        JScrollPane notesScroll = new JScrollPane(txtProcessNotes);
        notesScroll.setBorder(null);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(notesScroll, gbc);

        // Save Button
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;

        btnSave = new UIButton("Update Status", UIConfig.PRIMARY,
                new Dimension(160, 42), UIConfig.BTN_SMALL_FONT, 20,
                UIButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSaveClicked());
        formPanel.add(btnSave, gbc);

        card.add(formPanel, BorderLayout.CENTER);
        return card;
    }

    // ==================== HISTORY CARD ====================

    private JPanel createHistoryCard() {
        JPanel card = createCardPanel("Status History");

        String[] columns = { "Date & Time", "Status", "Process / Notes", "Updated By" };
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setRowHeight(28);
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0, 4));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(245, 245, 245));

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        tableScroll.setPreferredSize(new Dimension(0, 200));

        card.add(tableScroll, BorderLayout.CENTER);
        return card;
    }

    // ==================== HELPER METHODS ====================

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setOpaque(true);
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UIConfig.TEXT_PRIMARY);
        card.add(lblTitle, BorderLayout.NORTH);

        return card;
    }

    private JPanel createInfoRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UIConfig.TEXT_SECONDARY);
        row.add(lbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(UIConfig.TEXT_PRIMARY);
        row.add(valueLabel, BorderLayout.CENTER);

        return row;
    }

    private JLabel createInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UIConfig.TEXT_PRIMARY);
        return lbl;
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case ComplaintStatusController.STATUS_PENDING -> STATUS_PENDING;
            case ComplaintStatusController.STATUS_IN_PROGRESS -> STATUS_IN_PROGRESS;
            case ComplaintStatusController.STATUS_RESOLVED -> STATUS_RESOLVED;
            case ComplaintStatusController.STATUS_REJECTED -> STATUS_REJECTED;
            default -> UIConfig.TEXT_SECONDARY;
        };
    }

    // ==================== PUBLIC API ====================

    /**
     * Loads a complaint into the panel for status updating.
     * Call this before displaying the panel.
     */
    public void loadComplaint(ComplaintDetail complaint) {
        if (complaint == null) {
            clear();
            return;
        }

        this.currentCdId = complaint.getComplaintId();
        this.currentStatus = complaint.getCurrentStatus();

        lblComplaintId.setText(String.valueOf(currentCdId));
        lblSubject.setText(complaint.getSubject());
        lblType.setText(complaint.getType());
        lblCurrentStatus.setText(currentStatus);
        lblCurrentStatus.setForeground(getStatusColor(currentStatus));
        lblPurok.setText(complaint.getPurok());
        lblDateSubmitted.setText(
                complaint.getDateTime() != null
                        ? dateFormat.format(complaint.getDateTime())
                        : "—");

        refreshStatusCombo();
        refreshHistoryTable();
        txtProcessNotes.setText("");
        btnSave.setEnabled(true);
    }

    /**
     * Clears the panel state.
     */
    public void clear() {
        this.currentCdId = -1;
        this.currentStatus = null;

        lblComplaintId.setText("—");
        lblSubject.setText("—");
        lblType.setText("—");
        lblCurrentStatus.setText("—");
        lblCurrentStatus.setForeground(UIConfig.TEXT_PRIMARY);
        lblPurok.setText("—");
        lblDateSubmitted.setText("—");

        cmbNewStatus.removeAllItems();
        txtProcessNotes.setText("");
        historyTableModel.setRowCount(0);
        btnSave.setEnabled(false);
    }

    // ==================== EVENT HANDLERS ====================

    private void onStatusSelectionChanged() {
        String selected = (String) cmbNewStatus.getSelectedItem();
        if (selected == null)
            return;

        // Visual feedback on the button
        btnSave.setText("Update to " + selected);
        btnSave.setBackground(getStatusColor(selected));
    }

    private void onSaveClicked() {
        if (currentCdId < 0) {
            showError("No complaint loaded.");
            return;
        }

        String newStatus = (String) cmbNewStatus.getSelectedItem();
        if (newStatus == null || newStatus.isEmpty()) {
            showError("Please select a new status.");
            return;
        }

        String process = txtProcessNotes.getText().trim();
        if (process.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "No process notes provided. Continue anyway?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION)
                return;
        }

        UserSession session = app.getUserSession();
        if (session == null) {
            showError("No active session. Please log in.");
            return;
        }

        // Disable button during save
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return controller.updateComplaintStatus(currentCdId, newStatus, process, session);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccess("Status updated to " + newStatus);
                        currentStatus = newStatus;
                        lblCurrentStatus.setText(currentStatus);
                        lblCurrentStatus.setForeground(getStatusColor(currentStatus));
                        refreshStatusCombo();
                        refreshHistoryTable();
                        txtProcessNotes.setText("");
                    } else {
                        showError("Failed to update status. Please try again.");
                    }
                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    btnSave.setEnabled(true);
                    onStatusSelectionChanged();
                }
            }
        };

        worker.execute();
    }

    private void refreshStatusCombo() {
        cmbNewStatus.removeAllItems();
        List<String> validNext = ComplaintStatusController.getValidNextStatuses(currentStatus);
        for (String status : validNext) {
            cmbNewStatus.addItem(status);
        }
        cmbNewStatus.setEnabled(!validNext.isEmpty());
        if (!validNext.isEmpty()) {
            onStatusSelectionChanged();
        } else {
            btnSave.setText("No Valid Transitions");
            btnSave.setEnabled(false);
        }
    }

    private void refreshHistoryTable() {
        historyTableModel.setRowCount(0);

        if (currentCdId < 0)
            return;

        SwingWorker<List<ComplaintHistoryDetail>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ComplaintHistoryDetail> doInBackground() {
                return controller.getStatusHistory(currentCdId);
            }

            @Override
            protected void done() {
                try {
                    List<ComplaintHistoryDetail> history = get();
                    for (ComplaintHistoryDetail h : history) {
                        historyTableModel.addRow(new Object[] {
                                h.getDateTimeUpdated() != null
                                        ? dateFormat.format(h.getDateTimeUpdated())
                                        : "—",
                                h.getStatus(),
                                h.getProcess(),
                                h.getUpdatedBy()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    // ==================== DIALOG HELPERS ====================

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}