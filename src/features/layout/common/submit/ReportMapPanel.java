package features.layout.common.submit;

import config.AppConfig;
import features.submit.SubmitReportMapPanel;
import models.MapPinOutOfServiceAreaException;

import javax.swing.*;
import java.awt.*;

/**
 * Right-column panel containing the map, confirm/reset controls,
 * status label, and service-area hint.
 */
public class ReportMapPanel extends JPanel {
    private final SubmitReportMapPanel mapPanel;
    private final JLabel mapStatusLabel;
    private boolean pinConfirmed = false;

    private static final Color ACCENT_GREEN = new Color(76, 175, 80);
    private static final Color ACCENT_RED = new Color(244, 67, 54);
    private static final Color ACCENT_BLUE = new Color(33, 150, 243);
    private static final Color ACCENT_ORANGE = new Color(255, 152, 0);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private SubmitReportMapPanel.Listener externalListener;
    private Runnable onPinConfirmedChanged;

    public ReportMapPanel() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        JLabel sectionTitle = new JLabel("Map Location");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(TEXT_DARK);
        add(sectionTitle, BorderLayout.NORTH);

        // Map wrapper
        JPanel mapWrapper = new JPanel(new BorderLayout(0, 10));
        mapWrapper.setOpaque(false);

        mapPanel = new SubmitReportMapPanel(new SubmitReportMapPanel.Listener() {
            @Override
            public void onPinned(double latitude, double longitude) {
                mapStatusLabel.setText("Pin dropped! Click Confirm Pin to lock location.");
                mapStatusLabel.setForeground(ACCENT_ORANGE);
                if (externalListener != null)
                    externalListener.onPinned(latitude, longitude);
            }

            @Override
            public void onStatusChanged(String statusText) {
                mapStatusLabel.setText(statusText);
                if (externalListener != null)
                    externalListener.onStatusChanged(statusText);
            }

            @Override
            public void onAddressResolved(String addressText) {
                if (externalListener != null)
                    externalListener.onAddressResolved(addressText);
            }
        });

        mapPanel.setPreferredSize(new Dimension(300, 300));
        mapWrapper.add(mapPanel, BorderLayout.CENTER);

        // Controls
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        controlsRow.setOpaque(false);

        JButton confirmBtn = createStyledButton("Confirm Pin", ACCENT_GREEN);
        JButton resetBtn = createStyledButton("Reset", new Color(96, 125, 139));
        confirmBtn.setPreferredSize(new Dimension(130, 36));
        resetBtn.setPreferredSize(new Dimension(100, 36));

        confirmBtn.addActionListener(e -> confirmPin());
        resetBtn.addActionListener(e -> resetMap());

        controlsRow.add(confirmBtn);
        controlsRow.add(resetBtn);
        mapWrapper.add(controlsRow, BorderLayout.SOUTH);
        add(mapWrapper, BorderLayout.CENTER);

        // Instructions
        JPanel instructions = new JPanel(new BorderLayout(0, 8));
        instructions.setOpaque(false);
        instructions.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        mapStatusLabel = new JLabel("Click on the map to pin your location");
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

        add(instructions, BorderLayout.SOUTH);
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
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        return b;
    }

    private void confirmPin() {
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
            pinConfirmed = true;
            mapStatusLabel.setText("Location confirmed: " + String.format("%.6f, %.6f", lat, lon));
            mapStatusLabel.setForeground(ACCENT_GREEN);
            JOptionPane.showMessageDialog(this,
                    "Location confirmed successfully!",
                    "Location Confirmed", JOptionPane.INFORMATION_MESSAGE);
            if (onPinConfirmedChanged != null)
                onPinConfirmedChanged.run();
        } catch (MapPinOutOfServiceAreaException ex) {
            mapStatusLabel.setText("Pin is outside the service area.");
            mapStatusLabel.setForeground(ACCENT_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Location Unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetMap() {
        mapPanel.resetView();
        pinConfirmed = false;
        mapStatusLabel.setText("Map reset. Click to drop a new pin.");
        mapStatusLabel.setForeground(TEXT_MUTED);
        if (onPinConfirmedChanged != null)
            onPinConfirmedChanged.run();
    }

    private void validatePinWithinServiceArea(double lat, double lon) {
        double distance = haversineDistanceMeters(lat, lon,
                AppConfig.REPORT_DEFAULT_MAP_LATITUDE,
                AppConfig.REPORT_DEFAULT_MAP_LONGITUDE);
        if (distance > AppConfig.REPORT_SERVICE_AREA_RADIUS_METERS) {
            throw new MapPinOutOfServiceAreaException(
                    "Selected location is outside the Barangay Malacañang service area ("
                            + (AppConfig.REPORT_SERVICE_AREA_RADIUS_METERS / 1000) + "km radius). " +
                            "Please choose a location inside the highlighted blue circle.");
        }
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

    /* ================== Public API ================== */

    public Double getPinnedLatitude() {
        return mapPanel.getPinnedLatitude();
    }

    public Double getPinnedLongitude() {
        return mapPanel.getPinnedLongitude();
    }

    public boolean isPinConfirmed() {
        return pinConfirmed;
    }

    public void resetMapAndPin() {
        resetMap();
    }

    public void setMapStatus(String text, Color color) {
        mapStatusLabel.setText(text);
        mapStatusLabel.setForeground(color);
    }

    public SubmitReportMapPanel getMapPanel() {
        return mapPanel;
    }

    public void setExternalListener(SubmitReportMapPanel.Listener listener) {
        this.externalListener = listener;
    }

    public void setOnPinConfirmedChanged(Runnable listener) {
        this.onPinConfirmedChanged = listener;
    }
}