package features.layout.common.viewreport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for submitting a follow-up request on a complaint.
 */
public class FollowUpDialog extends JDialog {

    private final JTextArea txtNotes;
    private boolean submitted = false;

    public FollowUpDialog(Window owner, int complaintId) {
        super(owner, "Request Follow Up — Report #" + String.format("%03d", complaintId),
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 280);
        setLocationRelativeTo(owner);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setOpaque(true);
        content.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Request Follow Up");
        lblTitle.setFont(UIConstants.FONT_BOLD_16);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(8));

        JLabel lblDesc = new JLabel(
                "<html>This will notify the secretary/captain that you are<br>requesting a follow-up on this complaint.</html>");
        lblDesc.setFont(UIConstants.FONT_PLAIN_12);
        lblDesc.setForeground(UIConstants.C_TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblDesc);
        content.add(Box.createVerticalStrut(12));

        JLabel lblNotes = new JLabel("Additional Notes (optional)");
        lblNotes.setFont(UIConstants.FONT_BOLD_12);
        lblNotes.setForeground(UIConstants.C_TEXT_MUTED);
        lblNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblNotes);
        content.add(Box.createVerticalStrut(4));

        txtNotes = new JTextArea(4, 30);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setFont(UIConstants.FONT_PLAIN_13);
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane scroll = new JScrollPane(txtNotes);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        content.add(scroll);
        content.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));

        JButton btnCancel = ButtonFactory.createGhostButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = ButtonFactory.createPrimaryButton("Submit Request", new Color(245, 158, 11));
        btnSubmit.addActionListener(e -> {
            submitted = true;
            dispose();
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSubmit);
        content.add(btnRow);

        add(content);
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public String getNotes() {
        return txtNotes.getText().trim();
    }
}