package features.layout.common.viewreport;

import models.ComplaintDetail;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ComplaintDetailPanel extends JPanel {

    private final JTextField txtTitle;
    private final JTextArea txtDescription;
    private final JTextField txtLocation;
    private final JTextField txtPurok;
    private final JTextField txtCoords;
    private final JTextField txtDateSubmitted;
    private final JTextField txtLastUpdate;
    private final AttachmentViewerPanel attachmentViewer;

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

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        txtTitle = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Title", txtTitle), gbc);

        gbc.gridy = 1;
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

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtLocation = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Location", txtLocation), gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.gridx = 0;
        txtPurok = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Purok", txtPurok), gbc);
        gbc.gridx = 2;
        txtCoords = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Coordinates", txtCoords), gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        txtDateSubmitted = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Date Submitted", txtDateSubmitted), gbc);
        gbc.gridx = 2;
        txtLastUpdate = FieldFactory.createReadOnlyField();
        form.add(FieldFactory.createFieldRow("Last Update", txtLastUpdate), gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        attachmentViewer = new AttachmentViewerPanel();
        form.add(FieldFactory.createFieldRow("Attachments", attachmentViewer), gbc);

        add(form, BorderLayout.CENTER);
    }

    public void loadComplaint(ComplaintDetail cd) {
        if (cd == null)
            return;
        txtTitle.setText(safe(cd.getSubject()));
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

    public void setLastUpdate(String lastUpdate) {
        txtLastUpdate.setText(lastUpdate != null ? lastUpdate : "—");
    }

    private String safe(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }
}