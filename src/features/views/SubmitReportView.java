package features.views;

import app.E_Report;
import config.AppConfig;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.components.UIComboBox;
import features.components.UIInput;
import features.core.BackgroundPanel;
import features.submit.SubmitReportMapPanel;
import features.ui.DashboardFormUtils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.sql.Timestamp;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import models.ComplaintDetail;
import models.MapPinOutOfServiceAreaException;
import models.MissingReportFieldException;
import models.ReportSubmissionException;
import services.controller.ComplaintServiceController;

/**
 * SubmitReportView - Improved UX for Barangay E-Reporting System
 * Centered on Barangay Malacañang, Santa Rosa, Nueva Ecija, Philippines
 */
public class SubmitReportView extends JPanel {
    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private final UIComboBox<String> categoryCombo;
    private final UIComboBox<String> purokCombo;
    private final UIInput locationField;
    private final UIInput latitudeField;
    private final UIInput longitudeField;
    private final JTextArea detailsArea;
    private final JLabel selectedPhotoLabel;
    private final JLabel mapStatusLabel;
    private final JLabel formStatusLabel;
    private JButton submitBtn;
    private final SubmitReportMapPanel mapPanel;
    private File selectedFile;
    private boolean pinConfirmed;

    // Colors for improved UI
    private static final Color ACCENT_BLUE = new Color(33, 150, 243);
    private static final Color ACCENT_GREEN = new Color(76, 175, 80);
    private static final Color ACCENT_RED = new Color(244, 67, 54);
    private static final Color ACCENT_ORANGE = new Color(255, 152, 0);
    private static final Color BG_CARD = new Color(255, 255, 255, 235);
    private static final Color BG_INPUT = new Color(250, 250, 250);
    private static final Color BORDER_INPUT = new Color(224, 224, 224);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color TEXT_DARK = new Color(33, 33, 33);

    public SubmitReportView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // Initialize components with improved placeholders
        categoryCombo = new UIComboBox<>(AppConfig.COMPLAINT_TYPES);
        UIComboBox.applyPreset(categoryCombo, 200);
        categoryCombo.setSelectedIndex(0);

        purokCombo = new UIComboBox<>(AppConfig.REPORT_PUROK_OPTIONS);
        UIComboBox.applyPreset(purokCombo, 200);
        purokCombo.setSelectedIndex(0);

        locationField = new UIInput(10);
        latitudeField = new UIInput(10);
        longitudeField = new UIInput(10);
        detailsArea = new JTextArea(3, 22);
        selectedPhotoLabel = new JLabel("No photo selected");
        mapStatusLabel = new JLabel("Click on the map to pin your location");
        formStatusLabel = new JLabel(" ");
        formStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        // Style inputs
        locationField.setEditable(false);
        latitudeField.setEditable(false);
        longitudeField.setEditable(false);
        locationField.setBackground(new Color(245, 245, 245));
        latitudeField.setBackground(new Color(245, 245, 245));
        longitudeField.setBackground(new Color(245, 245, 245));
        locationField.setFont(new Font("Arial", Font.PLAIN, 12));
        latitudeField.setFont(new Font("Arial", Font.PLAIN, 12));
        longitudeField.setFont(new Font("Arial", Font.PLAIN, 12));

        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        detailsArea.setBackground(BG_INPUT);

        DashboardFormUtils.installPlaceholder(locationField, AppConfig.REPORT_LOCATION_PLACEHOLDER);

        // Initialize map centered on Barangay Malacañang
        mapPanel = new SubmitReportMapPanel(new SubmitReportMapPanel.Listener() {
            @Override
            public void onPinned(double latitude, double longitude) {
                latitudeField.setText(String.format("%.6f", latitude));
                longitudeField.setText(String.format("%.6f", longitude));
                mapStatusLabel.setText("Pin dropped! Click Confirm Pin to lock location.");
                mapStatusLabel.setForeground(ACCENT_ORANGE);
            }

            @Override
            public void onStatusChanged(String statusText) {
                mapStatusLabel.setText(statusText);
            }

            @Override
            public void onAddressResolved(String addressText) {
                if (addressText != null && !addressText.isBlank()) {
                    locationField.setForeground(TEXT_DARK);
                    locationField.setText(addressText);
                }
            }
        });

