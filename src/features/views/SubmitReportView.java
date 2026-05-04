package features.views;

import app.E_Report;
import config.AppConfig;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.layout.common.submit.ReportFormPanel;
import features.layout.common.submit.ReportMapPanel;
import features.layout.common.submit.RoundedPanel;
import features.submit.SubmitReportMapPanel;
import models.ComplaintDetail;
import models.MissingReportFieldException;
import models.ReportSubmissionException;
import services.controller.ComplaintServiceController;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;

/**
 * SubmitReportView - Coordinator for the report submission screen.
 * Panels live under features.layout.common.viewreport.
 * Backend uses the existing ComplaintServiceController + DAO layer.
 */
public class SubmitReportView extends JPanel {
    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /** Top header bar displaying user info and system branding. */
    private HeaderPanel header;
    /** Left-side navigation panel with role-specific menu items. */
    private NavPanel nav;
    /** Left panel containing the report form fields and controls. */
    private ReportFormPanel formPanel;
    /** Right panel containing the interactive map for location selection. */
    private ReportMapPanel mapPanel;

    /** Accent color used for the "ready to submit" status text. */
    private static final Color ACCENT_GREEN = new Color(76, 175, 80);
    /** Muted color used for the "incomplete" status text. */
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    /** Semi-transparent white for the content card background. */
    private static final Color BG_CARD = new Color(255, 255, 255, 235);

    /**
     * Constructs the report submission view and initializes all sub-components.
     * 
     * @param app the main E_Report application frame
     */
    public SubmitReportView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Assembles the full view: background, header, navigation, and the
     * two-column content card containing the form and map panels.
     * Wires event listeners between the map and form for bidirectional updates.
     */
    private void initComponents() {
        // Create the background panel with consistent gaps and padding.
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Instantiate shared chrome components.
        header = new HeaderPanel(app);
        nav = new NavPanel();
        setNavMenus();

        // Attach header and nav to the background.
        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);

        // Create the main content card with rounded corners and semi-transparent
        // background.
        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Left column: report form panel.
        formPanel = new ReportFormPanel(app);
        // Re-evaluate submit button whenever form fields change.
        formPanel.setFormChangeListener(this::updateSubmitButtonState);
        // Wire clear and submit actions.
        formPanel.getClearBtn().addActionListener(e -> clearForm());
        formPanel.getSubmitBtn().addActionListener(e -> submitComplaint());
        gbc.weightx = 0.5;
        gbc.ipadx = 10;
        card.add(formPanel, gbc);

        // Right column: map panel for location selection.
        mapPanel = new ReportMapPanel();
        // Re-evaluate submit button whenever the pin state changes.
        mapPanel.setOnPinConfirmedChanged(this::updateSubmitButtonState);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.ipadx = 0;
        card.add(mapPanel, gbc);

        bgPanel.add(card, BorderLayout.CENTER);

        // Wrap everything in a transparent panel to avoid layout artifacts.
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(bgPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        // Bridge map events to form fields so coordinates and address auto-populate.
        mapPanel.setExternalListener(new SubmitReportMapPanel.Listener() {
            @Override
            public void onPinned(double latitude, double longitude) {
                // Auto-fill latitude and longitude into the form when a pin is dropped.
                formPanel.setLatitude(String.format("%.6f", latitude));
                formPanel.setLongitude(String.format("%.6f", longitude));
            }

            @Override
            public void onStatusChanged(String statusText) {
                /* map panel handles its own label */ }

            @Override
            public void onAddressResolved(String addressText) {
                // Auto-fill the resolved street address into the form.
                formPanel.setLocationText(addressText);
            }
        });

        // Set initial button state based on current form and map readiness.
        updateSubmitButtonState();
    }

    /**
     * Configures the navigation menu based on the current user's role.
     */
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

    /**
     * Updates the submit button enabled state and status label based on
     * whether all required form fields are filled and the map pin is confirmed.
     */
    private void updateSubmitButtonState() {
        boolean ready = formPanel.isFormReady(mapPanel.isPinConfirmed());
        formPanel.setSubmitEnabled(ready);
        if (ready) {
            formPanel.setFormStatus("Ready to submit", ACCENT_GREEN);
        } else {
            formPanel.setFormStatus("Complete all required fields to enable submit", TEXT_MUTED);
        }
    }

    /**
     * Resets all form fields and map state to their initial empty values.
     */
    private void clearForm() {
        formPanel.clearForm();
        mapPanel.resetMapAndPin();
        updateSubmitButtonState();
    }

