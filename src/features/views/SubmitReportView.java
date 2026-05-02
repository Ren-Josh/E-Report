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
    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private ReportFormPanel formPanel;
    private ReportMapPanel mapPanel;

    private static final Color ACCENT_GREEN = new Color(76, 175, 80);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color BG_CARD = new Color(255, 255, 255, 235);

    public SubmitReportView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();
        setNavMenus();

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);

        // --- Content card ---
        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Left: form
        formPanel = new ReportFormPanel(app);
        formPanel.setFormChangeListener(this::updateSubmitButtonState);
        formPanel.getClearBtn().addActionListener(e -> clearForm());
        formPanel.getSubmitBtn().addActionListener(e -> submitComplaint());
        gbc.weightx = 0.5;
        gbc.ipadx = 10;
        card.add(formPanel, gbc);

        // Right: map
        mapPanel = new ReportMapPanel();
        mapPanel.setOnPinConfirmedChanged(this::updateSubmitButtonState);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.ipadx = 0;
        card.add(mapPanel, gbc);

        bgPanel.add(card, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(bgPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        // Bridge map events -> form fields
        mapPanel.setExternalListener(new SubmitReportMapPanel.Listener() {
            @Override
            public void onPinned(double latitude, double longitude) {
                formPanel.setLatitude(String.format("%.6f", latitude));
                formPanel.setLongitude(String.format("%.6f", longitude));
            }

            @Override
            public void onStatusChanged(String statusText) {
                /* map panel handles its own label */ }

            @Override
            public void onAddressResolved(String addressText) {
                formPanel.setLocationText(addressText);
            }
        });

        updateSubmitButtonState();
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

    private void updateSubmitButtonState() {
        boolean ready = formPanel.isFormReady(mapPanel.isPinConfirmed());
        formPanel.setSubmitEnabled(ready);
        if (ready) {
            formPanel.setFormStatus("Ready to submit", ACCENT_GREEN);
        } else {
            formPanel.setFormStatus("Complete all required fields to enable submit", TEXT_MUTED);
        }
    }

    private void clearForm() {
        formPanel.clearForm();
        mapPanel.resetMapAndPin();
        updateSubmitButtonState();
    }

    private void submitComplaint() {
        try {
            ComplaintDetail complaint = buildComplaintFromForm();
            ComplaintServiceController service = new ComplaintServiceController();
            service.addComplaint(getCurrentUserId(), complaint, formPanel.getSelectedFile());

            JOptionPane.showMessageDialog(this,
                    "Your report has been submitted successfully!\n\nTracking: " + complaint.getSubject(),
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
        String category = formPanel.getCategory();
        if (category.isEmpty() || category.startsWith("All ")) {
            throw new MissingReportFieldException("category", "Please select a category for your report.");
        }

        String purok = formPanel.getPurok();
        if (purok.isEmpty() || purok.startsWith("All ")) {
            throw new MissingReportFieldException("purok", "Please select the purok where it happens.");
        }

        String location = formPanel.getLocationText();
        if (location.isEmpty()) {
            throw new MissingReportFieldException("location", "Please select the location on the map.");
        }

        String details = formPanel.getDetails();
        if (details.isEmpty()) {
            throw new MissingReportFieldException("details", "Please provide a description of the incident.");
        }
        if (details.length() > 500) {
            throw new IllegalArgumentException("Description is too long. Maximum 500 characters allowed.");
        }

        Double pinnedLat = mapPanel.getPinnedLatitude();
        Double pinnedLon = mapPanel.getPinnedLongitude();
        if (pinnedLat == null || pinnedLon == null || !mapPanel.isPinConfirmed()) {
            throw new MissingReportFieldException("mapPin", "Please drop and confirm a pin on the map.");
        }

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

    private String parseStreetFromLocation(String location) {
        if (location == null || location.isBlank())
            return "";
        String[] parts = location.split(",");
        for (int i = 0; i < parts.length; i++)
            parts[i] = parts[i].trim();
        String street = parts[0];
        if (parts.length > 1) {
            String candidate = street + ", " + parts[1];
            if (candidate.length() <= 50)
                street = candidate;
        }
        if (street.length() > 50)
            street = street.substring(0, 47).trim() + "...";
        return street;
    }

    private int getCurrentUserId() {
        if (app == null || app.getUserSession() == null) {
            throw new IllegalStateException("No active user session found. Please log in before submitting a report.");
        }
        return app.getUserSession().getUserId();
    }

    public NavPanel getNavPanel() {
        return nav;
    }
}