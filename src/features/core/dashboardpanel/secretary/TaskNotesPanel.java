package features.core.dashboardpanel.secretary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TaskNotesPanel extends JPanel {

    private static final String TASKS_FILE = "data/secretary_tasks.dat";
    private static final long EXPIRY_DAYS = 3;

    private final JTextArea textArea;
    private final JButton saveButton;
    private final JLabel statusLabel;

    public TaskNotesPanel(String title) {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        // ── Title ───────────────────────────────────────────────
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 37, 41));
        add(titleLabel, BorderLayout.NORTH);

        // ── Text area ───────────────────────────────────────────
        textArea = new JTextArea();
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(new Color(250, 250, 252));
        textArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        textArea.setCaretColor(new Color(25, 118, 210));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        // ── Bottom bar (status + save) ──────────────────────────
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        saveButton = new JButton("Save Tasks") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(20, 90, 170));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 110, 200));
                } else {
                    g2.setColor(new Color(25, 118, 210));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveButton.setForeground(Color.WHITE);
        saveButton.setContentAreaFilled(false);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new Dimension(90, 30));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveTasks());

        bottom.add(statusLabel, BorderLayout.WEST);
        bottom.add(saveButton, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        loadTasks();
    }

    // ── Persistence ─────────────────────────────────────────────

    private void saveTasks() {
        try {
            File file = new File(TASKS_FILE);
            file.getParentFile().mkdirs();

            TaskData data = new TaskData(textArea.getText().trim(), System.currentTimeMillis());

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(data);
            }

            statusLabel.setText("Saved " + formatTime(data.timestamp));
            statusLabel.setForeground(new Color(46, 125, 50));
        } catch (IOException ex) {
            statusLabel.setText("Save failed");
            statusLabel.setForeground(new Color(220, 60, 60));
            ex.printStackTrace();
        }
    }

    private void loadTasks() {
        File file = new File(TASKS_FILE);
        if (!file.exists()) {
            statusLabel.setText("No saved tasks");
            statusLabel.setForeground(new Color(100, 116, 139));
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            TaskData data = (TaskData) ois.readObject();

            long daysSince = ChronoUnit.DAYS.between(
                    Instant.ofEpochMilli(data.timestamp), Instant.now());

            if (daysSince >= EXPIRY_DAYS) {
                textArea.setText("");
                statusLabel.setText("Tasks expired (" + daysSince + " days old) — cleared");
                statusLabel.setForeground(new Color(220, 60, 60));
                file.delete(); // clean up stale file
            } else {
                textArea.setText(data.text);
                long daysLeft = EXPIRY_DAYS - daysSince;
                statusLabel.setText("Expires in " + daysLeft + " day" + (daysLeft != 1 ? "s" : ""));
                statusLabel.setForeground(new Color(100, 116, 139));
            }
        } catch (IOException | ClassNotFoundException ex) {
            statusLabel.setText("Could not load tasks");
            statusLabel.setForeground(new Color(220, 60, 60));
            ex.printStackTrace();
        }
    }

    // ── Public API ──────────────────────────────────────────────

    public String getTasksText() {
        return textArea.getText();
    }

    public void setTasksText(String text) {
        textArea.setText(text);
    }

    public void clearTasks() {
        textArea.setText("");
        statusLabel.setText("Cleared");
        statusLabel.setForeground(new Color(100, 116, 139));
    }

    // ── Helpers ─────────────────────────────────────────────────

    private String formatTime(long epochMillis) {
        return DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(epochMillis));
    }

    // ── Serializable data holder ────────────────────────────────

    private static class TaskData implements Serializable {
        private static final long serialVersionUID = 1L;
        final String text;
        final long timestamp;

        TaskData(String text, long timestamp) {
            this.text = text;
            this.timestamp = timestamp;
        }
    }
}