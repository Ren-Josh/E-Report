package features.core.dashboardpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

/**
 * DashboardInfoCardsPanel - A glassmorphism-style statistics card panel
 * Displays 4 metric cards (Total Reports, Pending, In Progress, Resolved)
 * with colored backgrounds and icons.
 */
public class DashboardInfoCardsPanel extends JPanel {

    // ============================================================
    // CONSTANTS
    // ============================================================

    /** Minimum width for each individual card in pixels */
    private static final int MIN_CARD_WIDTH = 230;

    /** Fixed height for each individual card in pixels */
    private static final int CARD_HEIGHT = 100;

    // ============================================================
    // INSTANCE VARIABLES
    // ============================================================

    /** Array of card data objects containing title, value, color, and icon path */
    private CardInfo[] cards;

    /** References to card panel components for layout management */
    private JPanel[] cardPanels;

    /** References to value labels for dynamic updates without recreation */
    private JLabel[] valueLabels;

    /** Corner radius for rounded rectangle card backgrounds (default: 16px) */
    private int cornerRadius = 16;

    // ============================================================
    // INNER CLASSES
    // ============================================================

    /**
     * CardInfo - Data holder for individual card properties
     */
    public static class CardInfo {
        public String title;
        public int value;
        public Color backgroundColor;
        public String iconPath;

        public CardInfo(String title, int value, Color backgroundColor, String iconPath) {
            this.title = title;
            this.value = value;
            this.backgroundColor = backgroundColor;
            this.iconPath = iconPath;
        }
    }

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    /**
     * Constructor with custom CardInfo array
     * 
     * @param cards Array of CardInfo objects defining card properties
     */
    public DashboardInfoCardsPanel(CardInfo[] cards) {
        this.cards = cards;
        this.cardPanels = new JPanel[cards.length];
        this.valueLabels = new JLabel[cards.length];
        initialize();
    }

    /**
     * Constructor with default 4-card layout (Total, Pending, In Progress,
     * Resolved)
     * 
     * @param totalReports Initial value for Total Reports card
     * @param pending      Initial value for Pending card
     * @param inProgress   Initial value for In Progress card
     * @param resolved     Initial value for Resolved card
     * @param iconPaths    Array of 4 icon paths (must match card order)
     */
    public DashboardInfoCardsPanel(int totalReports, int pending, int inProgress, int resolved, String[] iconPaths) {
        this.cards = new CardInfo[] {
                new CardInfo("Total Reports", totalReports, new Color(66, 133, 244), iconPaths[0]),
                new CardInfo("Pending", pending, new Color(251, 188, 5), iconPaths[1]),
                new CardInfo("In Progress", inProgress, new Color(171, 71, 188), iconPaths[2]),
                new CardInfo("Resolved", resolved, new Color(52, 168, 83), iconPaths[3])
        };
        this.cardPanels = new JPanel[this.cards.length];
        this.valueLabels = new JLabel[this.cards.length];
        initialize();
    }

    // ============================================================
    // PRIVATE INITIALIZATION METHODS
    // ============================================================

