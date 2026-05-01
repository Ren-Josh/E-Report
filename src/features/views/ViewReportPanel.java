// package features.views;

// import app.E_Report;
// import config.UIConfig;
// import config.database.DBConnection;
// import daos.FollowUpRequestDao;
// import daos.GetComplaintDao;
// import models.ComplaintDetail;
// import models.ComplaintHistoryDetail;
// import models.FollowUpRequest;
// import models.UserSession;
// import services.controller.ComplaintStatusController;
// import services.controller.FollowUpRequestController;

// import javax.swing.*;
// import javax.swing.border.EmptyBorder;
// import java.awt.*;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.SQLException;
// import java.sql.Timestamp;
// import java.util.List;

// /**
// * Modularized Complaint View & Update Panel with Follow-Up Request support.
// * <p>
// * Assembles dedicated sub-panels into a cohesive report view:
// * <ul>
// * <li>{@link HeaderPanel} – status badge, follow-up badge, title, action
// * buttons</li>
// * <li>{@link StatusTimelinePanel} – visual progress timeline</li>
// * <li>{@link UpdateStatusPanel} – status update form with conditional
// * sub-panels</li>
// * <li>{@link ComplaintDetailPanel} – read-only complaint details +
// * {@link AttachmentViewerPanel}</li>
// * <li>{@link FollowUpBadgePanel} – displays active follow-up request
// * status</li>
// * <li>{@link ActionHistoryPanel} – action history table</li>
// * </ul>
// */
// public class ViewReportPanel extends JPanel {

// private final E_Report app;
// private final boolean canUpdateStatus;

// private int currentCdId = -1;
// private ComplaintDetail currentComplaint;
// private String returnRoute = "dashboard";

// private final HeaderPanel headerPanel;
// private final StatusTimelinePanel timelinePanel;
// private final UpdateStatusPanel updatePanel;
// private final ComplaintDetailPanel detailPanel;
// private final FollowUpBadgePanel followUpBadgePanel;
// private final ActionHistoryPanel historyPanel;
// private final ComplaintStatusController statusController;
// private final FollowUpRequestController followUpController;

// public ViewReportPanel(E_Report app) {
// this.app = app;
// this.currentComplaint = app.getCurrentComplaint();
// String role = app.getUserSession() != null ? app.getUserSession().getRole() :
// "";
// this.canUpdateStatus = role.toLowerCase().contains("secretary") ||
// role.toLowerCase().contains("captain");
// this.statusController = new ComplaintStatusController();
// this.followUpController = new FollowUpRequestController();

// setLayout(new BorderLayout(0, 0));
// setOpaque(false);

// JPanel mainContent = createMainContent();
// add(mainContent, BorderLayout.CENTER);

// if (currentComplaint != null) {
// loadComplaint(currentComplaint);
// }
// }

// public void setReturnRoute(String route) {
// this.returnRoute = (route != null && !route.isBlank()) ? route : "dashboard";
// }

// private JPanel createMainContent() {
// JPanel mainContent = new JPanel();
// mainContent.setOpaque(false);
// mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
// mainContent.setBorder(new EmptyBorder(0, 4, 12, 4));

// // Back button
// JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
// backRow.setOpaque(false);
// backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
// backRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));
// JButton btnBackTop = ButtonFactory.createGhostButton("← Back");
// btnBackTop.addActionListener(e -> app.navigate(returnRoute));
// backRow.add(btnBackTop);
// mainContent.add(backRow);
// mainContent.add(Box.createVerticalStrut(8));

// // Header
// headerPanel = new HeaderPanel();
// headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
// headerPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
// headerPanel.getUpdateButton().addActionListener(e -> toggleUpdatePanel());
// headerPanel.getCancelButton().addActionListener(e -> hideUpdatePanel());
// headerPanel.getRejectButton().addActionListener(e -> rejectComplaint());
// headerPanel.getSaveButton().addActionListener(e -> saveUpdate());
// headerPanel.getFollowUpButton().addActionListener(e -> openFollowUpDialog());
// mainContent.add(headerPanel);
// mainContent.add(Box.createVerticalStrut(12));

