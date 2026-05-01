package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class AttachmentViewerPanel extends JPanel {
    private final JLabel lblImage;
    private final JLabel lblName;
    private byte[] currentPhotoBytes;
    private String currentPhotoName;

    public AttachmentViewerPanel() {
        setLayout(new BorderLayout(8, 0));
        setOpaque(true);
        setBackground(UIConstants.C_BG_FIELD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(10, 10, 10, 10)));

        lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.LEFT);
        lblImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblImage.setToolTipText("Click to enlarge preview");
        lblImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                enlargePhoto();
            }
        });

        lblName = new JLabel("No attachments");
        lblName.setFont(UIConstants.FONT_PLAIN_13);

        add(lblImage, BorderLayout.WEST);
        add(lblName, BorderLayout.CENTER);
    }

    public void setAttachment(byte[] photo, String name) {
        this.currentPhotoBytes = photo;
        this.currentPhotoName = name;

        ImageIcon icon = new ImageIcon(photo);
        Image scaled = icon.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
        lblImage.setIcon(new ImageIcon(scaled));
        lblName.setText("  " + (name != null ? name : "Photo"));
    }

    public void clearAttachment() {
        this.currentPhotoBytes = null;
        this.currentPhotoName = null;
        lblImage.setIcon(null);
        lblName.setText("No attachments");
    }

    /** Opens a modal dialog showing the full image scaled to 95% of screen size. */
    private void enlargePhoto() {
        if (currentPhotoBytes == null || currentPhotoBytes.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No photo attached.", "No Image", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ImageIcon fullIcon = loadScaledImage(currentPhotoBytes, 0.95);
        if (fullIcon == null) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load image.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JLabel imageLabel = new JLabel(fullIcon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int padX = 60;
        int padY = 120;
        int prefW = Math.min(fullIcon.getIconWidth() + padX, screen.width - 80);
        int prefH = Math.min(fullIcon.getIconHeight() + padY, screen.height - 100);
        scrollPane.setPreferredSize(new Dimension(prefW, prefH));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Photo Preview — " + (currentPhotoName != null ? currentPhotoName : "Attachment"),
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Loads image from bytes scaled to a percentage of screen size. Never upscales
     * beyond original.
     */
    private ImageIcon loadScaledImage(byte[] data, double scale) {
        try {
            Image img = ImageIO.read(new ByteArrayInputStream(data));
            if (img == null)
                return null;

            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int maxW = (int) (screen.width * scale);
            int maxH = (int) (screen.height * scale);

            double scaleX = (double) maxW / imgW;
            double scaleY = (double) maxH / imgH;
            double s = Math.min(1.0, Math.min(scaleX, scaleY));

            int newW = (int) (imgW * s);
            int newH = (int) (imgH * s);

            Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}