package features.layout.common;

import features.components.UICard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class ProfileInfoCard extends UICard {
    private static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final int SPACING_MD = 16;
    private static final int SPACING_XS = 4;

    private final JLabel displayNameLabel;
    private final JLabel displayRoleLabel;
    private final JPanel avatarWrapper;
    private String initial = "U";

    public ProfileInfoCard() {
        super(12, Color.WHITE);
        setLayout(new BorderLayout());
        setShowBorder(true);
        setBorderColor(new Color(226, 232, 240));

        // FIX: explicit sizing so BoxLayout doesn't squash or collapse this card
        setPreferredSize(new Dimension(0, 100));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel content = new JPanel(new BorderLayout(SPACING_MD, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));

        avatarWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight());
                Ellipse2D circle = new Ellipse2D.Float(0, 0, size, size);

                GradientPaint avatarGradient = new GradientPaint(
                        0, 0, PRIMARY_BLUE.brighter(),
                        size, size, PRIMARY_BLUE);
                g2.setPaint(avatarGradient);
                g2.fill(circle);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (size - fm.stringWidth(initial)) / 2,
                        ((size - fm.getHeight()) / 2) + fm.getAscent());

                g2.setColor(new Color(255, 255, 255, 150));
                g2.setStroke(new BasicStroke(2));
                g2.draw(circle);

                g2.dispose();
            }
        };
        avatarWrapper.setPreferredSize(new Dimension(72, 72));
        avatarWrapper.setOpaque(false);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, SPACING_XS));
        infoPanel.setOpaque(false);

        displayNameLabel = new JLabel("User");
        displayNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        displayNameLabel.setForeground(TEXT_PRIMARY);

        displayRoleLabel = new JLabel("Role");
        displayRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        displayRoleLabel.setForeground(TEXT_SECONDARY);

        infoPanel.add(displayNameLabel);
        infoPanel.add(displayRoleLabel);

        content.add(avatarWrapper, BorderLayout.WEST);
        content.add(infoPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    public void setDisplayName(String name) {
        displayNameLabel.setText(name);
        this.initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "U";
        repaintAvatar();
    }

    public void setDisplayRole(String role) {
        displayRoleLabel.setText(role);
    }

    public void repaintAvatar() {
        avatarWrapper.repaint();
    }

    public String getDisplayName() {
        return displayNameLabel.getText();
    }
}