// // Timeline
// timelinePanel = new StatusTimelinePanel();
// timelinePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
// timelinePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));
// mainContent.add(timelinePanel);
// mainContent.add(Box.createVerticalStrut(12));

// // Update panel
// updatePanel = new UpdateStatusPanel(app.getCurrentUserFullName());
// updatePanel.setVisible(false);
// updatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
// mainContent.add(updatePanel);
// mainContent.add(Box.createVerticalStrut(12));

// // Detail panel
// detailPanel = new ComplaintDetailPanel();
// detailPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
// mainContent.add(detailPanel);
// mainContent.add(Box.createVerticalStrut(12));

// // Follow-up badge
// followUpBadgePanel = new FollowUpBadgePanel();
// followUpBadgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
// mainContent.add(followUpBadgePanel);
// mainContent.add(Box.createVerticalStrut(12));

// // History panel
// historyPanel = new ActionHistoryPanel();
// historyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
// mainContent.add(historyPanel);

// // Glue
// mainContent.add(Box.createVerticalGlue());

// return mainContent;
// }

// // ==================== FOLLOW UP ====================

// private void openFollowUpDialog() {
// if (currentComplaint == null)
// return;

// // Check if already has active follow-up
// if (followUpController.hasActiveFollowUp(currentCdId)) {
// JOptionPane.showMessageDialog(this,
// "A follow-up request is already pending for this complaint.",
// "Follow Up Active", JOptionPane.INFORMATION_MESSAGE);
// return;
// }

// FollowUpDialog dialog = new FollowUpDialog(
// SwingUtilities.getWindowAncestor(this), currentCdId);
// dialog.setVisible(true);

// if (dialog.isSubmitted()) {
// UserSession session = app.getUserSession();
// int uiId = session != null ? session.getUserId() : -1;

// boolean saved = followUpController.requestFollowUp(
// currentCdId, uiId, dialog.getNotes());

// if (saved) {
// JOptionPane.showMessageDialog(this,
// "Follow-up request submitted successfully.",
// "Request Sent", JOptionPane.INFORMATION_MESSAGE);
// refreshFollowUpStatus();
// } else {
// JOptionPane.showMessageDialog(this,
// "Failed to submit follow-up request.",
// "Error", JOptionPane.ERROR_MESSAGE);
// }
// }
// }

// private void refreshFollowUpStatus() {
// FollowUpRequest latest = followUpController.getLatestFollowUp(currentCdId);
// boolean hasActive = latest != null && "Pending".equals(latest.getStatus());

// headerPanel.setFollowUpBadgeVisible(hasActive);
// followUpBadgePanel.showRequest(latest);

// // Disable follow-up button if there's an active request
// headerPanel.setFollowUpVisible(!hasActive);
// }

// // ==================== UPDATE PANEL CONTROL ====================

// private void toggleUpdatePanel() {
// if (currentComplaint != null) {
// String cur = currentComplaint.getCurrentStatus();
// if ("Rejected".equalsIgnoreCase(cur) || "Resolved".equalsIgnoreCase(cur)) {
// JOptionPane.showMessageDialog(this,
// "This complaint cannot be updated because it is already " + cur + ".",
// "Update Not Allowed", JOptionPane.INFORMATION_MESSAGE);
// return;
// }
// }

// boolean visible = !updatePanel.isVisible();
// updatePanel.setVisible(visible);
// headerPanel.setUpdateMode(visible);

// if (visible) {
// String today = new java.sql.Date(System.currentTimeMillis()).toString();
// updatePanel.reset(app.getCurrentUserFullName());
// updatePanel.populateStatusDropdown(
// currentComplaint != null ? currentComplaint.getCurrentStatus() : "Pending");
// updatePanel.setCurrentStatus(
// currentComplaint != null ? currentComplaint.getCurrentStatus() : "—");

// boolean isPending = currentComplaint != null &&
// "Pending".equals(currentComplaint.getCurrentStatus());
// updatePanel.showPendingPanel(isPending);
// headerPanel.setRejectVisible(isPending);

