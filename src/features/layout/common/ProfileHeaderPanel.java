package features.layout.common;

import features.components.UIButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileHeaderPanel extends JPanel {
    private final UIButton backButton;
    private final JLabel titleLabel;

    public ProfileHeaderPanel(String title, Runnable onBack) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(255, 255, 255, 240));
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // FIX: wider button so "← Back" doesn't truncate
        backButton = new UIButton("← Back", Color.WHITE, new Dimension(100, 36),
                new Font("Segoe UI", Font.BOLD, 14), 8, UIButton.ButtonType.OUTLINED);
        backButton.setBorderColor(new Color(200, 200, 200));
        backButton.addActionListener(e -> {
            if (onBack != null)
                onBack.run();
        });

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(backButton);
        add(leftPanel, BorderLayout.WEST);

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel);
        add(centerPanel, BorderLayout.CENTER);

        add(Box.createHorizontalStrut(100), BorderLayout.EAST);
    }

    public JButton getBackButton() {
        return backButton;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}