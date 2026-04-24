package features.layout.captain;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import app.E_Report;
import features.components.FilterBarPanel;
import features.core.dashboardpanel.DashboardInfoCardsPanel;
import features.core.dashboardpanel.captain.*;
import models.UserSession;
import services.controller.RecentActivityController;
import services.controller.ReportStatisticsController;
import services.fetcher.CaptainDashboardFetcher;
import features.components.filter.TimeFilter;

public class CaptainDashboardPanel extends JPanel {
    private E_Report app;
    private UserSession us;
    private DashboardInfoCardsPanel infoCardsPanel;
    private FilterBarPanel filterBarPanel;
    private ReportStatisticsController rsc;
    private static final int MIN_CONTENT_WIDTH = 1000;

    private LineGraphPanel lineGraphPanel;
    private DonutChartPanel donutChartPanel;
    private StackedBarChartPanel stackedBarChartPanel;
    private BarChartPanel barChartPanel;

    private CaptainDashboardFetcher fetcher;

    public CaptainDashboardPanel(E_Report app) {
        this.app = app;
        this.us = app.getUserSession();
        rsc = new ReportStatisticsController();
        setLayout(new BorderLayout());
        setOpaque(false);

        JScrollPane scrollPane = createScrollableDashboard();
        add(scrollPane, BorderLayout.CENTER);

        this.fetcher = new CaptainDashboardFetcher(app);
        this.fetcher.addDataChangeListener(this::onDataChanged);
    }

    private void onDataChanged() {
        updateInfoCards(fetcher.getTotal(), fetcher.getPending(),
                fetcher.getInProgress(), fetcher.getResolved());

        lineGraphPanel.updateData(fetcher.getLineValues(),
                fetcher.getLineLabels(),
                fetcher.getLineDetails());

        donutChartPanel.updateData("Case Trends Category",
                fetcher.getCategoryLabels(),
                fetcher.getCategoryValues(),
                getDefaultDonutColors());

        stackedBarChartPanel.updateData("Case Status",
                fetcher.getStatusLabels(),
                fetcher.getBackgroundTotals(),
                fetcher.getStatusValues(),
                getDefaultStatusColors(),
                fetcher.getTotal());

        barChartPanel.updateData("Report Source",
                fetcher.getSourceLabels(),
                fetcher.getSourceValues(),
                getDefaultSourceColors(),
                fetcher.getTotal());
    }

    private JScrollPane createScrollableDashboard() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        String[] iconPaths = {
                "src/assets/icons/reports_icon.png",
                "src/assets/icons/report_pending_icon.png",
                "src/assets/icons/report_in_progress_icon.png",
                "src/assets/icons/resolved_icon.png"
        };
        infoCardsPanel = new DashboardInfoCardsPanel(12, 12, 12, 12, iconPaths);
        infoCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoCardsPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        filterBarPanel = new FilterBarPanel(new FilterBarPanel.FilterListener() {
            @Override
            public void onApply(Date fromDate, Date toDate, String category, String purok, String status) {
                String catFilter = (category == null || category.startsWith("All ")) ? null : category;
                String purokFilter = (purok == null || purok.startsWith("All ")) ? null : purok;
                String statusFilter = (status == null || status.startsWith("All ")) ? null : status;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String start = fromDate != null ? sdf.format(fromDate) : null;
                String end = toDate != null ? sdf.format(toDate) : null;

                TimeFilter timeFilter = filterBarPanel.getTimeFilter();
                fetcher.applyFilters(start, end, catFilter, purokFilter, statusFilter, timeFilter);
            }

            @Override
            public void onReset() {
                fetcher.applyFilters(null, null, null, null, null, null);
            }
        });
        filterBarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(filterBarPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel chartsWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chartsWrapper.setOpaque(false);
        chartsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartsWrapper.setMinimumSize(new Dimension(MIN_CONTENT_WIDTH, 280));
        chartsWrapper.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 280));

        JPanel chartsRow = createChartsRow();
        chartsRow.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 280));
        chartsWrapper.add(chartsRow);
        contentPanel.add(chartsWrapper);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel bottomWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomWrapper.setOpaque(false);
        bottomWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomWrapper.setMinimumSize(new Dimension(MIN_CONTENT_WIDTH, 250));
        bottomWrapper.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 250));

        JPanel bottomRow = createBottomRow();
        bottomRow.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 250));
        bottomWrapper.add(bottomRow);
        contentPanel.add(bottomWrapper);
        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUnitIncrement(16);
        verticalBar.setBlockIncrement(100);

        return scrollPane;
    }

    private JPanel createChartsRow() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setOpaque(false);

        lineGraphPanel = new LineGraphPanel("Monthly Case Graph", new double[0], new String[0]);
        donutChartPanel = new DonutChartPanel("Case Trends Category", new String[0], new int[0],
                getDefaultDonutColors());
        stackedBarChartPanel = new StackedBarChartPanel("Case Status", new String[0],
                new int[0], new int[0],
                getDefaultStatusColors(), 1);
        panel.add(lineGraphPanel);
        panel.add(donutChartPanel);
        panel.add(stackedBarChartPanel);
        return panel;
    }

    private JPanel createBottomRow() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);

        RecentActivityController rac = new RecentActivityController();
        List<ActivityItem> activities = rac.getRecentActivities(us, 7);
        panel.add(new RecentActivitiesPanel("Recent Activities", activities));

        barChartPanel = new BarChartPanel("Report Source", new String[0], new int[0],
                getDefaultSourceColors(), 1);
        panel.add(barChartPanel);
        return panel;
    }

    private Color[] getDefaultDonutColors() {
        return new Color[] {
                new Color(186, 85, 211), new Color(255, 193, 7),
                new Color(66, 133, 244), new Color(52, 168, 83), new Color(255, 152, 0)
        };
    }

    private Color[] getDefaultStatusColors() {
        return new Color[] {
                new Color(66, 133, 244), new Color(255, 193, 7),
                new Color(186, 85, 211), new Color(52, 168, 83), new Color(189, 189, 189)
        };
    }

    private Color[] getDefaultSourceColors() {
        return new Color[] {
                new Color(255, 193, 7), new Color(186, 85, 211), new Color(66, 133, 244)
        };
    }

    public DashboardInfoCardsPanel getInfoCardsPanel() {
        return infoCardsPanel;
    }

    public FilterBarPanel getFilterBarPanel() {
        return filterBarPanel;
    }

    public void updateInfoCards(int total, int pending, int inProgress, int resolved) {
        if (infoCardsPanel != null) {
            infoCardsPanel.updateValues(total, pending, inProgress, resolved);
        }
    }
}