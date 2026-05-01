package features.layout.common.viewreport;

import java.awt.Color;
import java.awt.Font;

/**
 * Shared UI constants for the View Report module.
 * Extracted from ComplaintContentPanel for reusability across sub-panels.
 */
public final class UIConstants {
    private UIConstants() {
    }

    public static final Font FONT_BOLD_11 = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_BOLD_12 = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_BOLD_13 = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BOLD_15 = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD_16 = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD_22 = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_PLAIN_11 = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_PLAIN_12 = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_PLAIN_13 = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_ITALIC_12 = new Font("Segoe UI", Font.ITALIC, 12);

    public static final Color C_PENDING = new Color(245, 158, 11);
    public static final Color C_IN_PROGRESS = new Color(59, 130, 246);
    public static final Color C_RESOLVED = new Color(16, 185, 129);
    public static final Color C_TRANSFERRED = new Color(139, 92, 246);
    public static final Color C_REJECTED = new Color(239, 68, 68);
    public static final Color C_CARD = new Color(255, 255, 255, 245);
    public static final Color C_BORDER = new Color(226, 232, 240);
    public static final Color C_BG_FIELD = new Color(248, 250, 252);
    public static final Color C_BG_EDITABLE = new Color(255, 255, 255);
    public static final Color C_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color C_TIMELINE_INACTIVE = new Color(203, 213, 225);
    public static final Color C_ROW_BORDER = new Color(241, 245, 249);
    public static final Color C_UPDATE_BORDER = new Color(59, 130, 246, 90);
    public static final Color C_RESOLUTION_BORDER = new Color(16, 185, 129);

    public static final String[] TIMELINE_LABELS = { "Submitted", "In Progress", "Resolved" };
}