    /**
     * Gathers data from the form and map, validates all required fields,
     * builds a ComplaintDetail object, and submits it through the service layer.
     * Shows appropriate feedback dialogs for success or each failure type.
     */
    private void submitComplaint() {
        try {
            // Build the domain object from UI state with full validation.
            ComplaintDetail complaint = buildComplaintFromForm();
            // Submit through the service controller with optional file attachment.
            ComplaintServiceController service = new ComplaintServiceController();
            service.addComplaint(getCurrentUserId(), complaint, formPanel.getSelectedFile());

            // Show success feedback and reset the form for the next report.
            JOptionPane.showMessageDialog(this,
                    "Your report has been submitted successfully!\n\nTracking: " + complaint.getSubject(),
                    "Report Submitted", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (MissingReportFieldException ex) {
            // Specific missing field with a user-friendly message.
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Missing Information", JOptionPane.WARNING_MESSAGE);
        } catch (ReportSubmissionException ex) {
            // Database or service layer failure.
            String details = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            JOptionPane.showMessageDialog(this, "Submission failed: " + details, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            // Validation rule violation (e.g., description too long, out of bounds).
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            // Catch-all for unexpected errors.
            JOptionPane.showMessageDialog(this, "Submission failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Constructs a ComplaintDetail from the current form and map state.
     * Performs comprehensive validation on every required field and business rule.
     * 
     * @return a fully populated ComplaintDetail ready for persistence
     * @throws MissingReportFieldException if any required field is empty or invalid
     * @throws IllegalArgumentException    if validation rules are violated
     */
    private ComplaintDetail buildComplaintFromForm() {
        // Validate category: must be selected and not a placeholder.
        String category = formPanel.getCategory();
        if (category.isEmpty() || category.startsWith("All ")) {
            throw new MissingReportFieldException("category", "Please select a category for your report.");
        }

        // Validate purok: must be selected and not a placeholder.
        String purok = formPanel.getPurok();
        if (purok.isEmpty() || purok.startsWith("All ")) {
            throw new MissingReportFieldException("purok", "Please select the purok where it happens.");
        }

        // Validate location: must have a resolved address from the map.
        String location = formPanel.getLocationText();
        if (location.isEmpty()) {
            throw new MissingReportFieldException("location", "Please select the location on the map.");
        }

        // Validate details: required and within length limit.
        String details = formPanel.getDetails();
        if (details.isEmpty()) {
            throw new MissingReportFieldException("details", "Please provide a description of the incident.");
        }
        if (details.length() > 500) {
            throw new IllegalArgumentException("Description is too long. Maximum 500 characters allowed.");
        }

        // Validate map pin: must be dropped and explicitly confirmed by the user.
        Double pinnedLat = mapPanel.getPinnedLatitude();
        Double pinnedLon = mapPanel.getPinnedLongitude();
        if (pinnedLat == null || pinnedLon == null || !mapPanel.isPinConfirmed()) {
            throw new MissingReportFieldException("mapPin", "Please drop and confirm a pin on the map.");
        }

        // Validate service area: pin must fall within the configured radius.
        double distance = haversineDistanceMeters(
                pinnedLat, pinnedLon,
                AppConfig.REPORT_DEFAULT_MAP_LATITUDE,
                AppConfig.REPORT_DEFAULT_MAP_LONGITUDE);
        if (distance > AppConfig.REPORT_SERVICE_AREA_RADIUS_METERS) {
            throw new IllegalArgumentException(
                    "Selected location is outside the Barangay Malacañang service area ("
                            + (AppConfig.REPORT_SERVICE_AREA_RADIUS_METERS / 1000) + "km radius). " +
                            "Please select a location within the highlighted blue circle.");
        }

        // Extract the street portion from the full resolved address.
        String street = parseStreetFromLocation(location);

        // Build and populate the complaint domain object.
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

    /**
     * Calculates the great-circle distance between two lat/lon points using the
     * Haversine formula.
     * 
     * @param lat1 latitude of the first point in degrees
     * @param lon1 longitude of the first point in degrees
     * @param lat2 latitude of the second point in degrees
     * @param lon2 longitude of the second point in degrees
     * @return the distance in meters
     */
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

    /**
     * Extracts the street portion from a comma-separated location string.
     * Limits the result to 50 characters to fit database constraints.
     * 
     * @param location the full resolved address string
     * @return the truncated street portion
     */
    private String parseStreetFromLocation(String location) {
        if (location == null || location.isBlank())
            return "";
        // Split by comma and trim each component.
        String[] parts = location.split(",");
        for (int i = 0; i < parts.length; i++)
            parts[i] = parts[i].trim();
        String street = parts[0];
        // Include the second component if the combined length stays within limit.
        if (parts.length > 1) {
            String candidate = street + ", " + parts[1];
            if (candidate.length() <= 50)
                street = candidate;
        }
        // Hard truncate with ellipsis if still too long.
        if (street.length() > 50)
            street = street.substring(0, 47).trim() + "...";
        return street;
    }

    /**
     * Retrieves the current user's ID from the active session.
     * 
     * @return the authenticated user's ID
     * @throws IllegalStateException if no active session exists
     */
    private int getCurrentUserId() {
        if (app == null || app.getUserSession() == null) {
            throw new IllegalStateException("No active user session found. Please log in before submitting a report.");
        }
        return app.getUserSession().getUserId();
    }

    /** @return the navigation panel instance */
    public NavPanel getNavPanel() {
        return nav;
    }
}