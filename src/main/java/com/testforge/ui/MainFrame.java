package com.testforge.ui;

import com.testforge.model.User;
import com.testforge.service.AuthService;
import com.testforge.service.ExamService;
import com.testforge.service.GradingService;
import com.testforge.ui.components.Badge;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.UITheme;
import com.testforge.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Main application window for TestForge.
 * Manages view transitions between Login, Registration, Creator Studio, and Student Portal.
 */
public class MainFrame extends JFrame {

    private final AuthService authService;
    private final ExamService examService;
    private final GradingService gradingService;

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private CreatorDashboardPanel creatorDashboard;
    private StudentDashboardPanel studentDashboard;

    // Top Navigation Header
    private JPanel topHeaderBar;
    private JLabel brandTitle;
    private JLabel brandTagline;
    private JPanel userProfilePanel;
    private JLabel userNameLabel;
    private Badge userRoleBadge;
    private ModernButton logoutBtn;

    public MainFrame() {
        super("TestForge — Desktop Exam Application");
        this.authService = new AuthService();
        this.examService = new ExamService();
        this.gradingService = new GradingService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1040, 700));
        setLocationRelativeTo(null);

        initUI();
        showLogin();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);

        // Header Navigation Bar
        topHeaderBar = createHeaderBar();
        root.add(topHeaderBar, BorderLayout.NORTH);

        // Card Layout for Screens
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(UITheme.BG_DARK);

        loginPanel = new LoginPanel(this, authService);
        registerPanel = new RegisterPanel(this, authService);
        creatorDashboard = new CreatorDashboardPanel(this, examService, gradingService);
        studentDashboard = new StudentDashboardPanel(this, examService, gradingService);

        cardsPanel.add(loginPanel, "LOGIN");
        cardsPanel.add(registerPanel, "REGISTER");
        cardsPanel.add(creatorDashboard, "CREATOR");
        cardsPanel.add(studentDashboard, "STUDENT");

        root.add(cardsPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Brand Logo & Tagline
        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        brandTitle = new JLabel("⚡ TestForge");
        brandTitle.setFont(UITheme.FONT_BRAND);
        brandTitle.setForeground(UITheme.ACCENT_CYAN);

        brandTagline = new JLabel("“Forge your future.”  •  College Assessment Platform");
        brandTagline.setFont(UITheme.FONT_SMALL);
        brandTagline.setForeground(UITheme.TEXT_SECONDARY);

        brandBox.add(brandTitle);
        brandBox.add(brandTagline);

        // User Profile & Logout (shown when logged in)
        userProfilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userProfilePanel.setOpaque(false);
        userProfilePanel.setVisible(false);

        userNameLabel = new JLabel("John Doe");
        userNameLabel.setFont(UITheme.FONT_BOLD);
        userNameLabel.setForeground(UITheme.TEXT_PRIMARY);

        userRoleBadge = Badge.info("STUDENT");

        logoutBtn = ModernButton.createSecondary("Sign Out");
        logoutBtn.setPreferredSize(new Dimension(100, 34));
        logoutBtn.addActionListener(e -> performLogout());

        userProfilePanel.add(userNameLabel);
        userProfilePanel.add(userRoleBadge);
        userProfilePanel.add(logoutBtn);

        bar.add(brandBox, BorderLayout.WEST);
        bar.add(userProfilePanel, BorderLayout.EAST);
        return bar;
    }

    public void showLogin() {
        topHeaderBar.setVisible(false);
        userProfilePanel.setVisible(false);
        loginPanel.clearFields();
        cardLayout.show(cardsPanel, "LOGIN");
    }

    public void showRegister() {
        topHeaderBar.setVisible(false);
        userProfilePanel.setVisible(false);
        registerPanel.clearFields();
        cardLayout.show(cardsPanel, "REGISTER");
    }

    public void onLoginSuccess(User user) {
        topHeaderBar.setVisible(true);
        userNameLabel.setText(user.getFullName());

        if (user.isCreator()) {
            userRoleBadge.setText("EXAM CREATOR");
            userRoleBadge.setForeground(UITheme.ACCENT_CYAN);
            creatorDashboard.refreshExams();
            cardLayout.show(cardsPanel, "CREATOR");
        } else {
            userRoleBadge.setText("STUDENT");
            userRoleBadge.setForeground(UITheme.SUCCESS);
            studentDashboard.refreshHistory();
            cardLayout.show(cardsPanel, "STUDENT");
        }

        userProfilePanel.setVisible(true);
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to sign out?",
                "Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            showLogin();
        }
    }

    public void refreshStudentDashboard() {
        studentDashboard.refreshHistory();
    }

    public void refreshCreatorDashboard() {
        creatorDashboard.refreshExams();
    }
}
