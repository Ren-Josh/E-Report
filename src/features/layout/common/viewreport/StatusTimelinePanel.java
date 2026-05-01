package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Visual timeline showing complaint progress: Submitted → In Progress →
 * Resolved.
 */
public class StatusTimelinePanel extends JPanel {

    private final JLabel[] timelineSteps;
    private final JLabel[] timelineLabels;
    private final JPanel[] timelineConnectors;

    public StatusTimelinePanel() {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(UIConstants.C_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        timelineSteps = new JLabel[UIConstants.TIMELINE_LABELS.length];
        timelineLabels = new JLabel[UIConstants.TIMELINE_LABELS.length];
        timelineConnectors = new JPanel[UIConstants.TIMELINE_LABELS.length - 1];

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        for (int i = 0; i < UIConstants.TIMELINE_LABELS.length; i++) {
            JLabel circle = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            circle.setFont(UIConstants.FONT_BOLD_12);
            circle.setForeground(Color.WHITE);
            circle.setOpaque(true);
            circle.setBackground(UIConstants.C_TIMELINE_INACTIVE);
            circle.setPreferredSize(new Dimension(28, 28));
            circle.setMinimumSize(new Dimension(28, 28));
            circle.setMaximumSize(new Dimension(28, 28));
            timelineSteps[i] = circle;

            JLabel label = new JLabel(UIConstants.TIMELINE_LABELS[i], SwingConstants.CENTER);
            label.setFont(UIConstants.FONT_BOLD_11);
            label.setForeground(UIConstants.C_TEXT_MUTED);
            timelineLabels[i] = label;

            JPanel stepPanel = new JPanel();
            stepPanel.setOpaque(false);
            stepPanel.setLayout(new BoxLayout(stepPanel, BoxLayout.Y_AXIS));
            circle.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            stepPanel.add(circle);
            stepPanel.add(Box.createVerticalStrut(4));
            stepPanel.add(label);

            gbc.gridx = i * 2;
            gbc.weightx = 0;
            add(stepPanel, gbc);

            if (i < UIConstants.TIMELINE_LABELS.length - 1) {
                JPanel line = new JPanel();
                line.setOpaque(true);
                line.setBackground(UIConstants.C_TIMELINE_INACTIVE);
                line.setPreferredSize(new Dimension(60, 3));
                line.setMaximumSize(new Dimension(Short.MAX_VALUE, 3));
                timelineConnectors[i] = line;

                gbc.gridx = i * 2 + 1;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(0, 6, 10, 6);
                add(line, gbc);
                gbc.insets = new Insets(0, 0, 0, 0);
            }
        }
    }

    public void updateTimeline(String currentStatus) {
        int activeIndex = StatusColorUtil.getTimelineActiveIndex(currentStatus);
        Color activeColor = StatusColorUtil.getStatusColor(currentStatus);
        if (activeColor.equals(UIConstants.C_TEXT_MUTED)) {
            activeColor = UIConstants.C_IN_PROGRESS;
        }

        for (int i = 0; i < UIConstants.TIMELINE_LABELS.length; i++) {
            if (i <= activeIndex && activeIndex >= 0) {
                timelineSteps[i].setBackground(activeColor);
                timelineSteps[i].setForeground(Color.WHITE);
                timelineLabels[i].setForeground(activeColor);
                timelineSteps[i].setText(i < activeIndex ? "\u2714" : String.valueOf(i + 1));
            } else {
                timelineSteps[i].setBackground(UIConstants.C_TIMELINE_INACTIVE);
                timelineSteps[i].setForeground(Color.WHITE);
                timelineLabels[i].setForeground(UIConstants.C_TEXT_MUTED);
                timelineSteps[i].setText(String.valueOf(i + 1));
            }

            if (i < UIConstants.TIMELINE_LABELS.length - 1) {
                timelineConnectors[i].setBackground(
                        i < activeIndex && activeIndex >= 0 ? activeColor : UIConstants.C_TIMELINE_INACTIVE);
            }
        }
    }
}