        add(createMainPanel(), BorderLayout.CENTER);
        initFieldListeners();
        updateSubmitButtonState();
    }

    private void styleInputField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        field.setBackground(BG_INPUT);
    }

    private JPanel createMainPanel() {
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();
        setNavMenus();

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(createContentPanel(), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(bgPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void setNavMenus() {
        String role = app.getUserSession().getRole();
        if (role.equalsIgnoreCase(AppConfig.ROLE_CAPTAIN)) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (role.equalsIgnoreCase(AppConfig.ROLE_SECRETARY)) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else {
            nav.setResidentMenus(route -> app.navigate(route));
        }
    }

    private JPanel createContentPanel() {
        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Left column - give it much more weight and enforce minimum size
        gbc.weightx = 0.5;
        gbc.ipadx = 10;
        JPanel leftColumn = createLeftColumn();
        card.add(leftColumn, gbc);

        // Right column (map) - constrain with less weight
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.ipadx = 0; // reset padding
        JPanel rightColumn = createConstrainedMapSection(); // use constrained version
        card.add(rightColumn, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createConstrainedMapSection() {
        JPanel mapSection = createMapSection();
        mapSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        return mapSection;
    }

    private JPanel createLeftColumn() {
        JPanel leftColumn = new JPanel(new GridBagLayout());
        leftColumn.setOpaque(false);
        leftColumn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        leftColumn.setMinimumSize(new Dimension(400, 0));
        leftColumn.setPreferredSize(new Dimension(550, 0)); // width hint, height 0 = stretch

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 14, 0);
        gbc.gridx = 0;
        leftColumn.add(createPageHeader(), gbc);

        // Category & Purok row
        gbc.gridy = 1;
        leftColumn.add(createCompactRow(
                createStyledLabeledField("Category *", categoryCombo, true),
                createStyledLabeledField("Purok *", purokCombo, true)), gbc);

        // Location field
        gbc.gridy = 2;
        leftColumn.add(createStyledLabeledField("Location / Street *", locationField, false), gbc);

        // Coordinates row
        gbc.gridy = 3;
        leftColumn.add(createCoordinatesRow(), gbc);

        // Description - expands vertically to fill remaining space
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        leftColumn.add(createDescriptionField(), gbc);

        // Photo upload - back to horizontal only
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        leftColumn.add(createPhotoUploadRow(), gbc);

        // Form status
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 8, 0);
        leftColumn.add(formStatusLabel, gbc);

        // Action buttons - anchor southeast, no vertical stretch
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        leftColumn.add(createActionButtons(), gbc);

        return leftColumn;
    }

    private Component createPageHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        // Ensure it stretches horizontally
        headerPanel.setPreferredSize(new Dimension(0, 60)); // 0 width = stretch

        JLabel title = new JLabel("Submit New Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Barangay Malacañang, Santa Rosa, Nueva Ecija");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        JPanel textPanel = new JPanel(new BorderLayout(0, 4));
        textPanel.setOpaque(false);
        textPanel.add(title, BorderLayout.NORTH);
        textPanel.add(subtitle, BorderLayout.CENTER);

        headerPanel.add(textPanel, BorderLayout.WEST);
        return headerPanel;
    }

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

        // Ensure field itself stretches
        if (field instanceof JComponent) {
            ((JComponent) field).setPreferredSize(null); // clear any fixed preferred size
        }

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setOpaque(false);
        fieldWrapper.add(field, BorderLayout.CENTER);
        container.add(fieldWrapper, BorderLayout.CENTER);

        return container;
    }

    private JPanel createCoordinatesRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 8);

        JPanel latPanel = createStyledLabeledField("Latitude", latitudeField, false);
        row.add(latPanel, gbc);

        gbc.insets = new Insets(0, 8, 0, 0);
        JPanel lonPanel = createStyledLabeledField("Longitude", longitudeField, false);
        row.add(lonPanel, gbc);

        return row;
    }

    private JPanel createDescriptionField() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);

        JLabel label = new JLabel("Description / Details *");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        wrapper.add(label, BorderLayout.NORTH);

        // Let textarea expand - rows determine minimum height, but it can grow
        detailsArea.setRows(4); // minimum visible rows
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_INPUT, 1, true));
        scrollPane.setBackground(BG_INPUT);
        // Ensure scrollpane expands in both directions
        scrollPane.setPreferredSize(new Dimension(0, 120)); // 0 width = let parent decide
        wrapper.add(scrollPane, BorderLayout.CENTER);

        // Character counter
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

        JButton uploadBtn = createStyledButton("Upload Photo", ACCENT_BLUE);
        uploadBtn.setPreferredSize(new Dimension(140, 40));
        uploadBtn.addActionListener(e -> choosePhoto());

        // CENTER component stretches, EAST is fixed size
        row.add(selectedPhotoLabel, BorderLayout.CENTER);
        row.add(uploadBtn, BorderLayout.EAST);

        return row;
    }

    private JPanel createActionButtons() {
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS)); // FIXED: pass 'actions', not 'this'
        actions.setOpaque(false);

        JButton cancelBtn = createStyledButton("Clear", new Color(158, 158, 158));
        submitBtn = createStyledButton("Submit Report", ACCENT_GREEN);
        submitBtn.setEnabled(false);
        submitBtn.setBackground(new Color(200, 200, 200));

        cancelBtn.addActionListener(e -> clearForm());
        submitBtn.addActionListener(e -> submitComplaint());

        // Add horizontal glue before buttons to push them right, or remove if you want
        // left-aligned
        actions.add(Box.createHorizontalGlue()); // pushes buttons to the right
        actions.add(cancelBtn);
        actions.add(Box.createHorizontalStrut(10)); // 10px gap between buttons
        actions.add(submitBtn);

        return actions;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        button.setPreferredSize(new Dimension(140, 42));
        return button;
    }

    private JPanel createMapSection() {
        JPanel mapSection = new JPanel(new BorderLayout(0, 12));
        mapSection.setOpaque(false);

        JLabel sectionTitle = new JLabel("Map Location");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(TEXT_DARK);
        mapSection.add(sectionTitle, BorderLayout.NORTH);

        JPanel mapWrapper = new JPanel(new BorderLayout(0, 10));
        mapWrapper.setOpaque(false);
        // REMOVED: mapPanel.setPreferredSize(new Dimension(0, 340));
        // Let the parent container control sizing
        mapPanel.setPreferredSize(new Dimension(300, 300)); // smaller, more reasonable default
        mapWrapper.add(mapPanel, BorderLayout.CENTER);

        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        controlsRow.setOpaque(false);

        JButton confirmPinBtn = createStyledButton("Confirm Pin", ACCENT_GREEN);
        JButton resetPinBtn = createStyledButton("Reset", new Color(96, 125, 139));
        confirmPinBtn.setPreferredSize(new Dimension(130, 36));
        resetPinBtn.setPreferredSize(new Dimension(100, 36));

        confirmPinBtn.addActionListener(e -> confirmMapPin());
        resetPinBtn.addActionListener(e -> {
            mapPanel.resetView();
            latitudeField.setText("");
            longitudeField.setText("");
            pinConfirmed = false;
            updateSubmitButtonState();
            mapStatusLabel.setText("Map reset. Click to drop a new pin.");
            mapStatusLabel.setForeground(TEXT_MUTED);
        });

        controlsRow.add(confirmPinBtn);
        controlsRow.add(resetPinBtn);
        mapWrapper.add(controlsRow, BorderLayout.SOUTH);
        mapSection.add(mapWrapper, BorderLayout.CENTER);

        JPanel instructions = new JPanel(new BorderLayout(0, 8));
        instructions.setOpaque(false);
        instructions.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        mapStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mapStatusLabel.setForeground(ACCENT_BLUE);
        instructions.add(mapStatusLabel, BorderLayout.NORTH);

        JTextArea hint = new JTextArea(
                "Tip: Click directly on the map to drop your pin. " +
                        "The blue circle shows the Barangay Malacañang service area. " +
                        "Pins outside this area cannot be submitted.");
        hint.setWrapStyleWord(true);
        hint.setLineWrap(true);
        hint.setEditable(false);
        hint.setOpaque(false);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        instructions.add(hint, BorderLayout.CENTER);

        mapSection.add(instructions, BorderLayout.SOUTH);
        return mapSection;
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Photo Evidence");
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (JPG, PNG)", "jpg", "jpeg", "png");
        chooser.setFileFilter(filter);

        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            selectedPhotoLabel.setText(truncateFileName(selectedFile.getName(), 20));
            selectedPhotoLabel.setForeground(ACCENT_GREEN);
            // FIXED: Force UI update after file selection
            updateSubmitButtonState();
            revalidate();
            repaint();
        }
    }

    private void submitComplaint() {
        try {
            ComplaintDetail complaint = buildComplaintFromForm();
            ComplaintServiceController service = new ComplaintServiceController();
            service.addComplaint(getCurrentUserId(), complaint, selectedFile);
            JOptionPane.showMessageDialog(this,
                    "Your report has been submitted successfully!\n\n" +
                            "Tracking: " + complaint.getSubject(),
                    "Report Submitted", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (MissingReportFieldException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Missing Information", JOptionPane.WARNING_MESSAGE);
        } catch (ReportSubmissionException ex) {
            String details = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            JOptionPane.showMessageDialog(this, "Submission failed: " + details, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Submission failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ComplaintDetail buildComplaintFromForm() {
        String category = getSelectedComboValue(categoryCombo, AppConfig.REPORT_CATEGORY_PLACEHOLDER);
        if (category.isEmpty()) {
            throw new MissingReportFieldException("category", "Please select a category for your report.");
        }

        String purok = getSelectedComboValue(purokCombo, AppConfig.REPORT_PUROK_PLACEHOLDER);
        if (purok.isEmpty()) {
            throw new MissingReportFieldException("purok", "Please select your purok.");
        }

        String location = locationField.getText().trim();
        if (location.isEmpty() || AppConfig.REPORT_LOCATION_PLACEHOLDER.equals(location)) {
            throw new MissingReportFieldException("location", "Please enter the location or street name.");
        }

        String details = detailsArea.getText().trim();
        if (details.isEmpty()) {
            throw new MissingReportFieldException("details", "Please provide a description of the incident.");
        }
        if (details.length() > 500) {
            throw new IllegalArgumentException("Description is too long. Maximum 500 characters allowed.");
        }

        // FIXED: Photo is now optional - removed mandatory check
        // if (selectedFile == null) {
        // throw new MissingReportFieldException("photo", "Please attach a photo as
        // evidence.");
        // }

        Double pinnedLat = mapPanel.getPinnedLatitude();
        Double pinnedLon = mapPanel.getPinnedLongitude();
        if (pinnedLat == null || pinnedLon == null || !pinConfirmed) {
            throw new MissingReportFieldException("mapPin", "Please drop and confirm a pin on the map.");
        }

        double distance = haversineDistanceMeters(
                pinnedLat, pinnedLon,
                AppConfig.REPORT_DEFAULT_MAP_LATITUDE,
                AppConfig.REPORT_DEFAULT_MAP_LONGITUDE);
        if (distance > AppConfig.REPORT_SERVICE_AREA_RADIUS_METERS) {
            throw new IllegalArgumentException(
                    "Selected location is outside the Barangay Malacañang service area (3km radius). " +
                            "Please select a location within the highlighted blue circle.");
        }

        String street = parseStreetFromLocation(location);

        ComplaintDetail complaint = new ComplaintDetail();
        complaint.setSubject(category + " - " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        complaint.setType(category);
        complaint.setPurok(purok);
        complaint.setStreet(street);
        complaint.setPersonsInvolved("");
        complaint.setDetails(details);
        complaint.setCurrentStatus("Pending");
        complaint.setDateTime(new Timestamp(System.currentTimeMillis()));
        complaint.setLatitude(pinnedLat);
        complaint.setLongitude(pinnedLon);
        return complaint;
    }

    private double haversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private String getSelectedComboValue(UIComboBox<String> combo, String placeholder) {
        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return "";
        }
        String value = selected.toString().trim();
        return placeholder.equals(value) ? "" : value;
    }

    private int getCurrentUserId() {
        if (app == null || app.getUserSession() == null) {
            throw new IllegalStateException("No active user session found. Please log in before submitting a report.");
        }
        return app.getUserSession().getUserId();
    }

    private String parseStreetFromLocation(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        String[] parts = location.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String street = parts[0];
        if (parts.length > 1) {
            String candidate = street + ", " + parts[1];
            if (candidate.length() <= 50) {
                street = candidate;
            }
        }
        if (street.length() > 50) {
            street = street.substring(0, 47).trim() + "...";
        }
        return street;
    }

    private void confirmMapPin() {
        Double lat = mapPanel.getPinnedLatitude();
        Double lon = mapPanel.getPinnedLongitude();
        if (lat == null || lon == null) {
            JOptionPane.showMessageDialog(this,
                    "Please click on the map to drop a pin first.",
                    "Map Confirmation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            validatePinWithinServiceArea(lat, lon);
            latitudeField.setText(String.format("%.6f", lat));
            longitudeField.setText(String.format("%.6f", lon));
            mapStatusLabel.setText("Location confirmed: " + String.format("%.6f, %.6f", lat, lon));
            mapStatusLabel.setForeground(ACCENT_GREEN);
            pinConfirmed = true;
            updateSubmitButtonState();
            JOptionPane.showMessageDialog(this,
                    "Location confirmed successfully!",
                    "Location Confirmed", JOptionPane.INFORMATION_MESSAGE);
        } catch (MapPinOutOfServiceAreaException ex) {
            mapStatusLabel.setText("Pin is outside the service area.");
            mapStatusLabel.setForeground(ACCENT_RED);
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Location Unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        categoryCombo.setSelectedIndex(0);
        purokCombo.setSelectedIndex(0);
        locationField.setText(AppConfig.REPORT_LOCATION_PLACEHOLDER);
        locationField.setForeground(TEXT_MUTED);
        detailsArea.setText("");
        latitudeField.setText("");
        longitudeField.setText("");
        selectedFile = null;
        selectedPhotoLabel.setText("No photo selected");
        selectedPhotoLabel.setForeground(TEXT_DARK);
        mapStatusLabel.setText("Click on the map to pin your location");
        mapStatusLabel.setForeground(ACCENT_BLUE);
        formStatusLabel.setText(" ");
        pinConfirmed = false;
        updateSubmitButtonState();
        mapPanel.resetView();
    }

    private String truncateFileName(String fileName, int maxLength) {
        if (fileName == null || fileName.length() <= maxLength) {
            return fileName;
        }
        int partLength = (maxLength - 3) / 2;
        return fileName.substring(0, partLength) + "..." + fileName.substring(fileName.length() - partLength);
    }

    private void validatePinWithinServiceArea(double lat, double lon) {
        double distance = haversineDistanceMeters(lat, lon,
                AppConfig.REPORT_DEFAULT_MAP_LATITUDE,
                AppConfig.REPORT_DEFAULT_MAP_LONGITUDE);
        if (distance > AppConfig.REPORT_SERVICE_AREA_RADIUS_METERS) {
            throw new MapPinOutOfServiceAreaException(
                    "Selected location is outside the Barangay Malacañang service area (3km radius). " +
                            "Please choose a location inside the highlighted blue circle.");
        }
    }

    private void initFieldListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }
        };

        locationField.getDocument().addDocumentListener(listener);
        detailsArea.getDocument().addDocumentListener(listener);
        categoryCombo.addActionListener(e -> updateSubmitButtonState());
        purokCombo.addActionListener(e -> updateSubmitButtonState());
    }

    private void updateSubmitButtonState() {
        boolean formReady = isFormReady();
        submitBtn.setEnabled(formReady);
        submitBtn.setBackground(formReady ? ACCENT_GREEN : new Color(200, 200, 200));
        submitBtn.setForeground(formReady ? Color.WHITE : new Color(120, 120, 120));

        if (formReady) {
            formStatusLabel.setText("Ready to submit");
            formStatusLabel.setForeground(ACCENT_GREEN);
        } else {
            formStatusLabel.setText("Complete all required fields to enable submit");
            formStatusLabel.setForeground(TEXT_MUTED);
        }
    }

    private boolean isFormReady() {
        if (!pinConfirmed)
            return false;
        // FIXED: Photo is now optional - removed check for selectedFile
        if (getSelectedComboValue(categoryCombo, AppConfig.REPORT_CATEGORY_PLACEHOLDER).isEmpty())
            return false;
        if (getSelectedComboValue(purokCombo, AppConfig.REPORT_PUROK_OPTIONS[0]).isEmpty())
            return false;
        String location = locationField.getText().trim();
        if (location.isEmpty() || AppConfig.REPORT_LOCATION_PLACEHOLDER.equals(location))
            return false;
        if (detailsArea.getText().trim().isEmpty())
            return false;
        return true;
    }

    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fillColor;

        public RoundedPanel(int radius, Color fillColor) {
            super();
            this.radius = radius;
            this.fillColor = fillColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, width - 1, height - 1, radius, radius);

            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public NavPanel getNavPanel() {
        return nav;
    }
}