package features.layout.common.viewreport;

import java.awt.Color;

/**
 * Utility class for mapping complaint statuses to their corresponding colors.
 * Centralizes status-color logic previously embedded in ComplaintContentPanel.
 */
public final class StatusColorUtil {
    private StatusColorUtil() {
    }

    public static Color getStatusColor(String status) {
        if (status == null)
            return UIConstants.C_TEXT_MUTED;
        return switch (status) {
            case "Pending" -> UIConstants.C_PENDING;
            case "In Progress" -> UIConstants.C_IN_PROGRESS;
            case "Resolved" -> UIConstants.C_RESOLVED;
            case "Transferred" -> UIConstants.C_TRANSFERRED;
            case "Rejected" -> UIConstants.C_REJECTED;
            default -> UIConstants.C_TEXT_MUTED;
        };
    }

    public static int getTimelineActiveIndex(String currentStatus) {
        return switch (currentStatus) {
            case "Pending" -> 0;
            case "In Progress", "Transferred" -> 1;
            case "Resolved", "Rejected" -> 2;
            default -> -1;
        };
    }

    public static boolean isFinalStatus(String status) {
        return "Rejected".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status);
    }
}