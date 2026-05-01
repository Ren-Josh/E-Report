package features.layout.common.submit;

import javax.swing.*;
import java.awt.*;

/**
 * Page header with title and subtitle.
 */
public class ReportHeaderPanel extends JPanel {
    private static final Color TEXT_DARK = new Color(33, 33, 33);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);

    public ReportHeaderPanel() {
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);
        setPreferredSize(new Dimension(0, 60));

        JLabel title = new JLabel("Submit New Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Barangay Malacañang, Santa Rosa, Nueva Ecija");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        add(title, BorderLayout.NORTH);
        add(subtitle, BorderLayout.CENTER);
    }
}