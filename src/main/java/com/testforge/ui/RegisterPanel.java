package com.testforge.ui;

import com.testforge.model.User;
import com.testforge.service.AuthService;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.ModernCard;
import com.testforge.ui.components.PillInputField;
import com.testforge.ui.components.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;

/**
 * Modern, centered registration interface with pill-shaped inputs.
 */
public class RegisterPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    private PillInputField fullNameInput;
    private PillInputField usernameInput;
    private PillInputField emailInput;
    private PillInputField passwordInput;
    private JComboBox<String> roleCombo;

    public RegisterPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        JPanel centerCanvas = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                float radius = Math.max(w, h) * 0.55f;
                RadialGradientPaint p = new RadialGradientPaint(
                        w * 0.5f, h * 0.45f, radius,
                        new float[]{0.0f, 0.45f, 1.0f},
                        new Color[]{
                                new Color(0x10, 0xB9, 0x81, 24),
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

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // 1. TOP-CENTER BRAND HEADER
        JPanel brandHeader = createBrandHeader();
        brandHeader.setAlignmentX(CENTER_ALIGNMENT);

        // 2. CENTER REGISTRATION CARD
        ModernCard registerCard = createRegisterCard();
        registerCard.setAlignmentX(CENTER_ALIGNMENT);

        centerContainer.add(brandHeader);
        centerContainer.add(Box.createVerticalStrut(20));
        centerContainer.add(registerCard);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 20, 30, 20);

        centerCanvas.add(centerContainer, gbc);

        JScrollPane scrollPane = new JScrollPane(centerCanvas);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createBrandHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(CENTER_ALIGNMENT);

        JLabel brandIcon = new JLabel("⚡");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        brandIcon.setForeground(UITheme.SUCCESS);

        JLabel brandTitle = new JLabel("TestForge");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        brandTitle.setForeground(UITheme.TEXT_PRIMARY);

        titleRow.add(brandIcon);
        titleRow.add(brandTitle);

        JLabel brandTagline = new JLabel("“Forge your future.”", SwingConstants.CENTER);
        brandTagline.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        brandTagline.setForeground(UITheme.SUCCESS);
        brandTagline.setAlignmentX(CENTER_ALIGNMENT);

        header.add(titleRow);
        header.add(Box.createVerticalStrut(4));
        header.add(brandTagline);

        return header;
    }

    private ModernCard createRegisterCard() {
        ModernCard card = new ModernCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 560));
        card.setMaximumSize(new Dimension(460, 560));
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        card.setTopAccentColor(UITheme.SUCCESS);

        // Header
        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join as an Exam Creator or Student.", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Pill Input Fields
        fullNameInput = new PillInputField(PillInputField.IconType.USER, "Full Name", false);
        fullNameInput.setAlignmentX(CENTER_ALIGNMENT);

        usernameInput = new PillInputField(PillInputField.IconType.USER, "Username", false);
        usernameInput.setAlignmentX(CENTER_ALIGNMENT);

        emailInput = new PillInputField(PillInputField.IconType.EMAIL, "Email Address", false);
        emailInput.setAlignmentX(CENTER_ALIGNMENT);

        passwordInput = new PillInputField(PillInputField.IconType.LOCK, "Password (min 6 characters)", true);
        passwordInput.setAlignmentX(CENTER_ALIGNMENT);

        // Role Selector with pill-styled container
        JPanel rolePanel = new JPanel(new BorderLayout(10, 0));
        rolePanel.setOpaque(false);
        rolePanel.setPreferredSize(new Dimension(360, 42));
        rolePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        rolePanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(UITheme.FONT_BOLD);
        roleLabel.setForeground(UITheme.TEXT_PRIMARY);

        String[] roles = {"Student", "Exam Creator"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(UITheme.FONT_BODY);
        roleCombo.setBackground(UITheme.BG_INPUT);
        roleCombo.setForeground(UITheme.TEXT_PRIMARY);

        rolePanel.add(roleLabel, BorderLayout.WEST);
        rolePanel.add(roleCombo, BorderLayout.CENTER);

        // Submit Button
        ModernButton registerBtn = ModernButton.createSuccess("Get Started");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        registerBtn.setCornerRadius(14);
        registerBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> performRegister());

        // Back to Login Link
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchPanel.setOpaque(false);
        switchPanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hasAccountLabel = new JLabel("Already have an account?");
        hasAccountLabel.setForeground(UITheme.TEXT_MUTED);
        hasAccountLabel.setFont(UITheme.FONT_BODY);

        JButton loginLink = new JButton("Sign in");
        loginLink.setFont(UITheme.FONT_BOLD);
        loginLink.setForeground(UITheme.ACCENT_CYAN);
        loginLink.setContentAreaFilled(false);
        loginLink.setBorderPainted(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addActionListener(e -> mainFrame.showLogin());

        switchPanel.add(hasAccountLabel);
        switchPanel.add(loginLink);

        // Assemble into Card
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(20));

        card.add(fullNameInput);
        card.add(Box.createVerticalStrut(10));

        card.add(usernameInput);
        card.add(Box.createVerticalStrut(10));

        card.add(emailInput);
        card.add(Box.createVerticalStrut(10));

        card.add(passwordInput);
        card.add(Box.createVerticalStrut(10));

        card.add(rolePanel);
        card.add(Box.createVerticalStrut(18));

        card.add(registerBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(switchPanel);

        return card;
    }

    private void performRegister() {
        String fullName = fullNameInput.getText().trim();
        String username = usernameInput.getText().trim();
        String email = emailInput.getText().trim();
        String password = passwordInput.getPassword();
        String selectedRole = (String) roleCombo.getSelectedItem();
        String role = "Exam Creator".equals(selectedRole) ? "CREATOR" : "STUDENT";

        try {
            User user = authService.register(username, fullName, email, password, role);
            JOptionPane.showMessageDialog(this,
                    "🎉 Account created successfully!\nPlease sign in with your credentials.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            mainFrame.showLogin();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred:\n" + ex.getMessage(),
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void clearFields() {
        fullNameInput.setText("");
        usernameInput.setText("");
        emailInput.setText("");
        passwordInput.setText("");
        roleCombo.setSelectedIndex(0);
    }
}
