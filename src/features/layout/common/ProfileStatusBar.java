package features.layout.common;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class ProfileStatusBar extends JPanel {
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(226, 232, 240);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color SUCCESS = new Color(34, 197, 94);

    private final JLabel statusLabel;

    public ProfileStatusBar() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(CARD_BG);
        setBorder(new MatteBorder(1, 0, 0, 0, CARD_BORDER));
        setPreferredSize(new Dimension(0, 40));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        add(statusLabel, BorderLayout.WEST);
    }

    public void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);

        if (color == SUCCESS) {
            Timer timer = new Timer(3000, e -> {
                if (statusLabel.getText().equals(message)) {
                    statusLabel.setText("");
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void clearStatus() {
        statusLabel.setText("");
    }

    public String getStatusText() {
        return statusLabel.getText();
    }
}