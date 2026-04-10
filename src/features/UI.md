1. LOGO
   Element Size
   Logo Dimension 280 × 280 px

2. TEXT HIERARCHY (FONT SYSTEM)
    | Category               | Usage                  | Font      | Size        | Weight |
    | ---------------------- | ---------------------- | --------- | ----------- | ------ |
    | **H1 (Main Title)**    | Page titles            | SansSerif | **56px**    | Bold   |
    | **H2 (Section Title)** | Sub sections           | SansSerif | **32px**    | Bold   |
    | **H3 (Card Title)**    | Minor titles           | SansSerif | **24px**    | Bold   |
    | **Body Large**         | Subtitle / description | SansSerif | **22px**    | Plain  |
    | **Body Normal**        | Regular text           | SansSerif | **16px**    | Plain  |
    | **Caption**            | Small labels           | SansSerif | **12–14px** | Plain  |

   e.g.
   Title (E-Reporting System) - 56px
   Subtitle - 22px

3. BUTTON SYSTEM
   | Type               | Width     | Height   | Radius   |
   | ------------------ | --------- | -------- | -------- |
   | Primary Button     | 220px     | 65px     | 45px     | .
   | Secondary Button   | 180px     | 55px     | 35px     |
   | Small Button       | 140px     | 45px     | 25px     |

   | Button Type | Font Size | Weight |
   | ----------- | --------- | ------ |
   | Primary     | 22px      | Bold   | .
   | Secondary   | 18px      | Bold   |
   | Small       | 16px      | Plain  |

4. SPACING
   | Type | Size    |
   | ---- | ------- |
   | XS   | 5px     |
   | SM   | 10px    |
   | MD   | 20px    |
   | LG   | 40px    |
   | XL   | 50–60px |

========

Button Presets
1. Primary Button
   UIButton btn = new UIButton(
    "Login",
    UIConfig.PRIMARY,
    UIConfig.BTN_PRIMARY,
    UIConfig.BTN_PRIMARY_FONT,
    UIConfig.RADIUS_PRIMARY,
    UIButton.ButtonType.PRIMARY
   );

2. Outlined Button
   UIButton btn = new UIButton(
    "Cancel",
    UIConfig.PRIMARY,
    UIConfig.BTN_SECONDARY,
    UIConfig.BTN_SECONDARY_FONT,
    UIConfig.RADIUS_SECONDARY,
    UIButton.ButtonType.OUTLINED
   );

3. Disabled Button
   UIButton btn = new UIButton(
    "Submit",
    UIConfig.PRIMARY,
    UIConfig.BTN_PRIMARY,
    UIConfig.BTN_PRIMARY_FONT,
    UIConfig.RADIUS_PRIMARY,
    UIButton.ButtonType.DISABLED
   );
   btn.setEnabled(false);

4. Elevated Button
   UIButton btn = new UIButton(
    "Proceed",
    UIConfig.PRIMARY,
    UIConfig.BTN_PRIMARY,
    UIConfig.BTN_PRIMARY_FONT,
    UIConfig.RADIUS_PRIMARY,
    UIButton.ButtonType.ELEVATED
   );


class RoundedButton extends JButton {
        private Color bgColor;

        public RoundedButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setPreferredSize(new Dimension(220, 65));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 22));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 45, 45));
            super.paintComponent(g); // Call super last to draw text over the shape
            g2.dispose();
        }
    }