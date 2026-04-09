package tests.ui;

import javax.swing.*;
import java.awt.*;

import features.components.UIButton;
import features.components.UIInput;
import config.UIConfig;

public class TestUI {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test UI");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        // Input
        UIInput username = new UIInput(15);

        // Button
        UIButton loginBtn = new UIButton(
            "Login",
            UIConfig.PRIMARY,
            UIConfig.BTN_PRIMARY,
            UIConfig.BTN_PRIMARY_FONT,
            UIConfig.RADIUS_PRIMARY,
            UIButton.ButtonType.PRIMARY
        );

        loginBtn.addActionListener(e -> {
            System.out.println("Username: " + username.getText());
        });

        panel.add(username);
        panel.add(loginBtn);

        frame.add(panel);
        frame.setVisible(true);
    }
}
