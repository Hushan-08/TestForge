package com.testforge.ui;

import com.testforge.model.User;
import com.testforge.service.AuthService;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.ModernCard;
import com.testforge.ui.components.PillInputField;
import com.testforge.ui.components.UITheme;
import com.testforge.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * Modern, centered login interface matching contemporary pill-input design:
 * - Brand logo, title, and tagline at the top-center
 * - Clean centered card dialog
 * - Pill-shaped input fields with embedded vector icons (envelope & lock)
 * - In-field floating placeholder text (no clunky top labels)
 * - Interactive show/hide password eye toggle
 * - "Forgot password?" right-aligned helper link
 * - Sleek pill "Get Started" action button
 * - Dotted divider with "Or sign in with"
 * - 1-Click Demo accounts for viva demonstration
 */
public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    private PillInputField identifierInput;
    private PillInputField passwordInput;

    public LoginPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        // Main canvas with centered radial glow
        JPanel centerCanvas = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Centered subtle blue ambient halo behind the login card
                float radius = Math.max(w, h) * 0.55f;
                RadialGradientPaint p = new RadialGradientPaint(
                        w * 0.5f, h * 0.45f, radius,
                        new float[]{0.0f, 0.45f, 1.0f},
                        new Color[]{
                                new Color(0x25, 0x63, 0xEB, 26),
                                new Color(0x0A, 0x0F, 0x1D, 240),
                                UITheme.BG_DARK
                        }
                );
                g2.setPaint(p);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        centerCanvas.setOpaque(true);
        centerCanvas.setBackground(UITheme.BG_DARK);

        // Center vertical stack
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // 1. TOP-CENTER BRAND HEADER (Logo, Name, Tagline)
        JPanel brandHeader = createBrandHeader();
        brandHeader.setAlignmentX(CENTER_ALIGNMENT);

        // 2. CENTER LOGIN CARD DIALOG
        ModernCard loginCard = createLoginCard();
        loginCard.setAlignmentX(CENTER_ALIGNMENT);

        centerContainer.add(brandHeader);
        centerContainer.add(Box.createVerticalStrut(22));
        centerContainer.add(loginCard);

        // Perfectly centered via GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 20, 30, 20);

        centerCanvas.add(centerContainer, gbc);

        // Scrollpane wrapper for responsive scaling
        JScrollPane scrollPane = new JScrollPane(centerCanvas);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Top-centered brand presentation.
     */
    private JPanel createBrandHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(CENTER_ALIGNMENT);

        JLabel brandIcon = new JLabel("⚡");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        brandIcon.setForeground(UITheme.ACCENT_CYAN);

        JLabel brandTitle = new JLabel("TestForge");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        brandTitle.setForeground(UITheme.TEXT_PRIMARY);

        titleRow.add(brandIcon);
        titleRow.add(brandTitle);

        JLabel brandTagline = new JLabel("“Forge your future.”", SwingConstants.CENTER);
        brandTagline.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        brandTagline.setForeground(UITheme.ACCENT_CYAN);
        brandTagline.setAlignmentX(CENTER_ALIGNMENT);

        header.add(titleRow);
        header.add(Box.createVerticalStrut(4));
        header.add(brandTagline);

        return header;
    }

    /**
     * Modern card containing pill-shaped input fields matching user's design reference.
     */
    private ModernCard createLoginCard() {
        ModernCard card = new ModernCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(440, 520));
        card.setMaximumSize(new Dimension(440, 520));
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        card.setTopAccentColor(UITheme.ACCENT_CYAN);

        // Header (Centered inside card)
        JLabel welcomeTitle = new JLabel("Sign In", SwingConstants.CENTER);
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeTitle.setForeground(UITheme.TEXT_PRIMARY);
        welcomeTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel welcomeSub = new JLabel("Enter your credentials to continue.", SwingConstants.CENTER);
        welcomeSub.setFont(UITheme.FONT_BODY);
        welcomeSub.setForeground(UITheme.TEXT_SECONDARY);
        welcomeSub.setAlignmentX(CENTER_ALIGNMENT);

        // Input 1: Pill Input Field for Email / Username (with Envelope Vector Icon)
        identifierInput = new PillInputField(
                PillInputField.IconType.EMAIL,
                "Email or Username",
                false
        );
        identifierInput.setAlignmentX(CENTER_ALIGNMENT);

        // Input 2: Pill Input Field for Password (with Padlock Vector Icon and Eye Toggle)
        passwordInput = new PillInputField(
                PillInputField.IconType.LOCK,
                "Password",
                true
        );
        passwordInput.setAlignmentX(CENTER_ALIGNMENT);

        // Enter key navigation
        identifierInput.addActionListener(e -> passwordInput.requestFocusInWindow());
        passwordInput.addActionListener(e -> performLogin());

        // "Forgot password?" right-aligned helper link
        JPanel forgotRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotRow.setOpaque(false);
        forgotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        forgotRow.setAlignmentX(CENTER_ALIGNMENT);

        JButton forgotBtn = new JButton("Forgot password?");
        forgotBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        forgotBtn.setForeground(new Color(0x94, 0xA3, 0xB8));
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Demo Note:\nFor college viva evaluation, you can use the 1-Click Demo Login buttons below,\n" +
                        "or register a fresh account anytime.",
                "Account Recovery", JOptionPane.INFORMATION_MESSAGE));
        forgotRow.add(forgotBtn);

        // Primary "Get Started" Pill Action Button (matching user's design reference)
        ModernButton getStartedBtn = ModernButton.createPrimary("Get Started");
        getStartedBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        getStartedBtn.setCornerRadius(14);
        getStartedBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        getStartedBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        getStartedBtn.setAlignmentX(CENTER_ALIGNMENT);
        getStartedBtn.addActionListener(e -> performLogin());

        // Dotted Divider: · · · · · · · · Or sign in with · · · · · · · ·
        JPanel dottedDivider = createDottedDivider("Or sign in with");
        dottedDivider.setAlignmentX(CENTER_ALIGNMENT);

        // 1-Click Demo Accounts Row (Pill buttons)
        JPanel demoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        demoRow.setOpaque(false);
        demoRow.setAlignmentX(CENTER_ALIGNMENT);

        ModernButton creatorDemo = ModernButton.createSecondary("Creator Demo");
        creatorDemo.setPreferredSize(new Dimension(145, 36));
        creatorDemo.setCornerRadius(12);
        creatorDemo.setFont(UITheme.FONT_BODY);
        creatorDemo.addActionListener(e -> {
            identifierInput.setText("creator@testforge.com");
            passwordInput.setText("password123");
            performLogin();
        });

        ModernButton studentDemo = ModernButton.createSecondary("Student Demo");
        studentDemo.setPreferredSize(new Dimension(145, 36));
        studentDemo.setCornerRadius(12);
        studentDemo.setFont(UITheme.FONT_BODY);
        studentDemo.addActionListener(e -> {
            identifierInput.setText("student@testforge.com");
            passwordInput.setText("password123");
            performLogin();
        });

        demoRow.add(creatorDemo);
        demoRow.add(studentDemo);

        // Register Switch Link
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchPanel.setOpaque(false);
        switchPanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setForeground(UITheme.TEXT_MUTED);
        noAcc.setFont(UITheme.FONT_BODY);

        JButton regBtn = new JButton("Register");
        regBtn.setFont(UITheme.FONT_BOLD);
        regBtn.setForeground(UITheme.ACCENT_CYAN);
        regBtn.setContentAreaFilled(false);
        regBtn.setBorderPainted(false);
        regBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regBtn.addActionListener(e -> mainFrame.showRegister());

        switchPanel.add(noAcc);
        switchPanel.add(regBtn);

        // Assemble into Card
        card.add(welcomeTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(welcomeSub);
        card.add(Box.createVerticalStrut(24));

        card.add(identifierInput);
        card.add(Box.createVerticalStrut(14));

        card.add(passwordInput);
        card.add(Box.createVerticalStrut(8));
        card.add(forgotRow);
        card.add(Box.createVerticalStrut(18));

        card.add(getStartedBtn);
        card.add(Box.createVerticalStrut(18));

        card.add(dottedDivider);
        card.add(Box.createVerticalStrut(12));

        card.add(demoRow);
        card.add(Box.createVerticalStrut(16));

        card.add(switchPanel);

        return card;
    }

    /**
     * Creates a custom dotted separator matching the reference image.
     */
    private JPanel createDottedDivider(String label) {
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int midY = getHeight() / 2;
                g2.setFont(UITheme.FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(label);
                int textX = (w - textW) / 2;

                // Dotted stroke
                Stroke dotted = new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 5}, 0);
                g2.setStroke(dotted);
                g2.setColor(new Color(0x33, 0x41, 0x55));
                g2.drawLine(8, midY, textX - 10, midY);
                g2.drawLine(textX + textW + 10, midY, w - 8, midY);

                // Centered text
                g2.setColor(new Color(0x94, 0xA3, 0xB8));
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(label, textX, textY);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(360, 20));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return divider;
    }

    private void performLogin() {
        String identifier = identifierInput.getText().trim();
        String password = passwordInput.getPassword();

        try {
            User user = authService.login(identifier, password);
            SessionManager.setCurrentUser(user);
            passwordInput.setText("");
            mainFrame.onLoginSuccess(user);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred during login:\n" + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void clearFields() {
        identifierInput.setText("");
        passwordInput.setText("");
    }
}
