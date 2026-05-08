package features.layout.common.viewreport;

import models.ComplaintDetail;
import models.UserInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ComplaintDetailPanel extends JPanel {

    private JLabel lblComplainantName;
    private JLabel lblRoleBadge;
    private JLabel lblComplainantMeta;

    private final JTextField txtTitle;
    private final JTextArea txtDescription;
    private final JTextField txtLocation;
    private final JTextField txtPurok;
    private final JTextField txtCoords;
    private final JTextField txtDateSubmitted;
    private final JTextField txtLastUpdate;
    private final AttachmentViewerPanel attachmentViewer;

    private static final Color TEXT_DARK = new Color(33, 33, 33);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);

    // Role badge colors
    private static final Color RESIDENT_BG = new Color(232, 245, 233);
    private static final Color RESIDENT_TEXT = new Color(46, 125, 50);
    private static final Color SECRETARY_BG = new Color(227, 242, 253);
    private static final Color SECRETARY_TEXT = new Color(21, 101, 192);
    private static final Color CAPTAIN_BG = new Color(255, 243, 224);
    private static final Color CAPTAIN_TEXT = new Color(230, 81, 0);

    public ComplaintDetailPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);
        setBackground(UIConstants.C_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 12);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;

        int row = 0;

        // ========== COMPLAINANT HEADER ==========
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 16, 0);
        form.add(buildComplainantHeader(), gbc);

        row++;
        // --- Title ---
        gbc.gridy = row;
        gbc.insets = new Insets(6, 0, 6, 12);
        txtTitle = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Title", txtTitle), gbc);

        row++;
        // --- Description ---
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFont(UIConstants.FONT_PLAIN_13);
        txtDescription.setBackground(UIConstants.C_BG_FIELD);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane descScroll = FieldFactory.createNonScrollingScrollPane(txtDescription);
        form.add(FieldFactory.createFieldRow("Description", descScroll), gbc);

        row++;
        // --- Location ---
        gbc.gridy = row;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtLocation = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Location", txtLocation), gbc);

        row++;
        // --- Purok & Coordinates ---
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.gridx = 0;
        txtPurok = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Purok", txtPurok), gbc);
        gbc.gridx = 2;
        txtCoords = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Coordinates", txtCoords), gbc);

        row++;
        // --- Date Submitted & Last Update ---
        gbc.gridy = row;
        gbc.gridx = 0;
        txtDateSubmitted = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Date Submitted", txtDateSubmitted), gbc);
        gbc.gridx = 2;
        txtLastUpdate = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Last Update", txtLastUpdate), gbc);

        row++;
        // --- Attachments ---
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        attachmentViewer = new AttachmentViewerPanel();
        form.add(FieldFactory.createFieldRow("Attachments", attachmentViewer), gbc);

        add(form, BorderLayout.CENTER);
    }

    private JPanel buildComplainantHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Name + Badge row ---
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblComplainantName = new JLabel("—");
        lblComplainantName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblComplainantName.setForeground(TEXT_DARK);
        nameRow.add(lblComplainantName);

        lblRoleBadge = new JLabel("Resident");
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameRow.add(Box.createHorizontalStrut(10));
        nameRow.add(lblRoleBadge);

        header.add(nameRow);

        // --- Meta row ---
        lblComplainantMeta = new JLabel("—");
        lblComplainantMeta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblComplainantMeta.setForeground(TEXT_MUTED);
        lblComplainantMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblComplainantMeta.setBorder(new EmptyBorder(4, 0, 0, 0));
        header.add(lblComplainantMeta);

        return header;
    }

    public void loadComplaint(ComplaintDetail cd) {
        if (cd == null)
            return;

        txtTitle.setText(safe(cd.getSubject()));
        txtLocation.setText(safe(cd.getStreet()));
        txtDescription.setText(safe(cd.getDetails()));
        txtPurok.setText(safe(cd.getPurok()));
        txtCoords.setText(String.format("%.6f, %.6f", cd.getLatitude(), cd.getLongitude()));
        txtDateSubmitted.setText(cd.getDateTime() != null ? cd.getDateTime().toString() : "N/A");

        byte[] photo = cd.getPhotoAttachmentBytes();
        if (photo != null && photo.length > 0) {
            attachmentViewer.setAttachment(photo, cd.getPhotoName());
        } else {
            attachmentViewer.clearAttachment();
        }
    }

    public void loadUserInfo(UserInfo ui, String role) {
        if (ui == null) {
            lblComplainantName.setText("—");
            lblComplainantMeta.setText("—");
            setRoleBadge("Resident");
            return;
        }

        String fullName = (safe(ui.getFName()) + " " + safe(ui.getMName()) + " " + safe(ui.getLName()))
                .replaceAll("  ", " ").trim();
        if (fullName.isEmpty() || fullName.equals("— — —"))
            fullName = "—";

        lblComplainantName.setText(fullName);

        // Set role badge with color
        String displayRole = role != null && !role.isBlank() ? role : "Resident";
        setRoleBadge(displayRole);

        // Build meta line
        String houseNum = safe(ui.getHouseNum());
        String purok = safe(ui.getPurok());
        String phone = safe(ui.getContact());
        String email = safe(ui.getEmail());

        StringBuilder meta = new StringBuilder();
        if (!purok.equals("—")) {
            meta.append(houseNum + ", " + purok);
        }
        if (!phone.equals("—")) {
            if (meta.length() > 0)
                meta.append("  |  ");
            meta.append(phone);
        }
        if (!email.equals("—")) {
            if (meta.length() > 0)
                meta.append("  |  ");
            meta.append(email);
        }

        lblComplainantMeta.setText(meta.length() > 0 ? meta.toString() : "—");
    }

    private void setRoleBadge(String role) {
        lblRoleBadge.setText(role);

        switch (role.toLowerCase()) {
            case "secretary":
                lblRoleBadge.setForeground(SECRETARY_TEXT);
                lblRoleBadge.setBackground(SECRETARY_BG);
                lblRoleBadge.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SECRETARY_TEXT, 1, true),
                        new EmptyBorder(2, 8, 2, 8)));
                break;
            case "captain":
                lblRoleBadge.setForeground(CAPTAIN_TEXT);
                lblRoleBadge.setBackground(CAPTAIN_BG);
                lblRoleBadge.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CAPTAIN_TEXT, 1, true),
                        new EmptyBorder(2, 8, 2, 8)));
                break;
            default: // resident
                lblRoleBadge.setForeground(RESIDENT_TEXT);
                lblRoleBadge.setBackground(RESIDENT_BG);
                lblRoleBadge.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(RESIDENT_TEXT, 1, true),
                        new EmptyBorder(2, 8, 2, 8)));
                break;
        }
        lblRoleBadge.setOpaque(true);
    }

    public void setLastUpdate(String lastUpdate) {
        txtLastUpdate.setText(lastUpdate != null ? lastUpdate : "—");
    }

    private String safe(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }
}