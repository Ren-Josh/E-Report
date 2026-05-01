package features.layout.common.viewreport;

import models.ComplaintHistoryDetail;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Panel displaying the complaint action history.
 */
public class ActionHistoryPanel extends JPanel {

    private final JPanel historyContent;

    public ActionHistoryPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(UIConstants.C_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel title = new JLabel("Actions Taken");
        title.setFont(UIConstants.FONT_BOLD_15);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(10));

        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.C_BORDER));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerCols = new JPanel(new GridLayout(1, 3, 0, 0));
        headerCols.setOpaque(false);

        JLabel col1 = new JLabel("Date & Time");
        col1.setFont(UIConstants.FONT_BOLD_11);
        col1.setForeground(UIConstants.C_TEXT_MUTED);

        JLabel col2 = new JLabel("Status");
        col2.setFont(UIConstants.FONT_BOLD_11);
        col2.setForeground(UIConstants.C_TEXT_MUTED);

        JLabel col3 = new JLabel("Notes / Action");
        col3.setFont(UIConstants.FONT_BOLD_11);
        col3.setForeground(UIConstants.C_TEXT_MUTED);

        headerCols.add(col1);
        headerCols.add(col2);
        headerCols.add(col3);
        header.add(headerCols, BorderLayout.CENTER);

        add(header);
        add(Box.createVerticalStrut(6));

        historyContent = new JPanel();
        historyContent.setName("historyContent");
        historyContent.setOpaque(false);
        historyContent.setLayout(new BoxLayout(historyContent, BoxLayout.Y_AXIS));
        historyContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(historyContent);
    }

    public Timestamp loadHistory(List<ComplaintHistoryDetail> history) {
        historyContent.removeAll();
        Timestamp mostRecent = null;

        if (history == null || history.isEmpty()) {
            JLabel empty = new JLabel("No actions recorded yet");
            empty.setFont(UIConstants.FONT_ITALIC_12);
            empty.setForeground(UIConstants.C_TEXT_MUTED);
            historyContent.add(empty);
        } else {
            for (ComplaintHistoryDetail h : history) {
                if (h.getDateTimeUpdated() != null) {
                    if (mostRecent == null || h.getDateTimeUpdated().after(mostRecent)) {
                        mostRecent = h.getDateTimeUpdated();
                    }
                }

                JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.C_ROW_BORDER));

                JLabel date = new JLabel(h.getDateTimeUpdated() != null ? h.getDateTimeUpdated().toString() : "—");
                date.setFont(UIConstants.FONT_PLAIN_12);

                JLabel st = new JLabel(h.getStatus());
                st.setFont(UIConstants.FONT_BOLD_12);
                st.setForeground(StatusColorUtil.getStatusColor(h.getStatus()));

                JLabel action = new JLabel(h.getProcess() != null ? h.getProcess() : "—");
                action.setFont(UIConstants.FONT_PLAIN_12);

                row.add(date);
                row.add(st);
                row.add(action);
                historyContent.add(row);
            }
        }

        historyContent.revalidate();
        historyContent.repaint();
        return mostRecent;
    }

    public void showError(String message) {
        historyContent.removeAll();
        JLabel err = new JLabel(message);
        err.setForeground(Color.RED);
        historyContent.add(err);
        historyContent.revalidate();
        historyContent.repaint();
    }
}