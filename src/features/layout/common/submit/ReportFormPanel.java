package features.layout.common.submit;

import config.AppConfig;
import features.components.UIComboBox;
import features.components.UIInput;
import features.ui.DashboardFormUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import app.E_Report;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Left-column panel containing all form fields, photo upload, status label,
 * and action buttons (Clear / Submit).
 */
public class ReportFormPanel extends JPanel {
    protected E_Report app;
    private final UIComboBox<String> categoryCombo;
    private final UIComboBox<String> purokCombo;
    private final UIInput locationField;
    private final UIInput latitudeField;
    private final UIInput longitudeField;
    private final JTextArea detailsArea;
    private JLabel selectedPhotoLabel;
    private JLabel formStatusLabel;
    private JButton submitBtn;
    private JButton clearBtn;
    private File selectedFile;

    private static final Color ACCENT_GREEN = new Color(76, 175, 80);
    private static final Color ACCENT_RED = new Color(244, 67, 54);
    private static final Color TEXT_DARK = new Color(33, 33, 33);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color BG_INPUT = new Color(250, 250, 250);
    private static final Color BORDER_INPUT = new Color(224, 224, 224);

    private Runnable formChangeListener;

    public ReportFormPanel(E_Report app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        setMinimumSize(new Dimension(400, 0));
        setPreferredSize(new Dimension(550, 0));

        // --- Inputs ---
        categoryCombo = new UIComboBox<>(AppConfig.COMPLAINT_TYPES);
        UIComboBox.applyPreset(categoryCombo, 200);
        categoryCombo.setSelectedIndex(0);

        purokCombo = new UIComboBox<>(AppConfig.REPORT_PUROK_OPTIONS);
        UIComboBox.applyPreset(purokCombo, 200);
        purokCombo.setSelectedIndex(0);

        locationField = new UIInput(10);
        latitudeField = new UIInput(10);
        longitudeField = new UIInput(10);
        detailsArea = new JTextArea(4, 22);

        selectedPhotoLabel = new JLabel("No photo selected");
        selectedPhotoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectedPhotoLabel.setToolTipText("Click to enlarge preview");
        selectedPhotoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                enlargePhoto();
            }
        });

        formStatusLabel = new JLabel(" ");
        formStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        locationField.setEditable(true);
        locationField.setFont(new Font("Arial", Font.PLAIN, 12));
        latitudeField.setEditable(false);
        latitudeField.setFont(new Font("Arial", Font.PLAIN, 12));
        longitudeField.setEditable(false);
        longitudeField.setFont(new Font("Arial", Font.PLAIN, 12));
        locationField.setBackground(new Color(245, 245, 245));
        latitudeField.setBackground(new Color(245, 245, 245));
        longitudeField.setBackground(new Color(245, 245, 245));

        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        detailsArea.setBackground(BG_INPUT);

        DashboardFormUtils.installPlaceholder(locationField, AppConfig.REPORT_LOCATION_PLACEHOLDER);

        // --- Layout ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 14, 0);
        gbc.gridx = 0;

        gbc.gridy = 0;
        add(new ReportHeaderPanel(), gbc);

        gbc.gridy = 1;
        add(createCompactRow(
                createStyledLabeledField("Category *", categoryCombo, true),
                createStyledLabeledField("Purok *", purokCombo, true)), gbc);

        gbc.gridy = 2;
        add(createStyledLabeledField("Location / Purok *", locationField, false), gbc);

        gbc.gridy = 3;
        add(createCoordinatesRow(), gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(createDescriptionField(), gbc);

        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        add(createPhotoUploadRow(), gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 8, 0);
        add(formStatusLabel, gbc);

        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(createActionButtons(), gbc);

        initListeners();
    }

    /* ================== UI Helpers ================== */

    private JPanel createCompactRow(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 8);
        row.add(left, gbc);
        gbc.insets = new Insets(0, 8, 0, 0);
        row.add(right, gbc);
        return row;
    }

    private JPanel createStyledLabeledField(String labelText, Component field, boolean isCombo) {
        JPanel container = new JPanel(new BorderLayout(0, 6));
        container.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        container.add(label, BorderLayout.NORTH);

        if (field instanceof JComponent) {
            ((JComponent) field).setPreferredSize(null);
        }
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(field, BorderLayout.CENTER);
        container.add(wrap, BorderLayout.CENTER);
        return container;
    }

    private JPanel createCoordinatesRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 8);
        row.add(createStyledLabeledField("Latitude", latitudeField, false), gbc);
        gbc.insets = new Insets(0, 8, 0, 0);
        row.add(createStyledLabeledField("Longitude", longitudeField, false), gbc);
        return row;
    }

    private JPanel createDescriptionField() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);

        JLabel label = new JLabel("Description / Details *");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        wrapper.add(label, BorderLayout.NORTH);

        detailsArea.setRows(4);
        JScrollPane scroll = new JScrollPane(detailsArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_INPUT, 1, true));
        scroll.setBackground(BG_INPUT);
        scroll.setPreferredSize(new Dimension(0, 120));
        wrapper.add(scroll, BorderLayout.CENTER);

        JLabel charCount = new JLabel("0 / 500 characters");
        charCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        charCount.setForeground(TEXT_MUTED);
        wrapper.add(charCount, BorderLayout.SOUTH);

        detailsArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                int len = detailsArea.getText().length();
                charCount.setText(len + " / 500 characters");
                charCount.setForeground(len > 500 ? ACCENT_RED : TEXT_MUTED);
                notifyFormChanged();
            }
        });
        return wrapper;
    }

    private JPanel createPhotoUploadRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        selectedPhotoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        selectedPhotoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        selectedPhotoLabel.setBackground(BG_INPUT);
        selectedPhotoLabel.setOpaque(true);

        JButton uploadBtn = createStyledButton("Upload Photo", new Color(33, 150, 243));
        uploadBtn.setPreferredSize(new Dimension(140, 40));
        uploadBtn.addActionListener(e -> choosePhoto());

        row.add(selectedPhotoLabel, BorderLayout.CENTER);
        row.add(uploadBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel createActionButtons() {
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setOpaque(false);

        clearBtn = createStyledButton("Clear", new Color(158, 158, 158));
        submitBtn = createStyledButton("Submit Report", ACCENT_GREEN);
        submitBtn.setEnabled(false);
        submitBtn.setBackground(new Color(200, 200, 200));

        actions.add(Box.createHorizontalGlue());
        actions.add(clearBtn);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(submitBtn);
        return actions;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bgColor);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        b.setPreferredSize(new Dimension(140, 42));
        return b;
    }

    /* ================== Logic ================== */

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Photo Evidence");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (JPG, PNG)", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            selectedPhotoLabel.setText(truncateFileName(selectedFile.getName(), 20));
            selectedPhotoLabel.setForeground(ACCENT_GREEN);
            selectedPhotoLabel.setIcon(createThumbnailIcon(selectedFile, 24));
            notifyFormChanged();
            revalidate();
            repaint();
        }
    }

    private String truncateFileName(String fileName, int maxLength) {
        if (fileName == null || fileName.length() <= maxLength)
            return fileName;
        int part = (maxLength - 3) / 2;
        return fileName.substring(0, part) + "..." + fileName.substring(fileName.length() - part);
    }

    private void initListeners() {
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifyFormChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notifyFormChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notifyFormChanged();
            }
        };
        locationField.getDocument().addDocumentListener(dl);
        categoryCombo.addActionListener(e -> notifyFormChanged());
        purokCombo.addActionListener(e -> notifyFormChanged());
    }

    private void notifyFormChanged() {
        if (formChangeListener != null)
            formChangeListener.run();
    }

    public void setFormChangeListener(Runnable listener) {
        this.formChangeListener = listener;
    }

    /* ================== Public API ================== */

    public String getCategory() {
        return getSelectedComboValue(categoryCombo, AppConfig.REPORT_CATEGORY_PLACEHOLDER);
    }

    public String getPurok() {
        return getSelectedComboValue(purokCombo, AppConfig.REPORT_PUROK_PLACEHOLDER);
    }

    public String getLocationText() {
        String loc = locationField.getText().trim();
        return AppConfig.REPORT_LOCATION_PLACEHOLDER.equals(loc) ? "" : loc;
    }

    public String getDetails() {
        return detailsArea.getText().trim();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setLatitude(String lat) {
        latitudeField.setText(lat);
    }

    public void setLongitude(String lon) {
        longitudeField.setText(lon);
    }

    public void setLocationText(String location) {
        if (location != null && !location.isBlank()) {
            locationField.setForeground(TEXT_DARK);
            locationField.setText(location);
        }
    }

    public boolean isFormReady(boolean pinConfirmed) {
        if (!pinConfirmed)
            return false;
        if (getCategory().isEmpty())
            return false;
        if (getPurok().isEmpty())
            return false;
        if (getLocationText().isEmpty())
            return false;
        return !getDetails().isEmpty();
    }

    public void setFormStatus(String text, Color color) {
        formStatusLabel.setText(text);
        formStatusLabel.setForeground(color);
    }

    public void setSubmitEnabled(boolean enabled) {
        submitBtn.setEnabled(enabled);
        submitBtn.setBackground(enabled ? ACCENT_GREEN : new Color(200, 200, 200));
        submitBtn.setForeground(enabled ? Color.WHITE : new Color(120, 120, 120));
    }

    public void clearForm() {
        categoryCombo.setSelectedIndex(0);
        purokCombo.setSelectedIndex(0);
        locationField.setText(AppConfig.REPORT_LOCATION_PLACEHOLDER);
        locationField.setForeground(TEXT_MUTED);
        detailsArea.setText("");
        latitudeField.setText("");
        longitudeField.setText("");
        selectedFile = null;
        selectedPhotoLabel.setText("No photo selected");
        selectedPhotoLabel.setIcon(null);
        selectedPhotoLabel.setForeground(TEXT_DARK);
        setFormStatus(" ", TEXT_MUTED);
    }

    public JButton getSubmitBtn() {
        return submitBtn;
    }

    public JButton getClearBtn() {
        return clearBtn;
    }

    private String getSelectedComboValue(UIComboBox<String> combo, String placeholder) {
        Object sel = combo.getSelectedItem();
        if (sel == null)
            return "";
        String val = sel.toString().trim();
        return placeholder.equals(val) ? "" : val;
    }

    /* ================== Photo Enlargement ================== */

    /**
     * Opens a modal JOptionPane showing the image scaled up to 95% of screen size.
     * Uses JScrollPane so even massive images are scrollable. Zero impact on the
     * content panel layout because it is a separate modal dialog.
     */
    private void enlargePhoto() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(app,
                    "No photo selected. Please upload a photo first.",
                    "No Image", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ImageIcon fullIcon = loadScaledImage(selectedFile, 0.95);
        if (fullIcon == null) {
            JOptionPane.showMessageDialog(app,
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
                "Photo Preview — " + selectedFile.getName(),
                JOptionPane.PLAIN_MESSAGE);
    }

    /** Tiny inline thumbnail for the label beside the filename. */
    private ImageIcon createThumbnailIcon(File file, int height) {
        try {
            Image img = new ImageIcon(file.getAbsolutePath()).getImage();
            int width = (int) ((double) img.getWidth(null) / img.getHeight(null) * height);
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
    private ImageIcon loadScaledImage(File file, double scale) {
        try {
            ImageIcon original = new ImageIcon(file.getAbsolutePath());
            int imgW = original.getIconWidth();
            int imgH = original.getIconHeight();

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int maxW = (int) (screen.width * scale);
            int maxH = (int) (screen.height * scale);

            double scaleX = (double) maxW / imgW;
            double scaleY = (double) maxH / imgH;
            double s = Math.min(1.0, Math.min(scaleX, scaleY));

            int newW = (int) (imgW * s);
            int newH = (int) (imgH * s);

            Image scaled = original.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}