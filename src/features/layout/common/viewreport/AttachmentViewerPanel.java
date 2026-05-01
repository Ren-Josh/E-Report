package features.layout.common.viewreport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

/**
 * Panel for displaying complaint photo attachments.
 * <p>
 * Extracted from ComplaintContentPanel to provide a reusable, dedicated
 * image viewing component that can be merged into any report view.
 * <p>
 * Supports click-to-enlarge with screen-aware scaling (up to 95% of screen
 * size).
 * Never upscales beyond original resolution to keep quality crisp.
 */
public class AttachmentViewerPanel extends JPanel {

    private final JLabel imageLabel;
    private byte[] currentImageData;
    private String currentImageName;

    private static final int THUMB_HEIGHT = 80;

    public AttachmentViewerPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UIConstants.C_BG_FIELD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        imageLabel = new JLabel("No attachments", SwingConstants.LEFT);
        imageLabel.setFont(UIConstants.FONT_PLAIN_13);
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentImageData != null && currentImageData.length > 0) {
                    enlargePhoto();
                }
            }
        });

        add(imageLabel, BorderLayout.CENTER);
    }

    /**
     * Displays the attachment from raw bytes.
     *
     * @param photoBytes the image data
     * @param photoName  the file name for display
     */
    public void setAttachment(byte[] photoBytes, String photoName) {
        this.currentImageData = photoBytes;
        this.currentImageName = photoName;

        if (photoBytes != null && photoBytes.length > 0) {
            ImageIcon thumbIcon = createThumbnailIcon(photoBytes, THUMB_HEIGHT);
            imageLabel.setIcon(thumbIcon);
            imageLabel.setText("  " + (photoName != null ? photoName : "Photo") +
                    "  (Click to enlarge)");
            imageLabel.setToolTipText("Click to view full size");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("No attachments");
            imageLabel.setToolTipText(null);
        }
    }

    /**
     * Clears the current attachment display.
     */
    public void clearAttachment() {
        this.currentImageData = null;
        this.currentImageName = null;
        imageLabel.setIcon(null);
        imageLabel.setText("No attachments");
        imageLabel.setToolTipText(null);
    }

    // ================== Photo Enlargement ==================

    /**
     * Opens a modal JOptionPane showing the image scaled up to 95% of screen size.
     * Uses JScrollPane so even massive images are scrollable. Zero impact on the
     * content panel layout because it is a separate modal dialog.
     */
    private void enlargePhoto() {
        if (currentImageData == null || currentImageData.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No photo available. Please upload a photo first.",
                    "No Image", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ImageIcon fullIcon = loadScaledImage(currentImageData, 0.95);
        if (fullIcon == null) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load image.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JLabel imageLabel = new JLabel(fullIcon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Size the scroll pane to the image + padding, capped only by screen bounds
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int padX = 60;
        int padY = 120; // room for title bar + buttons
        int prefW = Math.min(fullIcon.getIconWidth() + padX, screen.width - 80);
        int prefH = Math.min(fullIcon.getIconHeight() + padY, screen.height - 100);
        scrollPane.setPreferredSize(new Dimension(prefW, prefH));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Photo Preview — " + (currentImageName != null ? currentImageName : "Image"),
                JOptionPane.PLAIN_MESSAGE);
    }

    /** Tiny inline thumbnail for the label beside the filename. */
    private ImageIcon createThumbnailIcon(byte[] imageData, int height) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage img = ImageIO.read(bais);
            if (img == null)
                return null;

            int width = (int) ((double) img.getWidth() / img.getHeight() * height);
            if (width <= 0)
                width = height;
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Loads an image scaled to a percentage of the current screen size.
     * Never upscales beyond the original resolution (keeps quality crisp).
     *
     * @param scale 0.0–1.0 (e.g. 0.95 = 95% of screen)
     */
    private ImageIcon loadScaledImage(byte[] imageData, double scale) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage original = ImageIO.read(bais);
            if (original == null)
                return null;

            int imgW = original.getWidth();
            int imgH = original.getHeight();

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int maxW = (int) (screen.width * scale);
            int maxH = (int) (screen.height * scale);

            double scaleX = (double) maxW / imgW;
            double scaleY = (double) maxH / imgH;
            double s = Math.min(1.0, Math.min(scaleX, scaleY));

            int newW = (int) (imgW * s);
            int newH = (int) (imgH * s);

            Image scaled = original.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}