// if (currentComplaint != null) {
// updatePanel.prefillForPending(
// currentComplaint.getSubject(),
// currentComplaint.getType(),
// app.getCurrentUserFullName(),
// today);
// }

// SwingUtilities.invokeLater(() -> {
// Container parent = getParent();
// if (parent instanceof JViewport vp) {
// JScrollPane scroll = (JScrollPane) vp.getParent();
// if (scroll != null)
// scroll.getVerticalScrollBar().setValue(0);
// }
// });
// } else {
// headerPanel.setRejectVisible(false);
// }

// revalidate();
// repaint();
// }

// private void hideUpdatePanel() {
// updatePanel.setVisible(false);
// headerPanel.setUpdateMode(false);
// headerPanel.setRejectVisible(false);
// revalidate();
// repaint();
// }

// // ==================== REJECT COMPLAINT ====================

// private void rejectComplaint() {
// int confirm = JOptionPane.showConfirmDialog(this,
// "Are you sure you want to reject this complaint?\nThis action cannot be
// undone.",
// "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

// if (confirm != JOptionPane.YES_OPTION)
// return;

// String note = updatePanel.getPendingPanel().getPendingNotes();
// if (note.isEmpty())
// note = "Complaint rejected during validation";

// boolean saved = statusController.updateComplaintStatus(
// currentCdId, "Rejected", note, app.getUserSession());

// if (!saved) {
// JOptionPane.showMessageDialog(this, "Failed to reject complaint.", "Error",
// JOptionPane.ERROR_MESSAGE);
// return;
// }

// JOptionPane.showMessageDialog(this, "Complaint rejected successfully.",
// "Rejected",
// JOptionPane.INFORMATION_MESSAGE);
// currentComplaint.setCurrentStatus("Rejected");
// loadComplaint(currentComplaint);
// hideUpdatePanel();
// }

// // ==================== SAVE ====================

// private void saveUpdate() {
// String newStatus = updatePanel.getSelectedStatus();
// String note = updatePanel.getProcessNotes();

// if (newStatus.isBlank()) {
// JOptionPane.showMessageDialog(this, "Please select a status.", "Required",
// JOptionPane.WARNING_MESSAGE);
// return;
// }

// // Pending → In Progress validation
// if ("In Progress".equals(newStatus) &&
// "Pending".equals(currentComplaint.getCurrentStatus())) {
// String title = updatePanel.getPendingPanel().getPendingTitle();
// String type = updatePanel.getPendingPanel().getPendingType();

// if (title.isBlank() || "—".equals(title)) {
// JOptionPane.showMessageDialog(this, "Title is required.", "Required",
// JOptionPane.WARNING_MESSAGE);
// return;
// }
// if (type.isBlank() || "—".equals(type)) {
// JOptionPane.showMessageDialog(this, "Type / Category is required.",
// "Required",
// JOptionPane.WARNING_MESSAGE);
// return;
// }
// String officer = updatePanel.getPendingPanel().getPendingOfficer();
// if (officer.isBlank()) {
// JOptionPane.showMessageDialog(this, "Officer / Personnel Assigned is
// required.", "Required",
// JOptionPane.WARNING_MESSAGE);
// return;
// }

// StringBuilder sb = new StringBuilder();
// if (!note.isEmpty())
// sb.append(note).append("\n");
// sb.append("[Validated by:
// ").append(app.getCurrentUserFullName()).append("]");
// sb.append(" [Title: ").append(title).append("]");
// sb.append(" [Type: ").append(type).append("]");
// sb.append(" [Assigned to: ").append(officer).append("]");
// String pendingNotes = updatePanel.getPendingPanel().getPendingNotes();
// if (!pendingNotes.isEmpty())
// sb.append(" [Notes: ").append(pendingNotes).append("]");
// note = sb.toString();

// currentComplaint.setSubject(title);
// currentComplaint.setType(type);

