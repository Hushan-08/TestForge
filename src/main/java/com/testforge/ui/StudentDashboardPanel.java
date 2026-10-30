package com.testforge.ui;

import com.testforge.model.Exam;
import com.testforge.model.ExamAttempt;
import com.testforge.model.User;
import com.testforge.service.ExamService;
import com.testforge.service.GradingService;
import com.testforge.ui.components.Badge;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.ModernCard;
import com.testforge.ui.components.ModernTextField;
import com.testforge.ui.components.UITheme;
import com.testforge.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern student portal for searching available exams, viewing pre-exam instructions,
 * launching attempts with clear unmissable buttons, and reviewing score history.
 */
public class StudentDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ExamService examService;
    private final GradingService gradingService;

    private ModernTextField accessCodeField;
    private JPanel lobbyContainer;
    private Exam matchedExam = null;

    // Quick launch banner inside search card
    private JPanel quickLaunchBar;
    private JLabel quickExamTitleLabel;
    private ModernButton quickStartBtn;

    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private List<ExamAttempt> historyList = new ArrayList<>();

    // Quick stats
    private JLabel totalAttemptsStat;
    private JLabel averageScoreStat;

    public StudentDashboardPanel(MainFrame mainFrame, ExamService examService, GradingService gradingService) {
        this.mainFrame = mainFrame;
        this.examService = examService;
        this.gradingService = gradingService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        // Top Banner
        JPanel topBanner = new JPanel(new BorderLayout(12, 0));
        topBanner.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel("Student Examination Portal");
        titleLabel.setFont(UITheme.FONT_BRAND);
        titleLabel.setForeground(UITheme.ACCENT_CYAN);

        JLabel subLabel = new JLabel("Enter an exam access code to begin your examination or inspect your past scorecards.");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subLabel);

        // Stats Row
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        statsRow.setOpaque(false);

        totalAttemptsStat = new JLabel("0");
        averageScoreStat = new JLabel("0.0%");

        statsRow.add(createMiniStatCard("Total Attempts", totalAttemptsStat, UITheme.ACCENT_CYAN));
        statsRow.add(createMiniStatCard("Avg. Performance", averageScoreStat, UITheme.SUCCESS));

        topBanner.add(titleBox, BorderLayout.WEST);
        topBanner.add(statsRow, BorderLayout.EAST);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.BG_CARD);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);

        tabbedPane.addTab("🚀  Take An Exam", createTakeExamPanel());
        tabbedPane.addTab("📈  My Score History", createHistoryPanel());

        add(topBanner, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Auto-load pre-seeded exam into lobby immediately for seamless student experience
        javax.swing.SwingUtilities.invokeLater(() -> searchExam());
    }

    private JPanel createMiniStatCard(String label, JLabel valueLabel, Color accent) {
        ModernCard card = new ModernCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(140, 62));
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        card.setTopAccentColor(accent);

        valueLabel.setFont(UITheme.FONT_SUBTITLE);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel desc = new JLabel(label);
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_MUTED);

        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(desc);
        return card;
    }

    private JPanel createTakeExamPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Top Search Bar Card
        ModernCard searchCard = new ModernCard();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        searchCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        searchCard.setTopAccentColor(UITheme.PRIMARY_BLUE);

        // Row 1: Input & Find button
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        searchRow.setOpaque(false);

        JLabel promptLabel = new JLabel("🔑  Enter Exam Access Code:");
        promptLabel.setFont(UITheme.FONT_BOLD);
        promptLabel.setForeground(UITheme.TEXT_PRIMARY);

        accessCodeField = new ModernTextField();
        accessCodeField.setPreferredSize(new Dimension(240, 42));
        accessCodeField.setFont(UITheme.FONT_BOLD);
        accessCodeField.setText("JAVA-101"); // Default demo code
        accessCodeField.addActionListener(e -> searchExam());

        ModernButton searchBtn = ModernButton.createPrimary("🔍  Find Exam");
        searchBtn.setPreferredSize(new Dimension(140, 42));
        searchBtn.addActionListener(e -> searchExam());

        JLabel hintLabel = new JLabel("(e.g. Try pre-seeded code: JAVA-101)");
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.TEXT_MUTED);

        searchRow.add(promptLabel);
        searchRow.add(accessCodeField);
        searchRow.add(searchBtn);
        searchRow.add(hintLabel);

        // Row 2: Quick Launch Action Banner (Visible as soon as exam is found)
        quickLaunchBar = new JPanel(new BorderLayout(12, 0));
        quickLaunchBar.setOpaque(false);
        quickLaunchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(10, 4, 2, 4)
        ));
        quickLaunchBar.setVisible(false);

        quickExamTitleLabel = new JLabel("Exam Found: Java Core & OOP Fundamentals");
        quickExamTitleLabel.setFont(UITheme.FONT_BOLD);
        quickExamTitleLabel.setForeground(UITheme.ACCENT_CYAN);

        quickStartBtn = ModernButton.createSuccess("🚀  START EXAM NOW →");
        quickStartBtn.setFont(UITheme.FONT_SUBTITLE);
        quickStartBtn.setPreferredSize(new Dimension(220, 40));
        quickStartBtn.addActionListener(e -> {
            User student = SessionManager.getCurrentUser();
            if (matchedExam == null) {
                searchExam();
            }
            if (matchedExam != null && student != null) {
                launchExam(matchedExam, student);
            }
        });

        quickLaunchBar.add(quickExamTitleLabel, BorderLayout.CENTER);
        quickLaunchBar.add(quickStartBtn, BorderLayout.EAST);

        searchCard.add(searchRow);
        searchCard.add(quickLaunchBar);

        // Lobby Card Container (dynamically populated when exam found)
        lobbyContainer = new JPanel(new BorderLayout());
        lobbyContainer.setOpaque(false);

        showPlaceholderLobby();

        panel.add(searchCard, BorderLayout.NORTH);
        panel.add(lobbyContainer, BorderLayout.CENTER);
        return panel;
    }

    private void showPlaceholderLobby() {
        lobbyContainer.removeAll();
        quickLaunchBar.setVisible(false);

        ModernCard placeholderCard = new ModernCard();
        placeholderCard.setLayout(new BoxLayout(placeholderCard, BoxLayout.Y_AXIS));
        placeholderCard.setBorder(BorderFactory.createEmptyBorder(45, 30, 45, 30));

        JLabel iconLabel = new JLabel("🎯", JLabel.CENTER);
        iconLabel.setFont(UITheme.FONT_TITLE.deriveFont(44f));
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel infoText = new JLabel("Enter an Exam Access Code above (e.g. JAVA-101) and click 'Find Exam'", JLabel.CENTER);
        infoText.setFont(UITheme.FONT_TITLE.deriveFont(16f));
        infoText.setForeground(UITheme.TEXT_PRIMARY);
        infoText.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subText = new JLabel("Your examination lobby, timer rules, question counts, and Start button will appear here.", JLabel.CENTER);
        subText.setFont(UITheme.FONT_BODY);
        subText.setForeground(UITheme.TEXT_SECONDARY);
        subText.setAlignmentX(CENTER_ALIGNMENT);

        // Quick demo pill button
        ModernButton quickDemoBtn = ModernButton.createSecondary("⚡ Quick Demo: Load JAVA-101");
        quickDemoBtn.setAlignmentX(CENTER_ALIGNMENT);
        quickDemoBtn.setPreferredSize(new Dimension(220, 36));
        quickDemoBtn.addActionListener(e -> {
            accessCodeField.setText("JAVA-101");
            searchExam();
        });

        placeholderCard.add(iconLabel);
        placeholderCard.add(Box.createVerticalStrut(10));
        placeholderCard.add(infoText);
        placeholderCard.add(Box.createVerticalStrut(4));
        placeholderCard.add(subText);
        placeholderCard.add(Box.createVerticalStrut(16));
        placeholderCard.add(quickDemoBtn);

        lobbyContainer.add(placeholderCard, BorderLayout.CENTER);
        lobbyContainer.revalidate();
        lobbyContainer.repaint();
    }

    private void searchExam() {
        String code = accessCodeField.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Exam Access Code.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User student = SessionManager.getCurrentUser();
        if (student == null) return;

        try {
            Exam exam = examService.getExamByAccessCode(code);
            if (exam == null) {
                JOptionPane.showMessageDialog(this,
                        "No examination found with access code '" + code + "'.\nPlease check the code with your instructor.",
                        "Exam Not Found", JOptionPane.WARNING_MESSAGE);
                showPlaceholderLobby();
                return;
            }

            if (!exam.isPublished()) {
                JOptionPane.showMessageDialog(this,
                        "This exam ('" + exam.getTitle() + "') is currently unpublished (draft) by the instructor and cannot be accessed yet.",
                        "Exam Unpublished", JOptionPane.INFORMATION_MESSAGE);
                showPlaceholderLobby();
                return;
            }

            matchedExam = exam;
            displayExamLobby(exam, student);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error looking up exam: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayExamLobby(Exam exam, User student) {
        lobbyContainer.removeAll();

        // Enable quick launch bar on search card
        quickExamTitleLabel.setText("✅ Ready: " + exam.getTitle() + " (" + exam.getDurationMinutes() + " mins)");
        quickLaunchBar.setVisible(true);

        // Main Lobby Scrollable Panel
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        ModernCard lobbyCard = new ModernCard();
        lobbyCard.setLayout(new BoxLayout(lobbyCard, BoxLayout.Y_AXIS));
        lobbyCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        lobbyCard.setTopAccentColor(UITheme.SUCCESS);

        // Top Header of Lobby Card with Immediate Big Start Button!
        JPanel headerRow = new JPanel(new BorderLayout(14, 0));
        headerRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel(exam.getTitle());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.ACCENT_CYAN);

        JLabel descLabel = new JLabel("<html><body style='width: 520px;'>" +
                (exam.getDescription() != null ? exam.getDescription() : "No additional description.") +
                "</body></html>");
        descLabel.setFont(UITheme.FONT_BODY);
        descLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(descLabel);

        // Prominent Top Start Button
        ModernButton topStartBtn = ModernButton.createSuccess("🚀  START EXAM NOW →");
        topStartBtn.setFont(UITheme.FONT_SUBTITLE);
        topStartBtn.setPreferredSize(new Dimension(240, 48));
        topStartBtn.addActionListener(e -> launchExam(exam, student));

        headerRow.add(titleBox, BorderLayout.CENTER);
        headerRow.add(topStartBtn, BorderLayout.EAST);

        // Metrics Grid Panel
        JPanel detailsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.setPreferredSize(new Dimension(0, 75));

        detailsPanel.add(createDetailCard("⏱ Duration", exam.getDurationMinutes() + " Minutes", UITheme.ACCENT_CYAN));
        detailsPanel.add(createDetailCard("📝 Questions", exam.getQuestionCount() + " Total", UITheme.PRIMARY_BLUE));
        detailsPanel.add(createDetailCard("🏆 Max Score", exam.getTotalMarks() + " Marks", UITheme.SUCCESS));
        detailsPanel.add(createDetailCard("🎯 Passing Mark", String.format("%.0f%%", exam.getPassPercentage()), UITheme.WARNING));

        // Instructions Card
        ModernCard instructionsCard = new ModernCard();
        instructionsCard.setLayout(new BoxLayout(instructionsCard, BoxLayout.Y_AXIS));
        instructionsCard.setCardBackground(UITheme.BG_SURFACE);
        instructionsCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel insTitle = new JLabel("📋  Important Examination Rules & Guidelines:");
        insTitle.setFont(UITheme.FONT_BOLD);
        insTitle.setForeground(UITheme.TEXT_PRIMARY);

        instructionsCard.add(insTitle);
        instructionsCard.add(Box.createVerticalStrut(8));
        instructionsCard.add(createInstructionBullet("1. Timer: The countdown begins the moment you click 'Start Exam Now'."));
        instructionsCard.add(createInstructionBullet("2. Randomization: Question order and multiple-choice options are shuffled per attempt."));
        instructionsCard.add(createInstructionBullet("3. Real-Time Autosave: Your selected choices are continuously saved to the database."));
        instructionsCard.add(createInstructionBullet("4. Auto-Submission: The exam submits automatically when the timer reaches 00:00."));
        instructionsCard.add(createInstructionBullet("5. Single Attempt: Only one active submission is permitted per student."));

        // Bottom Start Action Bar
        JPanel bottomActionBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        bottomActionBar.setOpaque(false);

        ModernButton bottomStartBtn = ModernButton.createSuccess("🚀  START EXAMINATION NOW  (Ready to Begin)");
        bottomStartBtn.setFont(UITheme.FONT_SUBTITLE);
        bottomStartBtn.setPreferredSize(new Dimension(380, 50));
        bottomStartBtn.addActionListener(e -> launchExam(exam, student));

        bottomActionBar.add(bottomStartBtn);

        lobbyCard.add(headerRow);
        lobbyCard.add(Box.createVerticalStrut(14));
        lobbyCard.add(detailsPanel);
        lobbyCard.add(Box.createVerticalStrut(14));
        lobbyCard.add(instructionsCard);
        lobbyCard.add(Box.createVerticalStrut(14));
        lobbyCard.add(bottomActionBar);

        scrollContent.add(lobbyCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        lobbyContainer.add(scrollPane, BorderLayout.CENTER);
        lobbyContainer.revalidate();
        lobbyContainer.repaint();
    }

    private JPanel createDetailCard(String title, String value, Color accent) {
        ModernCard card = new ModernCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        card.setTopAccentColor(accent);
        card.setCardBackground(UITheme.BG_SURFACE);

        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_SMALL);
        t.setForeground(UITheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_SUBTITLE);
        v.setForeground(UITheme.TEXT_PRIMARY);

        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(v);
        return card;
    }

    private JLabel createInstructionBullet(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return l;
    }

    private void launchExam(Exam exam, User student) {
        if (exam == null || student == null) {
            searchExam();
            exam = matchedExam;
            student = SessionManager.getCurrentUser();
            if (exam == null || student == null) {
                JOptionPane.showMessageDialog(this, "Please search and select an exam first.",
                        "No Exam Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            ExamAttempt attempt;
            try {
                attempt = examService.startOrResumeExamAttempt(exam.getId(), student.getId());
            } catch (IllegalStateException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("already submitted")) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "You have already completed an attempt for this exam.\n\n" +
                            "Would you like to start a fresh attempt for your demonstration?",
                            "Start Fresh Attempt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        attempt = examService.startFreshExamAttempt(exam.getId(), student.getId());
                    } else {
                        return;
                    }
                } else {
                    throw ex;
                }
            }

            // Hide parent dashboard so exam screen is front & center without distractions
            mainFrame.setVisible(false);

            ExamTakingFrame examFrame = new ExamTakingFrame(mainFrame, exam, attempt, examService, gradingService);
            examFrame.setVisible(true);
            examFrame.toFront();
            examFrame.requestFocus();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Start Exam", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to launch exam:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        String[] columns = {"#", "Exam Title", "Score Obtained", "Total Marks", "Percentage", "Result", "Submitted At"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(34);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(160);

        // Center-align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolbar.setOpaque(false);

        ModernButton viewScorecardBtn = ModernButton.createPrimary("🔍  View Scorecard & Review");
        viewScorecardBtn.setPreferredSize(new Dimension(210, 38));
        viewScorecardBtn.addActionListener(e -> viewSelectedScorecard());

        ModernButton refreshBtn = ModernButton.createSecondary("Refresh");
        refreshBtn.setPreferredSize(new Dimension(90, 38));
        refreshBtn.addActionListener(e -> refreshHistory());

        toolbar.add(viewScorecardBtn);
        toolbar.add(refreshBtn);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void refreshHistory() {
        User student = SessionManager.getCurrentUser();
        if (student == null) return;

        try {
            historyList = examService.getStudentAttempts(student.getId());
            historyTableModel.setRowCount(0);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            double totalPercentage = 0.0;

            for (int i = 0; i < historyList.size(); i++) {
                ExamAttempt att = historyList.get(i);
                totalPercentage += att.getPercentage();

                String dateStr = (att.getSubmitTime() != null)
                        ? att.getSubmitTime().format(dtf)
                        : (att.getStartTime() != null ? att.getStartTime().format(dtf) : "-");

                String passStatus = (att.getPercentage() >= 50.0) ? "PASSED" : "FAILED";
                if ("IN_PROGRESS".equalsIgnoreCase(att.getStatus())) {
                    passStatus = "IN PROGRESS";
                }

                historyTableModel.addRow(new Object[]{
                        i + 1,
                        att.getExamTitle() != null ? att.getExamTitle() : "Exam #" + att.getExamId(),
                        String.format("%.1f", att.getScore()),
                        String.format("%.1f", att.getTotalMarks()),
                        String.format("%.1f%%", att.getPercentage()),
                        passStatus,
                        dateStr
                });
            }

            // Update stats
            totalAttemptsStat.setText(String.valueOf(historyList.size()));
            double avg = historyList.isEmpty() ? 0.0 : (totalPercentage / historyList.size());
            averageScoreStat.setText(String.format("%.1f%%", avg));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load attempt history: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedScorecard() {
        int row = historyTable.getSelectedRow();
        if (row < 0 || row >= historyList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an attempt from the table to view the scorecard.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ExamAttempt att = historyList.get(row);
        try {
            Exam exam = examService.getExamById(att.getExamId());
            ExamResultsDialog dialog = new ExamResultsDialog(mainFrame, att, exam, gradingService, false);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading results: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
