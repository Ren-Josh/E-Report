package features.core.dashboardpanel.captain;

import config.UIConfig;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class RecentActivitiesPanel extends BaseCardPanel {
    private final String panelTitle;
    private List<ActivityItem> activities;
    private boolean compact = false;
    private int lastPanelWidth = 400; // best-guess until first layout

    public RecentActivitiesPanel(String title, List<ActivityItem> activities) {
        super(title);
        this.panelTitle = title;
        this.activities = activities;
        setLayout(new BorderLayout(UIConfig.SM, UIConfig.SM));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = getWidth();
                if (w == 0)
                    return;
                lastPanelWidth = w;
                boolean nowCompact = w < UIConfig.ACTIVITY_COMPACT_THRESHOLD;
                if (nowCompact != compact) {
                    compact = nowCompact;
                    rebuild();
                } else {
                    // width changed but mode stayed the same – just relayout so rows
                    // recalculate their wrapped heights against the new width
                    revalidate();
                    repaint();
                }
            }
        });

        rebuild();
    }

    public void updateActivities(List<ActivityItem> newActivities) {
        this.activities = newActivities;
        rebuild();
    }

    /* ------------------------------------------------------------------ */
    /* Call this from a button / timer after the UI is visible so you */
    /* can paste the console output back for diagnosis. */
    /* ------------------------------------------------------------------ */

    private void rebuild() {
        removeAll();

        JLabel titleLabel = new JLabel(panelTitle);
        titleLabel.setFont(compact ? UIConfig.ACTIVITY_TITLE_FONT_COMPACT : UIConfig.ACTIVITY_TITLE_FONT);
        titleLabel.setForeground(UIConfig.TEXT_DARK);
        add(titleLabel, BorderLayout.NORTH);

        ScrollableListPanel listPanel = new ScrollableListPanel();
        listPanel.setLayout(new GridBagLayout());
        listPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        int gridy = 0;
        for (int i = 0; i < activities.size(); i++) {
            gbc.gridy = gridy++;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.weighty = 0;
            listPanel.add(createRow(activities.get(i)), gbc);

            if (i < activities.size() - 1) {
                JSeparator sep = new JSeparator();
                sep.setForeground(UIConfig.BORDER_LIGHT);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setPreferredSize(new Dimension(1, 1));
                gbc.gridy = gridy++;
                gbc.insets = new Insets(compact ? UIConfig.XS : UIConfig.SM, 0, 0, 0);
                listPanel.add(sep, gbc);
                gbc.insets = new Insets(0, 0, 0, 0);
            }
        }

        // glue pushes rows to the top
        gbc.gridy = gridy;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        listPanel.add(Box.createGlue(), gbc);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();

        /* ── NEW: force scrollbar to the top so latest activity is visible ── */
        SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(new Point(0, 0)));
    }

    private JPanel createRow(ActivityItem item) {
        final int padV = compact ? UIConfig.XS : UIConfig.SM;
        final int padR = compact ? UIConfig.SM : UIConfig.MD;
        final int rwMax = compact ? UIConfig.ACTIVITY_RIGHT_COL_WIDTH_COMPACT
                : UIConfig.ACTIVITY_RIGHT_COL_WIDTH;
        final int iw = compact ? UIConfig.ACTIVITY_ICON_WIDTH_COMPACT : UIConfig.ACTIVITY_ICON_WIDTH;

        // ---- Icon ----
        JPanel icon = new JPanel();
        icon.setPreferredSize(new Dimension(iw, 32));
        icon.setMinimumSize(new Dimension(iw, 32));
        icon.setBackground(UIConfig.ACCENT_BLUE);
        icon.setOpaque(true);

        // ---- Title ----
        JLabel title = new JLabel(item.getTitle());
        title.setFont(compact ? UIConfig.ACTIVITY_ITEM_TITLE_FONT_COMPACT : UIConfig.ACTIVITY_ITEM_TITLE_FONT);
        title.setForeground(UIConfig.TEXT_DARK);

        // ---- Description ----
        JTextArea descArea = new JTextArea(item.getDescription());
        descArea.setFont(compact ? UIConfig.ACTIVITY_DESC_FONT_COMPACT : UIConfig.ACTIVITY_DESC_FONT);
        descArea.setForeground(UIConfig.TEXT_MUTED);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setHighlighter(null);
        descArea.setBorder(null);
        descArea.setMargin(new Insets(0, 0, 0, 0));
        descArea.setColumns(0);

        // ---- Right column (time / date) ----
        JLabel time = new JLabel(item.getTime());
        time.setFont(compact ? UIConfig.ACTIVITY_META_FONT_COMPACT : UIConfig.ACTIVITY_META_FONT);
        time.setForeground(UIConfig.TEXT_MUTED);
        time.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel date = new JLabel(item.getDate());
        date.setFont(compact ? UIConfig.ACTIVITY_META_FONT_COMPACT : UIConfig.ACTIVITY_META_FONT);
        date.setForeground(UIConfig.TEXT_MUTED);
        date.setHorizontalAlignment(SwingConstants.RIGHT);

        // FIX: size the right column to the *actual* label width instead of
        // always eating the full 150 px constant. This gives the description
        // the maximum possible space before wrapping.
        int naturalRightW = Math.max(time.getPreferredSize().width,
                date.getPreferredSize().width) + 10;
        final int actualRw = Math.min(rwMax, naturalRightW);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 2));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(actualRw, 40));
        right.add(time);
        right.add(date);

        // ---- Text panel ----
        JPanel textPanel = new JPanel(new BorderLayout(0, 2));
        textPanel.setOpaque(false);
        textPanel.add(title, BorderLayout.NORTH);
        textPanel.add(descArea, BorderLayout.CENTER);

        // ---- Row with dynamic height based on real wrapped text size ----
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override
            public Dimension getPreferredSize() {
                Container parent = getParent();
                int pw = (parent != null && parent.getWidth() > 0)
                        ? parent.getWidth()
                        : lastPanelWidth;

                // width available for the description after icon, gaps, right col, padding
                int textWidth = Math.max(0, pw - padR - iw - 20 - actualRw);

                if (textWidth > 0) {
                    int descHeight = measureWrappedHeight(descArea, textWidth);
                    // tell the text area exactly how wide it will be so it wraps correctly
                    descArea.setPreferredSize(new Dimension(textWidth, descHeight));

                    int textPanelHeight = title.getPreferredSize().height + 2 + descHeight;
                    int contentHeight = Math.max(32,
                            Math.max(textPanelHeight, right.getPreferredSize().height));
                    int rowHeight = contentHeight + padV * 2;
                    return new Dimension(pw, rowHeight);
                }
                return super.getPreferredSize();
            }
        };

        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(padV, 0, padV, padR));
        row.add(icon, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private int measureWrappedHeight(JTextArea reference, int width) {
        JTextArea measure = new JTextArea(reference.getText());
        measure.setFont(reference.getFont());
        measure.setLineWrap(true);
        measure.setWrapStyleWord(true);
        measure.setMargin(new Insets(0, 0, 0, 0));
        measure.setSize(width, Short.MAX_VALUE);
        int h = measure.getPreferredSize().height;
        return h;
    }

    private static class ScrollableListPanel extends JPanel implements Scrollable {
        public ScrollableListPanel() {
            super();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return r.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // ← this is the magic that removes manual scrollbar math
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}