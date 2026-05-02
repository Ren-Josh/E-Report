package features.core;

import features.components.GlassPanel;
import features.core.dashboardpanel.secretary.panels.DashboardTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RecentReportsPanel extends GlassPanel {

    // ── Data ──────────────────────────────────────────────────────
    private final List<Object[]> allData = new ArrayList<>();
    private int currentPage = 0;
    private int rowsPerPage;

    // ── Row highlights: key = encoded(page, row), value = color ───
    private final Map<Long, Color> rowHighlights = new HashMap<>();

    // ── Date filter (disabled by default) ─────────────────────────
    private int dateFilterDaysBack = 0;
    private int[] dateFilterColumnIndices = new int[0];

    // ── UI ────────────────────────────────────────────────────────
    private DashboardTable table;
    private DefaultTableModel tableModel;
    private final String title;

    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    private JTextField jumpField;
    private JPanel paginationBar;

    // ── Action column ─────────────────────────────────────────────
    private int actionColumnIndex = 6;
    private final List<TableAction> tableActions = new ArrayList<>();
    private Consumer<Integer> viewClickCallback = null; // legacy fallback

    // Default action metrics (override per TableAction if desired)
    private static final int ACTION_ICON_SIZE = 16;
    private static final int ACTION_BG_SIZE = 28;
    private static final int ACTION_GAP = 8;
    private static final int ACTION_CORNER_RADIUS = 6;

    // ── Constructor ───────────────────────────────────────────────

    public RecentReportsPanel(String title, String[] columnNames) {
        this(title, columnNames, 6);
    }

    public RecentReportsPanel(String title, String[] columnNames, int rowsPerPage) {
        super(new BorderLayout(0, 8));
        this.title = title;
        this.rowsPerPage = Math.max(1, rowsPerPage);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        initializeUI(columnNames);
    }

    // ═══════════════════════════════════════════════════════════════
    // TableAction – one interactive icon per cell
    // ═══════════════════════════════════════════════════════════════

    public static class TableAction {
        private final String iconPath;
        private final Color backgroundColor; // null = transparent / no bg
        private final int iconSize;
        private final int bgSize;
        private final int cornerRadius;
        private final Consumer<Integer> onClick;

        /** Icon with colored background (e.g. purple View button). */
        public TableAction(String iconPath, Color backgroundColor, Consumer<Integer> onClick) {
            this(iconPath, backgroundColor, ACTION_ICON_SIZE, ACTION_BG_SIZE, ACTION_CORNER_RADIUS, onClick);
        }

        /** Bare icon, no background. */
        public TableAction(String iconPath, Consumer<Integer> onClick) {
            this(iconPath, null, ACTION_ICON_SIZE, ACTION_BG_SIZE, ACTION_CORNER_RADIUS, onClick);
        }

        public TableAction(String iconPath, Color backgroundColor,
                int iconSize, int bgSize, int cornerRadius,
                Consumer<Integer> onClick) {
            this.iconPath = iconPath;
            this.backgroundColor = backgroundColor;
            this.iconSize = iconSize;
            this.bgSize = bgSize;
            this.cornerRadius = cornerRadius;
            this.onClick = onClick;
        }

        public String getIconPath() {
            return iconPath;
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public int getIconSize() {
            return iconSize;
        }

        public int getBgSize() {
            return bgSize;
        }

        public int getCornerRadius() {
            return cornerRadius;
        }

        public Consumer<Integer> getOnClick() {
            return onClick;
        }
    }

    // ── Action API ────────────────────────────────────────────────

    public void addAction(TableAction action) {
        tableActions.add(action);
        refreshActionColumnRenderer();
        autoSizeActionColumn();
    }

    public void clearActions() {
        tableActions.clear();
        refreshActionColumnRenderer();
    }

    public void setActionColumnIndex(int idx) {
        this.actionColumnIndex = idx;
        refreshActionColumnRenderer();
        autoSizeActionColumn();
    }

    public int getActionColumnIndex() {
        return actionColumnIndex;
    }

    /**
     * Legacy single callback – invoked only when no TableActions are registered.
     */
    public void setOnViewClicked(Consumer<Integer> callback) {
        this.viewClickCallback = callback;
    }

    // ── Row highlight API ─────────────────────────────────────────

    public void setRowHighlights(Map<Long, Color> highlights) {
        this.rowHighlights.clear();
        if (highlights != null) {
            this.rowHighlights.putAll(highlights);
        }
        if (table != null) {
            table.repaint();
        }
    }

    public void clearRowHighlights() {
        this.rowHighlights.clear();
        if (table != null) {
            table.repaint();
        }
    }

    private static long encodePosition(int page, int row) {
        return ((long) page << 32) | (row & 0xffffffffL);
    }

    private Color getHighlightColor(int visibleRow) {
        return rowHighlights.get(encodePosition(currentPage, visibleRow));
    }

    // ── Date filter API ───────────────────────────────────────────

    /**
     * Enables a date filter. Only rows where at least one of the specified
     * columns contains a date within {@code daysBack} days from today are kept.
     *
     * @param daysBack      how many days back to look (e.g. 2 = today, yesterday,
     *                      and the day before)
     * @param columnIndices one or more column indices to check (OR logic)
     */
    public void setDateFilter(int daysBack, int... columnIndices) {
        this.dateFilterDaysBack = daysBack;
        this.dateFilterColumnIndices = columnIndices != null ? columnIndices : new int[0];
    }

    public void clearDateFilter() {
        this.dateFilterDaysBack = 0;
        this.dateFilterColumnIndices = new int[0];
    }

    private boolean passesDateFilter(Object[] rowData) {
        if (dateFilterColumnIndices.length == 0 || rowData == null) {
            return true;
        }
        LocalDate cutoff = LocalDate.now().minusDays(dateFilterDaysBack);

        for (int idx : dateFilterColumnIndices) {
            if (idx < 0 || idx >= rowData.length)
                continue;
            Object val = rowData[idx];
            if (val == null)
                continue;

            Instant inst = parseDate(val);
            if (inst == null)
                continue;

            LocalDate rowDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            if (!rowDate.isBefore(cutoff)) {
                return true; // at least one date is within range
            }
        }
        return false;
    }

    private Instant parseDate(Object obj) {
        if (obj instanceof Instant)
            return (Instant) obj;
        if (obj instanceof java.sql.Timestamp)
            return ((java.sql.Timestamp) obj).toInstant();
        if (obj instanceof java.sql.Date)
            return ((java.sql.Date) obj).toInstant();
        if (obj instanceof java.util.Date)
            return ((java.util.Date) obj).toInstant();

        if (obj instanceof String) {
            String s = ((String) obj).trim();
            if (s.isEmpty())
                return null;

            String[] patterns = {
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss.S",
                    "yyyy-MM-dd HH:mm",
                    "yyyy-MM-dd",
                    "MM/dd/yyyy HH:mm:ss",
                    "MM/dd/yyyy",
                    "MMM dd, yyyy",
                    "MMM dd, yyyy HH:mm:ss",
                    "MMM dd, yyyy hh:mm a"
            };

            for (String p : patterns) {
                try {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern(p);
                    if (p.contains("H") || p.contains("h")) {
                        return LocalDateTime.parse(s, f).atZone(ZoneId.systemDefault()).toInstant();
                    } else {
                        return LocalDate.parse(s, f).atStartOfDay(ZoneId.systemDefault()).toInstant();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    // ── UI Init ───────────────────────────────────────────────────
    private void initializeUI(String[] columnNames) {
        add(buildHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new DashboardTable(columnNames) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                // Re-apply custom highlights after DashboardTable's zebra striping
                if (!isRowSelected(row)) {
                    Color hl = getHighlightColor(row);
                    if (hl != null) {
                        c.setBackground(hl);
                    }
                }
                return c;
            }
        };
        table.setModel(tableModel);
        table.setFillsViewportHeight(false);

        // Cursor & click on action column
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == actionColumnIndex
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                table.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (col == actionColumnIndex && row >= 0) {
                    handleActionClick(e, row);
                }
            }
        });

        // Default renderer with highlight support
        HighlightCellRenderer highlightRenderer = new HighlightCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(highlightRenderer);
        }

        refreshActionColumnRenderer();

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        paginationBar = buildPaginationBar();
        add(paginationBar, BorderLayout.SOUTH);
    }

    // ── Click handling ────────────────────────────────────────────

    private void handleActionClick(MouseEvent e, int visibleRow) {
        if (tableActions.isEmpty()) {
            if (viewClickCallback != null)
                viewClickCallback.accept(getAbsoluteRowIndex(visibleRow));
            return;
        }

        Rectangle cellRect = table.getCellRect(visibleRow, actionColumnIndex, false);
        int relX = e.getX() - cellRect.x;
        int relY = e.getY() - cellRect.y;

        int totalWidth = tableActions.size() * ACTION_BG_SIZE
                + (tableActions.size() - 1) * ACTION_GAP;
        int startX = (cellRect.width - totalWidth) / 2;
        int startY = (cellRect.height - ACTION_BG_SIZE) / 2;

        // Click must be inside the vertical band where icons live
        if (relY < startY || relY > startY + ACTION_BG_SIZE)
            return;

        int absRow = getAbsoluteRowIndex(visibleRow);

        for (int i = 0; i < tableActions.size(); i++) {
            int ax = startX + i * (ACTION_BG_SIZE + ACTION_GAP);
            if (relX >= ax && relX <= ax + ACTION_BG_SIZE) {
                Consumer<Integer> cb = tableActions.get(i).getOnClick();
                if (cb != null)
                    cb.accept(absRow);
                return;
            }
        }
    }

    private void refreshActionColumnRenderer() {
        if (table == null)
            return;
        if (actionColumnIndex >= 0 && actionColumnIndex < table.getColumnCount()) {
            table.getColumnModel().getColumn(actionColumnIndex)
                    .setCellRenderer(new ActionIconsRenderer());
        }
    }

    private void autoSizeActionColumn() {
        if (table == null || tableActions.isEmpty())
            return;
        int total = tableActions.size() * ACTION_BG_SIZE
                + (tableActions.size() - 1) * ACTION_GAP
                + 20; // padding
        table.getColumnModel().getColumn(actionColumnIndex)
                .setPreferredWidth(total);
    }

    // ── Renderer: default cells with highlight support ────────────

    private class HighlightCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);

            if (!isSelected) {
                Color hl = getHighlightColor(row);
                if (hl != null) {
                    c.setBackground(hl);
                } else {
                    c.setBackground(table.getBackground());
                }
            }
            return c;
        }
    }

    // ── Renderer: centered icons with optional background ─────────

    private class ActionIconsRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel container = new JPanel(new GridBagLayout());
            container.setOpaque(true);

            if (!isSelected) {
                Color hl = getHighlightColor(row);
                if (hl != null) {
                    container.setBackground(hl);
                } else {
                    container.setBackground(t.getBackground());
                }
            } else {
                container.setBackground(t.getSelectionBackground());
            }

            if (tableActions.isEmpty()) {
                return container;
            }

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, ACTION_GAP, 0));
            wrapper.setOpaque(false);

            for (TableAction action : tableActions) {
                wrapper.add(buildActionComponent(action));
            }

            container.add(wrapper, new GridBagConstraints());
            return container;
        }

        private JPanel buildActionComponent(TableAction action) {
            JPanel p = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (action.getBackgroundColor() != null) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(action.getBackgroundColor());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                                action.getCornerRadius(), action.getCornerRadius());
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(action.getBgSize(), action.getBgSize()));

            ImageIcon icon = loadIcon(action.getIconPath(), action.getIconSize());
            JLabel iconLabel = new JLabel(icon);
            p.add(iconLabel);
            return p;
        }
    }

    // ── Icon loader: BICUBIC scale + white tint ───────────────────

    private static ImageIcon loadIcon(String path, int size) {
        try {
            Image src = new ImageIcon(path).getImage();
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // 1. Draw original icon scaled
            g2d.drawImage(src, 0, 0, size, size, null);

            // 2. Tint every opaque pixel white while preserving alpha
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, size, size);

            g2d.dispose();
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Header & Pagination (unchanged)
    // ═══════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        header.add(titleLabel, BorderLayout.WEST);

        return header;
    }

    private JPanel buildPaginationBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(new Color(220, 220, 225));
        bar.add(separator, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        controls.setOpaque(false);

        pageLabel = new JLabel();
        pageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        pageLabel.setForeground(new Color(120, 120, 130));

        jumpField = new JTextField(3) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 195, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        jumpField.setHorizontalAlignment(JTextField.CENTER);
        jumpField.setFont(new Font("Arial", Font.PLAIN, 12));
        jumpField.setForeground(new Color(180, 175, 195));
        jumpField.setBackground(new Color(248, 247, 252));
        jumpField.setOpaque(false);
        jumpField.setPreferredSize(new Dimension(44, 28));
        jumpField.setText("pg #");

        jumpField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (jumpField.getText().equals("pg #")) {
                    jumpField.setText("");
                    jumpField.setForeground(new Color(60, 60, 70));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (jumpField.getText().isBlank()) {
                    jumpField.setText("pg #");
                    jumpField.setForeground(new Color(180, 175, 195));
                }
            }
        });

        jumpField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    handleJump();
            }
        });

        JLabel jumpLabel = new JLabel("Go to:");
        jumpLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        jumpLabel.setForeground(new Color(120, 120, 130));

        JButton goButton = buildNavButton("Go");
        goButton.setPreferredSize(new Dimension(50, 28));
        goButton.addActionListener(e -> handleJump());

        prevButton = buildNavButton("‹ Prev");
        nextButton = buildNavButton("Next ›");
        prevButton.addActionListener(e -> goToPage(currentPage - 1));
        nextButton.addActionListener(e -> goToPage(currentPage + 1));

        controls.add(jumpLabel);
        controls.add(jumpField);
        controls.add(goButton);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(pageLabel);
        controls.add(Box.createHorizontalStrut(4));
        controls.add(prevButton);
        controls.add(nextButton);

        bar.add(controls, BorderLayout.CENTER);
        updatePaginationControls();
        return bar;
    }

    private JButton buildNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(240, 240, 243));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(100, 150, 255, 230));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(110, 160, 255, 240));
                } else {
                    g2.setColor(new Color(90, 140, 255, 250));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(72, 28));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addPropertyChangeListener("enabled",
                evt -> btn.setForeground(btn.isEnabled() ? Color.WHITE : new Color(180, 180, 185)));

        return btn;
    }

    private void handleJump() {
        String text = jumpField.getText().trim();
        try {
            int requested = Integer.parseInt(text);
            int target = requested - 1;
            if (target < 0 || target >= getTotalPages()) {
                jumpField.setBackground(new Color(255, 220, 220));
                Timer t = new Timer(350, e -> jumpField.setBackground(new Color(248, 247, 252)));
                t.setRepeats(false);
                t.start();
                return;
            }
            goToPage(target);
            jumpField.setText("pg #");
            jumpField.setForeground(new Color(180, 175, 195));
        } catch (NumberFormatException ex) {
            jumpField.setBackground(new Color(255, 220, 220));
            Timer t = new Timer(350, e -> jumpField.setBackground(new Color(248, 247, 252)));
            t.setRepeats(false);
            t.start();
        }
    }

    // ── Pagination Logic ──────────────────────────────────────────
    private int getTotalPages() {
        if (allData.isEmpty())
            return 0;
        return (int) Math.ceil((double) allData.size() / rowsPerPage);
    }

    private void goToPage(int page) {
        int total = getTotalPages();
        if (total == 0) {
            currentPage = 0;
            refreshPage();
            return;
        }
        currentPage = Math.max(0, Math.min(page, total - 1));
        refreshPage();
    }

    private void refreshPage() {
        tableModel.setRowCount(0);

        int from = currentPage * rowsPerPage;
        int to = Math.min(from + rowsPerPage, allData.size());

        for (int i = from; i < to; i++)
            tableModel.addRow(allData.get(i));

        updatePaginationControls();
    }

    private void updatePaginationControls() {
        if (pageLabel == null || paginationBar == null)
            return;

        int total = getTotalPages();

        boolean showPagination = total > 1;
        paginationBar.setVisible(showPagination);
        table.setFillsViewportHeight(!showPagination);

        if (total == 0) {
            pageLabel.setText("No data");
        } else {
            pageLabel.setText("Page " + (currentPage + 1) + " of " + total);
        }
        prevButton.setEnabled(currentPage > 0 && total > 0);
        nextButton.setEnabled(total > 0 && currentPage < total - 1);

        revalidate();
        repaint();
    }

    // ── Public API ────────────────────────────────────────────────

    public void addReport(Object[] reportData) {
        if (reportData == null)
            return;
        if (!passesDateFilter(reportData))
            return;
        allData.add(reportData);
        refreshPage();
    }

    public void commitReports() {
        currentPage = 0;
        refreshPage();
    }

    public void clearReports() {
        allData.clear();
        currentPage = 0;
        refreshPage();
    }

    /** Returns the filtered list currently held by the panel. */
    public List<Object[]> getAllData() {
        return new ArrayList<>(allData);
    }

    public DashboardTable getTable() {
        return table;
    }

    public void setRowsPerPage(int rows) {
        rowsPerPage = Math.max(1, rows);
        currentPage = 0;
        refreshPage();
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getAbsoluteRowIndex(int visibleRow) {
        return currentPage * rowsPerPage + visibleRow;
    }

    /**
     * Uses the original external ButtonRenderer so the button design stays intact.
     */
    public void setButtonColumn(int columnIndex, String buttonText, Color buttonColor) {
        table.getColumnModel().getColumn(columnIndex)
                .setCellRenderer(new ButtonRenderer(buttonText, buttonColor));
    }
}