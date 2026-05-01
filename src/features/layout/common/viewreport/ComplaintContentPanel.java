package features.layout.common.viewreport;

import app.E_Report;
import config.database.DBConnection;
import daos.GetComplaintDao;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.FollowUpRequest;
import services.controller.ComplaintStatusController;
import services.controller.FollowUpRequestController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class ComplaintContentPanel extends JPanel {
    private final E_Report app;
    private final boolean canUpdateStatus;
    private final ComplaintStatusController statusController;

    private int currentCdId = -1;
    private ComplaintDetail currentComplaint;
    private String returnRoute = "dashboard";

    private final HeaderPanel headerPanel;
    private final StatusTimelinePanel timelinePanel;
    private final UpdateStatusPanel updatePanel;
    private final ComplaintDetailPanel detailPanel;
    private final ActionHistoryPanel actionHistoryPanel;
    private final FollowUpBadgePanel followUpBadgePanel;
    private final FollowUpRequestController followUpController;

    public ComplaintContentPanel(E_Report app) {
        this.app = app;
        this.followUpController = new FollowUpRequestController();
        this.currentComplaint = app.getCurrentComplaint();
        String role = app.getUserSession() != null ? app.getUserSession().getRole() : "";
        this.canUpdateStatus = role.toLowerCase().contains("secretary") || role.toLowerCase().contains("captain");
        this.statusController = new ComplaintStatusController();

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(0, 4, 12, 4));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 1.0;

        // Back button
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        JButton btnBackTop = ButtonFactory.createGhostButton("← Back");
        btnBackTop.addActionListener(e -> app.navigate(returnRoute));
        backRow.add(btnBackTop);
        gbc.gridy = 0;
        gbc.weighty = 0;
        mainContent.add(backRow, gbc);

        // Header
        headerPanel = new HeaderPanel();
        headerPanel.getFollowUpButton().addActionListener(e -> onRequestFollowUp());
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 0, 0);
        mainContent.add(headerPanel, gbc);

        // Timeline
        timelinePanel = new StatusTimelinePanel();
        gbc.gridy = 2;
        gbc.insets = new Insets(12, 0, 0, 0);
        mainContent.add(timelinePanel, gbc);

        // Update panel
        updatePanel = new UpdateStatusPanel(app);
        updatePanel.setVisible(false);
        updatePanel.setOnStatusChangedCallback(() -> {
            mainContent.revalidate();
            mainContent.repaint();
        });
        gbc.gridy = 3;
        mainContent.add(updatePanel, gbc);

        // Follow-up badge
        followUpBadgePanel = new FollowUpBadgePanel();
        gbc.gridy = 4;
        gbc.insets = new Insets(12, 0, 0, 0);
        mainContent.add(followUpBadgePanel, gbc);

        // Detail card
        detailPanel = new ComplaintDetailPanel();
        gbc.gridy = 5;
        mainContent.add(detailPanel, gbc);

        // Action history
        actionHistoryPanel = new ActionHistoryPanel();
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        mainContent.add(actionHistoryPanel, gbc);

        add(mainContent, BorderLayout.CENTER);

        // Wire header buttons
        headerPanel.getUpdateButton().addActionListener(e -> toggleUpdatePanel());
        headerPanel.getCancelButton().addActionListener(e -> hideUpdatePanel());
        headerPanel.getSaveButton().addActionListener(e -> saveUpdate());
        headerPanel.getRejectButton().addActionListener(e -> rejectComplaint());

        if (currentComplaint != null) {
            loadComplaint(currentComplaint);
        }
    }

    @Override
    public Dimension getMaximumSize() {
        Container parent = getParent();
        if (parent instanceof JViewport viewport) {
            Dimension viewportSize = viewport.getSize();
            return new Dimension(viewportSize.width, Integer.MAX_VALUE);
        }
        return super.getMaximumSize();
    }

    @Override
    public Dimension getPreferredSize() {
        Container parent = getParent();
        if (parent instanceof JViewport viewport) {
            Dimension viewportSize = viewport.getSize();
            Dimension pref = super.getPreferredSize();
            return new Dimension(Math.min(pref.width, viewportSize.width), pref.height);
        }
        return super.getPreferredSize();
    }

    public void setReturnRoute(String route) {
        this.returnRoute = (route != null && !route.isBlank()) ? route : "dashboard";
    }

    private void toggleUpdatePanel() {
        if (currentComplaint != null) {
            String cur = currentComplaint.getCurrentStatus();
            if ("Rejected".equalsIgnoreCase(cur) || "Resolved".equalsIgnoreCase(cur)) {
                JOptionPane.showMessageDialog(this,
                        "This complaint cannot be updated because it is already " + cur + ".",
                        "Update Not Allowed", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        boolean visible = !updatePanel.isVisible();
        updatePanel.setVisible(visible);
        headerPanel.setUpdateMode(visible);

        if (visible) {
            updatePanel.reset(app, currentComplaint != null ? currentComplaint.getCurrentStatus() : "Pending");

            if (currentComplaint != null && "Pending".equals(currentComplaint.getCurrentStatus())) {
                updatePanel.showPendingPanel(true);
                headerPanel.setRejectVisible(true);
                updatePanel.prefillPending(currentComplaint.getSubject(), currentComplaint.getType());
            } else {
                headerPanel.setRejectVisible(false);
            }

            updatePanel.setCurrentStatus(currentComplaint != null ? currentComplaint.getCurrentStatus() : "—");
        } else {
            headerPanel.setRejectVisible(false);
        }

        revalidate();
        repaint();

        if (visible) {
            SwingUtilities.invokeLater(() -> {
                Container parent = getParent();
                if (parent instanceof JViewport vp) {
                    JScrollPane scroll = (JScrollPane) vp.getParent();
                    if (scroll != null)
                        scroll.getVerticalScrollBar().setValue(0);
                }
            });
        }
    }

    private void hideUpdatePanel() {
        updatePanel.setVisible(false);
        headerPanel.setUpdateMode(false);
        headerPanel.setRejectVisible(false);
        revalidate();
        repaint();
    }

    private void rejectComplaint() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reject this complaint?\nThis action cannot be undone.",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        String note = updatePanel.getPendingPanel().getNotes();
        if (note.isEmpty())
            note = "Complaint rejected during validation";

        boolean saved = statusController.updateComplaintStatus(
                currentCdId, "Rejected", note, app.getUserSession());

        if (!saved) {
            JOptionPane.showMessageDialog(app, "Failed to reject complaint.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(app, "Complaint rejected successfully.", "Rejected",
                JOptionPane.INFORMATION_MESSAGE);
        currentComplaint.setCurrentStatus("Rejected");
        loadComplaint(currentComplaint);
        hideUpdatePanel();
    }

    private void saveUpdate() {
        String newStatus = updatePanel.getSelectedStatus();
        String note = updatePanel.getProcessNotes();

        if (newStatus == null || newStatus.isBlank()) {
            JOptionPane.showMessageDialog(app, "Please select a status.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pending → In Progress
        if ("In Progress".equals(newStatus) && "Pending".equals(currentComplaint.getCurrentStatus())) {
            String title = updatePanel.getPendingPanel().getTitle();
            String type = updatePanel.getPendingPanel().getType();

            if (title.isBlank() || "—".equals(title)) {
                JOptionPane.showMessageDialog(app, "Title is required.", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (type.isBlank() || "—".equals(type)) {
                JOptionPane.showMessageDialog(app, "Type / Category is required.", "Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String officer = updatePanel.getPendingPanel().getOfficer();
            if (officer.isBlank()) {
                JOptionPane.showMessageDialog(app, "Officer / Personnel Assigned is required.", "Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            StringBuilder sb = new StringBuilder();
            if (!note.isEmpty())
                sb.append(note).append("\n");
            sb.append("[Validated by: ").append(app.getCurrentUserFullName()).append("]");
            sb.append(" [Title: ").append(title).append("]");
            sb.append(" [Type: ").append(type).append("]");
            sb.append(" [Assigned to: ").append(officer).append("]");
            String pendingNotes = updatePanel.getPendingPanel().getNotes();
            if (!pendingNotes.isEmpty()) {
                sb.append(" [Notes: ").append(pendingNotes).append("]");
            }
            note = sb.toString();

            currentComplaint.setSubject(title);
            currentComplaint.setType(type);

            if (!updateComplaintDetail(currentCdId, title, type)) {
                JOptionPane.showMessageDialog(app, "Failed to save complaint details.", "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        // Normal In Progress
        else if ("In Progress".equals(newStatus)) {
            String officer = updatePanel.getInProgressPanel().getOfficer();
            if (officer.isEmpty()) {
                JOptionPane.showMessageDialog(app, "Officer / Personnel Assigned is required.", "Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String date = updatePanel.getInProgressPanel().getAssignedDate();
            note += "\n[Assigned to: " + officer + (date.isEmpty() ? "" : " | Date: " + date) + "]";
        }

        if ("Resolved".equals(newStatus)) {
            var action = buildComplaintAction();
            if (action == null)
                return;
        }

        boolean saved = statusController.updateComplaintStatus(
                currentCdId, newStatus, note, app.getUserSession());

        if (!saved) {
            JOptionPane.showMessageDialog(app, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(app, "Status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        currentComplaint.setCurrentStatus(newStatus);
        loadComplaint(currentComplaint);
        hideUpdatePanel();
    }

    private models.ComplaintAction buildComplaintAction() {
        String actionTaken = updatePanel.getResolutionPanel().getActionTaken();
        if (actionTaken.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Action Taken is required.", "Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        var action = new models.ComplaintAction();
        action.setCD_ID(String.valueOf(currentCdId));
        action.setActionTaken(actionTaken);
        action.setRecommendation(updatePanel.getResolutionPanel().getRecommendation());
        action.setOIC(updatePanel.getResolutionPanel().getOIC());

        try {
            String rd = updatePanel.getResolutionPanel().getResolutionDate();
            if (!rd.isEmpty())
                action.setResolutionDateTime(Timestamp.valueOf(rd + " 00:00:00"));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD.", "Invalid Date",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return action;
    }

    private boolean updateComplaintDetail(int cdId, String subject, String type) {
        String sql = "UPDATE Complaint_Detail SET subject = ?, type = ? WHERE CD_ID = ?";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setString(2, type);
            ps.setInt(3, cdId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadComplaint(ComplaintDetail cd) {
        if (cd == null)
            return;

        hideUpdatePanel();

        this.currentComplaint = cd;
        this.currentCdId = cd.getComplaintId();

        String status = safe(cd.getCurrentStatus());

        headerPanel.setStatus(status);
        headerPanel.setTitle(cd.getComplaintId(), cd.getType());
        refreshFollowUpStatus();

        timelinePanel.updateTimeline(status);
        detailPanel.loadComplaint(cd);

        boolean isFinalStatus = "Rejected".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status);
        headerPanel.setUpdateVisible(canUpdateStatus && !isFinalStatus);

        loadHistory(cd.getComplaintId());
    }

    private void loadHistory(int complaintId) {
        try (Connection con = DBConnection.connect()) {
            List<ComplaintHistoryDetail> history = new GetComplaintDao().getComplaintHistory(con, complaintId);
            actionHistoryPanel.loadHistory(history);

            Timestamp mostRecent = actionHistoryPanel.getMostRecentTimestamp(history);
            detailPanel.setLastUpdate(mostRecent != null ? mostRecent.toString() : "—");

        } catch (SQLException e) {
            e.printStackTrace();
            detailPanel.setLastUpdate("—");
        }
    }

    private String safe(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }

    private void onRequestFollowUp() {
        if (currentComplaint == null)
            return;

        Window owner = SwingUtilities.getWindowAncestor(this);
        FollowUpDialog dialog = new FollowUpDialog(owner, currentComplaint.getComplaintId());

        dialog.setVisible(true); // blocks because it's modal

        if (dialog.isSubmitted()) {
            int uiId = app.getUserSession() != null ? app.getUserSession().getUserId() : -1;
            boolean ok = followUpController.requestFollowUp(
                    currentComplaint.getComplaintId(), uiId, dialog.getNotes());

            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Follow-up request submitted successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshFollowUpStatus();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit follow-up request.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshFollowUpStatus() {
        if (currentComplaint == null)
            return;

        FollowUpRequest req = followUpController.getLatestFollowUp(currentComplaint.getComplaintId());
        boolean hasActive = req != null && !"Resolved".equalsIgnoreCase(req.getStatus());

        headerPanel.setFollowUpBadgeVisible(hasActive);
        headerPanel.setFollowUpVisible(!hasActive && !canUpdateStatus);
        followUpBadgePanel.showRequest(req);
    }
}