// if (!updateComplaintDetail(currentCdId, title, type)) {
// JOptionPane.showMessageDialog(this, "Failed to save complaint details.",
// "Database Error",
// JOptionPane.ERROR_MESSAGE);
// return;
// }
// }
// // Normal In Progress
// else if ("In Progress".equals(newStatus)) {
// String officer = updatePanel.getInProgressPanel().getOfficer();
// if (officer.isEmpty()) {
// JOptionPane.showMessageDialog(this, "Officer / Personnel Assigned is
// required.", "Required",
// JOptionPane.WARNING_MESSAGE);
// return;
// }
// String date = updatePanel.getInProgressPanel().getAssignedDate();
// note += "\n[Assigned to: " + officer + (date.isEmpty() ? "" : " | Date: " +
// date) + "]";
// }

// if ("Resolved".equals(newStatus)) {
// var action = buildComplaintAction();
// if (action == null)
// return;
// }

// boolean saved = statusController.updateComplaintStatus(
// currentCdId, newStatus, note, app.getUserSession());

// if (!saved) {
// JOptionPane.showMessageDialog(this, "Failed to update status.", "Error",
// JOptionPane.ERROR_MESSAGE);
// return;
// }

// JOptionPane.showMessageDialog(this, "Status updated successfully!",
// "Success", JOptionPane.INFORMATION_MESSAGE);
// currentComplaint.setCurrentStatus(newStatus);
// loadComplaint(currentComplaint);
// hideUpdatePanel();
// }

// private models.ComplaintAction buildComplaintAction() {
// String actionTaken = updatePanel.getResolutionPanel().getActionTaken();
// if (actionTaken.isEmpty()) {
// JOptionPane.showMessageDialog(this, "Action Taken is required.", "Required",
// JOptionPane.WARNING_MESSAGE);
// return null;
// }

// var action = new models.ComplaintAction();
// action.setCD_ID(String.valueOf(currentCdId));
// action.setActionTaken(actionTaken);
// action.setRecommendation(updatePanel.getResolutionPanel().getRecommendation());
// action.setOIC(updatePanel.getResolutionPanel().getOIC());

// try {
// String rd = updatePanel.getResolutionPanel().getResolutionDate();
// if (!rd.isEmpty())
// action.setResolutionDateTime(Timestamp.valueOf(rd + " 00:00:00"));
// } catch (IllegalArgumentException e) {
// JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD.", "Invalid
// Date",
// JOptionPane.WARNING_MESSAGE);
// return null;
// }
// return action;
// }

// private boolean updateComplaintDetail(int cdId, String subject, String type)
// {
// String sql = "UPDATE Complaint_Detail SET subject = ?, type = ? WHERE CD_ID =
// ?";
// try (Connection con = DBConnection.connect();
// PreparedStatement ps = con.prepareStatement(sql)) {
// ps.setString(1, subject);
// ps.setString(2, type);
// ps.setInt(3, cdId);
// return ps.executeUpdate() > 0;
// } catch (SQLException e) {
// e.printStackTrace();
// return false;
// }
// }

// // ==================== DATA LOADING ====================

// public void loadComplaint(ComplaintDetail cd) {
// if (cd == null)
// return;

// hideUpdatePanel();
// this.currentComplaint = cd;
// this.currentCdId = cd.getComplaintId();

// String status = safe(cd.getCurrentStatus());
// headerPanel.setStatus(status);
// headerPanel.setTitle(cd.getComplaintId(), cd.getType());
// headerPanel.setUpdateVisible(canUpdateStatus &&
// !StatusColorUtil.isFinalStatus(status));

// detailPanel.loadComplaint(cd);
// timelinePanel.updateTimeline(status);

// loadHistory(cd.getComplaintId());
// refreshFollowUpStatus();
// }

// private void loadHistory(int complaintId) {
// try (Connection con = DBConnection.connect()) {
// List<ComplaintHistoryDetail> history = new
// GetComplaintDao().getComplaintHistory(con, complaintId);
// Timestamp mostRecent = historyPanel.loadHistory(history);
// detailPanel.setLastUpdate(mostRecent != null ? mostRecent.toString() : "—");
// } catch (SQLException e) {
// historyPanel.showError("Unable to load history");
// detailPanel.setLastUpdate("—");
// e.printStackTrace();
// }
// }

// private String safe(String v) {
// return v != null && !v.isBlank() ? v : "—";
// }
// }