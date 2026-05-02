package features.layout.common.viewreport;

import models.ComplaintAction;
import models.ComplaintHistoryDetail;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class ActionHistoryPanel extends JPanel {
    private final JPanel historyContent;

    public ActionHistoryPanel() {
        setOpaque(true);
        setBackground(UIConstants.C_CARD);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel title = new JLabel("Actions Taken");
        title.setFont(UIConstants.FONT_BOLD_15);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(10));

        historyContent = new JPanel(new GridBagLayout());
        historyContent.setName("historyContent");
        historyContent.setOpaque(false);
        historyContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(historyContent);
    }

    public void loadHistory(List<ComplaintHistoryDetail> history, ComplaintAction action) {
        historyContent.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 34);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Header row
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.weightx = 0.22;
        historyContent.add(makeHeaderLabel("Date & Time"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.13;
        historyContent.add(makeHeaderLabel("Status"), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.65;
        historyContent.add(makeHeaderLabel("Notes / Action"), gbc);

        // Header underline
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 10, 10, 24);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel headerLine = new JPanel();
        headerLine.setBackground(UIConstants.C_BORDER);
        headerLine.setPreferredSize(new Dimension(1, 1));
        historyContent.add(headerLine, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 10, 8, 24);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 2;

        if (history == null || history.isEmpty()) {
            gbc.gridy = row;
            gbc.gridx = 0;
            gbc.gridwidth = 3;
            JLabel empty = new JLabel("No actions recorded yet");
            empty.setFont(UIConstants.FONT_ITALIC_12);
            empty.setForeground(UIConstants.C_TEXT_MUTED);
            historyContent.add(empty, gbc);
        } else {
            for (ComplaintHistoryDetail h : history) {
                gbc.gridy = row;

                gbc.gridx = 0;
                gbc.weightx = 0.22;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel date = new JLabel(h.getDateTimeUpdated() != null ? h.getDateTimeUpdated().toString() : "—");
                date.setFont(UIConstants.FONT_PLAIN_12);
                historyContent.add(date, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.13;
                JLabel st = new JLabel(h.getStatus());
                st.setFont(UIConstants.FONT_BOLD_12);
                st.setForeground(StatusColorUtil.getStatusColor(h.getStatus()));
                historyContent.add(st, gbc);

                gbc.gridx = 2;
                gbc.weightx = 0.65;
                gbc.fill = GridBagConstraints.BOTH;
                JTextArea actionArea = new JTextArea(h.getProcess() != null ? h.getProcess() : "—");
                actionArea.setFont(UIConstants.FONT_PLAIN_12);
                actionArea.setOpaque(false);
                actionArea.setEditable(false);
                actionArea.setFocusable(false);
                actionArea.setLineWrap(true);
                actionArea.setWrapStyleWord(true);
                actionArea.setBorder(null);
                actionArea.setMargin(new Insets(0, 4, 0, 0));
                actionArea.setColumns(28);
                historyContent.add(actionArea, gbc);

                row++;
                gbc.gridy = row;
                gbc.gridx = 0;
                gbc.gridwidth = 3;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0, 10, 0, 24);
                JPanel sep = new JPanel();
                sep.setBackground(new Color(241, 245, 249));
                sep.setPreferredSize(new Dimension(1, 1));
                historyContent.add(sep, gbc);

                gbc.gridwidth = 1;
                gbc.insets = new Insets(8, 10, 8, 24);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                row++;
            }
        }

        // ── Resolution Summary ──
        if (action != null && action.getActionTaken() != null && !action.getActionTaken().isBlank()) {
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(16, 10, 8, 24);
            JLabel resTitle = new JLabel("Resolution Summary");
            resTitle.setFont(UIConstants.FONT_BOLD_12.deriveFont(13f));
            resTitle.setForeground(new Color(33, 115, 70));
            historyContent.add(resTitle, gbc);

            JPanel resGrid = new JPanel(new GridBagLayout());
            resGrid.setOpaque(false);

            GridBagConstraints rgbc = new GridBagConstraints();
            rgbc.anchor = GridBagConstraints.NORTHWEST;
            rgbc.insets = new Insets(4, 0, 4, 12);

            rgbc.gridx = 0;
            rgbc.gridy = 0;
            rgbc.weightx = 0;
            rgbc.fill = GridBagConstraints.NONE;
            resGrid.add(makeBoldLabel("Action Taken:"), rgbc);
            rgbc.gridx = 1;
            rgbc.weightx = 1.0;
            rgbc.fill = GridBagConstraints.HORIZONTAL;
            resGrid.add(makeValueLabel(action.getActionTaken()), rgbc);

            rgbc.gridx = 0;
            rgbc.gridy = 1;
            rgbc.weightx = 0;
            rgbc.fill = GridBagConstraints.NONE;
            resGrid.add(makeBoldLabel("Recommendation:"), rgbc);
            rgbc.gridx = 1;
            rgbc.weightx = 1.0;
            rgbc.fill = GridBagConstraints.HORIZONTAL;
            resGrid.add(makeValueLabel(action.getRecommendation()), rgbc);

            rgbc.gridx = 0;
            rgbc.gridy = 2;
            rgbc.weightx = 0;
            rgbc.fill = GridBagConstraints.NONE;
            resGrid.add(makeBoldLabel("OIC:"), rgbc);
            rgbc.gridx = 1;
            rgbc.weightx = 1.0;
            rgbc.fill = GridBagConstraints.HORIZONTAL;
            resGrid.add(makeValueLabel(action.getOIC()), rgbc);

            rgbc.gridx = 0;
            rgbc.gridy = 3;
            rgbc.weightx = 0;
            rgbc.fill = GridBagConstraints.NONE;
            resGrid.add(makeBoldLabel("Resolution Date:"), rgbc);
            rgbc.gridx = 1;
            rgbc.weightx = 1.0;
            rgbc.fill = GridBagConstraints.HORIZONTAL;
            resGrid.add(makeValueLabel(
                    action.getResolutionDateTime() != null ? action.getResolutionDateTime().toString() : "—"), rgbc);

            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 10, 0, 24);
            historyContent.add(resGrid, gbc);
        }

        historyContent.revalidate();
        historyContent.repaint();
    }

    // Old overload — keeps code compiling, but won't show resolution data
    public void loadHistory(List<ComplaintHistoryDetail> history) {
        loadHistory(history, null);
    }

    private JLabel makeHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BOLD_11);
        label.setForeground(UIConstants.C_TEXT_MUTED);
        return label;
    }

    private JLabel makeBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BOLD_12);
        return label;
    }

    private JLabel makeValueLabel(String text) {
        JLabel label = new JLabel(text != null && !text.isBlank() ? text : "—");
        label.setFont(UIConstants.FONT_PLAIN_12);
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    public Timestamp getMostRecentTimestamp(List<ComplaintHistoryDetail> history) {
        Timestamp mostRecent = null;
        if (history != null) {
            for (ComplaintHistoryDetail h : history) {
                if (h.getDateTimeUpdated() != null) {
                    if (mostRecent == null || h.getDateTimeUpdated().after(mostRecent)) {
                        mostRecent = h.getDateTimeUpdated();
                    }
                }
            }
        }
        return mostRecent;
    }
}