package features.layout.common.viewreport;

import app.E_Report;
import config.database.DBConnection;
import daos.GetComplaintDao;
import models.ComplaintAction;
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
import java.sql.ResultSet;
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

    private final TitlePanel titlePanel;
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
        titlePanel = new TitlePanel();
        titlePanel.getFollowUpButton().addActionListener(e -> onRequestFollowUp());
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 0, 0);
        mainContent.add(titlePanel, gbc);

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
        gbc.gridy = 4;
        mainContent.add(updatePanel, gbc);

        // Follow-up badge
        followUpBadgePanel = new FollowUpBadgePanel();
        gbc.gridy = 5;
        gbc.insets = new Insets(12, 0, 0, 0);
        mainContent.add(followUpBadgePanel, gbc);

        // Action history
        actionHistoryPanel = new ActionHistoryPanel();
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        mainContent.add(actionHistoryPanel, gbc);

        // Detail card
        detailPanel = new ComplaintDetailPanel();
        gbc.gridy = 7;
        gbc.weighty = 0;
        mainContent.add(detailPanel, gbc);

        add(mainContent, BorderLayout.CENTER);

        // Wire header buttons
        titlePanel.getUpdateButton().addActionListener(e -> toggleUpdatePanel());
        titlePanel.getCancelButton().addActionListener(e -> hideUpdatePanel());
        titlePanel.getSaveButton().addActionListener(e -> saveUpdate());
        titlePanel.getRejectButton().addActionListener(e -> rejectComplaint());

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
        titlePanel.setUpdateMode(visible);

        if (visible) {
            updatePanel.reset(app, currentComplaint != null ? currentComplaint.getCurrentStatus() : "Pending");

            if (currentComplaint != null && "Pending".equals(currentComplaint.getCurrentStatus())) {
                updatePanel.showPendingPanel(true);
                titlePanel.setRejectVisible(true);
                updatePanel.prefillPending(currentComplaint.getSubject(), currentComplaint.getType());
            } else {
                titlePanel.setRejectVisible(false);
            }

            updatePanel.setCurrentStatus(currentComplaint != null ? currentComplaint.getCurrentStatus() : "—");
        } else {
            titlePanel.setRejectVisible(false);
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
        titlePanel.setUpdateMode(false);
        titlePanel.setRejectVisible(false);
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

        // Pending → In Progress (validation required)
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
        // In Progress → In Progress (same status, just adding history/note)
        else if ("In Progress".equals(newStatus) && "In Progress".equals(currentComplaint.getCurrentStatus())) {
            String officer = updatePanel.getInProgressPanel().getOfficer();
            if (officer.isEmpty()) {
                JOptionPane.showMessageDialog(app, "Officer / Personnel Assigned is required.", "Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String date = updatePanel.getInProgressPanel().getAssignedDate();

            StringBuilder sb = new StringBuilder();
            if (!note.isEmpty()) {
                sb.append(note);
            }
            sb.append("[Assigned to: ").append(officer);
            if (!date.isEmpty()) {
                sb.append(" | Date: ").append(date);
            }
            sb.append("]");
            note = sb.toString();
        }
        // ── Resolved ──
        else if ("Resolved".equals(newStatus)) {
            var action = buildComplaintAction();
            if (action == null)
                return;
            if (!saveComplaintAction(action)) {
                JOptionPane.showMessageDialog(app, "Failed to save resolution details.", "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
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

    private boolean saveComplaintAction(models.ComplaintAction action) {
        String sql = "INSERT INTO Complaint_Action (CD_ID, action_taken, recommendation, oic, resolution_date_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(action.getCD_ID()));
            ps.setString(2, action.getActionTaken());
            ps.setString(3, action.getRecommendation());
            ps.setString(4, action.getOIC());
            ps.setTimestamp(5, action.getResolutionDateTime());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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

    private models.ComplaintAction fetchResolvedAction(int cdId) {
        String sql = "SELECT action_taken, recommendation, oic, resolution_date_time " +
                "FROM Complaint_Action WHERE CD_ID = ? " +
                "ORDER BY resolution_date_time DESC LIMIT 1";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cdId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                var action = new models.ComplaintAction();
                action.setCD_ID(String.valueOf(cdId));
                action.setActionTaken(rs.getString("action_taken"));
                action.setRecommendation(rs.getString("recommendation"));
                action.setOIC(rs.getString("oic"));
                action.setResolutionDateTime(rs.getTimestamp("resolution_date_time"));
                return action;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadComplaint(ComplaintDetail cd) {
        if (cd == null)
            return;

        hideUpdatePanel();

        this.currentComplaint = cd;
        this.currentCdId = cd.getComplaintId();

        String status = safe(cd.getCurrentStatus());

        titlePanel.setStatus(status);
        titlePanel.setTitle(cd.getComplaintId(), cd.getType());
        refreshFollowUpStatus();

        timelinePanel.updateTimeline(status);
        detailPanel.loadComplaint(cd);

        boolean isFinalStatus = "Rejected".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status);
        titlePanel.setUpdateVisible(canUpdateStatus && !isFinalStatus);

        loadHistory(cd.getComplaintId());
    }

    private void loadHistory(int complaintId) {
        try (Connection con = DBConnection.connect()) {
            List<ComplaintHistoryDetail> history = new GetComplaintDao().getComplaintHistory(con, complaintId);
            ComplaintAction action = fetchResolvedAction(complaintId);
            actionHistoryPanel.loadHistory(history, action);

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

        FollowUpDialog dialog = new FollowUpDialog(app, currentComplaint.getComplaintId());

        dialog.setVisible(true);

        if (dialog.isSubmitted()) {
            int uiId = app.getUserSession() != null ? app.getUserSession().getUserId() : -1;
            boolean ok = followUpController.requestFollowUp(
                    currentComplaint.getComplaintId(), uiId, dialog.getNotes());

            if (ok) {
                JOptionPane.showMessageDialog(app,
                        "Follow-up request submitted successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshFollowUpStatus();
            } else {
                JOptionPane.showMessageDialog(app,
                        "Failed to submit follow-up request.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Determines follow-up visibility.
     * Badge + button are FORCE-HIDDEN when the complaint is Resolved, Rejected,
     * or Transferred because no further follow-up is possible.
     */
    private void refreshFollowUpStatus() {
        if (currentComplaint == null)
            return;

        String status = safe(currentComplaint.getCurrentStatus());

        // ── TERMINAL STATUSES: hide everything immediately ──
        if ("Resolved".equalsIgnoreCase(status)
                || "Rejected".equalsIgnoreCase(status)
                || "Transferred".equalsIgnoreCase(status)) {
            titlePanel.setFollowUpBadgeVisible(false);
            titlePanel.setFollowUpVisible(false);
            followUpBadgePanel.hideRequest();
            return;
        }

        // For Pending / In Progress, follow-ups are allowed
        boolean isOpenStatus = "Pending".equalsIgnoreCase(status)
                || "In Progress".equalsIgnoreCase(status);

        FollowUpRequest req = followUpController.getLatestFollowUp(currentComplaint.getComplaintId());

        // No follow-up request at all
        if (req == null) {
            titlePanel.setFollowUpBadgeVisible(false);
            titlePanel.setFollowUpVisible(!canUpdateStatus && isOpenStatus);
            followUpBadgePanel.hideRequest();
            return;
        }

        // Get the most recent status update timestamp from history
        Timestamp lastStatusUpdate = getMostRecentStatusUpdateTime(currentComplaint.getComplaintId());
        Timestamp followUpDate = req.getRequestDate();

        // Use millisecond comparison (>=) so equal timestamps still count as addressed
        boolean statusUpdatedAfterFollowUp = lastStatusUpdate != null && followUpDate != null
                && lastStatusUpdate.getTime() >= followUpDate.getTime();

        boolean isFollowUpStale = statusUpdatedAfterFollowUp;

        // Active = exists, not resolved, and not stale
        boolean hasActive = !"Resolved".equalsIgnoreCase(req.getStatus()) && !isFollowUpStale;
        boolean isResident = !canUpdateStatus;

        // Resident can request a NEW follow-up only on open statuses
        boolean showButton = !hasActive && isResident && isOpenStatus;

        titlePanel.setFollowUpBadgeVisible(hasActive);
        titlePanel.setFollowUpVisible(showButton);

        if (hasActive) {
            followUpBadgePanel.showRequest(req);
        } else {
            followUpBadgePanel.hideRequest();
        }
    }

    /**
     * Gets the most recent status update timestamp from Complaint_History_Detail.
     * Returns null if no history exists.
     */
    private Timestamp getMostRecentStatusUpdateTime(int complaintId) {
        try (Connection con = DBConnection.connect()) {
            List<ComplaintHistoryDetail> history = new GetComplaintDao().getComplaintHistory(con, complaintId);
            if (history == null || history.isEmpty()) {
                return null;
            }
            Timestamp mostRecent = null;
            for (ComplaintHistoryDetail h : history) {
                if (h.getDateTimeUpdated() != null) {
                    if (mostRecent == null || h.getDateTimeUpdated().after(mostRecent)) {
                        mostRecent = h.getDateTimeUpdated();
                    }
                }
            }
            return mostRecent;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}