    /**
     * Initializes panel layout and creates all cards
     * No return value (void)
     */
    private void initialize() {
        // Layout configuration
        setLayout(new GridLayout(1, cards.length, 15, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Calculate total preferred width based on card count and gaps
        int totalWidth = cards.length * MIN_CARD_WIDTH + (cards.length - 1) * 15;
        setPreferredSize(new Dimension(totalWidth, CARD_HEIGHT + 20));

        // Create and store each card
        for (int i = 0; i < cards.length; i++) {
            JPanel card = createCard(cards[i], i);
            cardPanels[i] = card;
            add(card);
        }
    }

    /**
     * Creates a single glassmorphism card panel
     * 
     * @param card  CardInfo data object
     * @param index Card index for value label reference storage
     * @return Configured JPanel with rounded background, icon, and text
     */
    private JPanel createCard(CardInfo card, int index) {
        // Local variables for card component construction
        JPanel cardPanel;
        JLabel iconLabel;
        JPanel textPanel;
        JLabel titleLabel;
        JLabel valueLabel;

        // Card panel with custom paintComponent for rounded glass effect
        cardPanel = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(card.backgroundColor);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                // Return dynamic width based on parent, but minimum of MIN_CARD_WIDTH
                Container parent = getParent();
                if (parent != null && parent.getWidth() > 0) {
                    int availableWidth = parent.getWidth() - (cards.length - 1) * 15; // subtract gaps
                    int cardWidth = availableWidth / cards.length;
                    return new Dimension(Math.max(cardWidth, MIN_CARD_WIDTH), CARD_HEIGHT);
                }
                return new Dimension(MIN_CARD_WIDTH, CARD_HEIGHT);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(MIN_CARD_WIDTH, CARD_HEIGHT);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Icon configuration
        iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(40, 40));

        if (card.iconPath != null && !card.iconPath.isEmpty()) {
            try {
                URL iconUrl = getClass().getResource(card.iconPath);
                BufferedImage icon;
                if (iconUrl != null) {
                    icon = ImageIO.read(iconUrl);
                } else {
                    icon = ImageIO.read(new java.io.File(card.iconPath));
                }
                if (icon != null) {
                    Image scaled = icon.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    iconLabel.setIcon(tintIcon(new ImageIcon(scaled), Color.WHITE));
                }
            } catch (IOException e) {
                iconLabel.setText("📋");
                iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            }
        }

        iconLabel.setForeground(Color.WHITE);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Text content panel
        textPanel = new JPanel(new GridLayout(2, 1, 0, -5));
        textPanel.setOpaque(false);

        titleLabel = new JLabel(card.title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        titleLabel.setForeground(new Color(255, 255, 255, 220));

        valueLabel = new JLabel(String.valueOf(card.value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);

        // Store reference for dynamic updates
        valueLabels[index] = valueLabel;

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        cardPanel.add(iconLabel, BorderLayout.WEST);
        cardPanel.add(textPanel, BorderLayout.CENTER);

        return cardPanel;
    }

    // ============================================================
    // PRIVATE UTILITY METHODS
    // ============================================================

    /**
     * Applies white color tint to an icon using SRC_ATOP composite
     * 
     * @param icon  Source ImageIcon to tint
     * @param color Tint color to apply
     * @return New ImageIcon with tint applied, or null if input is null
     */
    private ImageIcon tintIcon(ImageIcon icon, Color color) {
        if (icon == null) {
            return null;
        }

        int w = icon.getIconWidth();
        int h = icon.getIconHeight();

        // Validate dimensions before processing
        if (w <= 0 || h <= 0) {
            return icon;
        }

        BufferedImage tinted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tinted.createGraphics();

        // Enable anti-aliasing for smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw original image
        g2.drawImage(icon.getImage(), 0, 0, null);

        // Apply color tint using SRC_ATOP composite (only tints existing pixels)
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.setColor(color);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
        return new ImageIcon(tinted);
    }

    // ============================================================
    // PUBLIC UPDATE METHODS
    // ============================================================

    /**
     * Updates the value of a specific card by index
     * 
     * @param cardIndex Index of the card (0=Total Reports, 1=Pending, 2=In
     *                  Progress, 3=Resolved)
     * @param newValue  The new value to display
     * @return void
     */
    public void updateCardValue(int cardIndex, int newValue) {
        if (cardIndex >= 0 && cardIndex < cards.length) {
            cards[cardIndex].value = newValue;
            if (valueLabels[cardIndex] != null) {
                valueLabels[cardIndex].setText(String.valueOf(newValue));
                valueLabels[cardIndex].revalidate();
                valueLabels[cardIndex].repaint();
            }
        }
    }

    /**
     * Updates all card values at once
     * 
     * @param total      Total Reports value
     * @param pending    Pending value
     * @param inProgress In Progress value
     * @param resolved   Resolved value
     * @return void
     */
    public void updateValues(int total, int pending, int inProgress, int resolved) {
        if (cards.length >= 4) {
            updateCardValue(0, total);
            updateCardValue(1, pending);
            updateCardValue(2, inProgress);
            updateCardValue(3, resolved);
        }
    }

    // ============================================================
    // PUBLIC GETTER METHODS
    // ============================================================

    /**
     * Gets the current value of a specific card
     * 
     * @param cardIndex Index of the card
     * @return Current value, or -1 if index is invalid
     */
    public int getCardValue(int cardIndex) {
        if (cardIndex >= 0 && cardIndex < cards.length) {
            return cards[cardIndex].value;
        }
        return -1;
    }

    /**
     * Gets the title of a specific card
     * 
     * @param cardIndex Index of the card
     * @return Card title, or null if index is invalid
     */
    public String getCardTitle(int cardIndex) {
        if (cardIndex >= 0 && cardIndex < cards.length) {
            return cards[cardIndex].title;
        }
        return null;
    }
}