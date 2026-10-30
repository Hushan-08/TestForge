package com.testforge.ui;

import com.testforge.model.AnswerOption;
import com.testforge.model.Exam;
import com.testforge.model.ExamAttempt;
import com.testforge.model.Question;
import com.testforge.model.StudentAnswer;
import com.testforge.service.ExamService;
import com.testforge.service.GradingService;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.ModernCard;
import com.testforge.ui.components.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modern, distraction-free exam examination environment.
 * Features:
 * - Live real-time countdown timer using javax.swing.Timer (safe on EDT)
 * - Anti-tampering timer calculated from start timestamp
 * - Instant database autosave upon answer change
 * - Interactive question palette grid
 * - Accidental exit warning with graceful submission
 * - Automatic submission on time expiration
 */
public class ExamTakingFrame extends JFrame {

    private final Exam exam;
    private final ExamAttempt attempt;
    private final ExamService examService;
    private final GradingService gradingService;
    private final JFrame parentDashboard;

    private List<Question> questions = new ArrayList<>();
    private Map<Integer, StudentAnswer> savedAnswers = new HashMap<>();
    private int currentIndex = 0;

    // UI Components
    private JLabel timerLabel;
    private JLabel progressCountLabel;
    private JLabel questionHeaderLabel;
    private JLabel questionTextLabel;
    private JPanel optionsPanel;
    private List<OptionCard> currentOptionCards = new ArrayList<>();
    private ButtonGroup optionButtonGroup;

    private JPanel paletteGrid;
    private List<ModernButton> paletteButtons = new ArrayList<>();
    private ModernButton prevBtn;
    private ModernButton nextBtn;
    private ModernButton markReviewBtn;

    private Timer countdownTimer;
    private long totalAllowedSeconds;
    private boolean isSubmitted = false;

    public ExamTakingFrame(JFrame parentDashboard, Exam exam, ExamAttempt attempt,
                           ExamService examService, GradingService gradingService) {
        super("TestForge Examination — " + exam.getTitle());
        this.parentDashboard = parentDashboard;
        this.exam = exam;
        this.attempt = attempt;
        this.examService = examService;
        this.gradingService = gradingService;

        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Confirmation on accidental close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        loadExamData();
        initUI();
        initTimer();
        displayQuestion(0);
    }

    private void loadExamData() {
        try {
            // Load randomized questions based on attempt seed
            questions = examService.getRandomizedQuestionsForAttempt(exam.getId(), attempt.getQuestionOrderSeed());
            // Restore any previously autosaved answers
            savedAnswers = examService.getSavedAnswers(attempt.getId());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load examination questions:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            if (parentDashboard != null) {
                parentDashboard.setVisible(true);
                parentDashboard.toFront();
                parentDashboard.requestFocus();
            }
        }
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(UITheme.BG_DARK);

        // Top Navigation & Timer Bar
        JPanel topBar = createTopBar();
        contentPane.add(topBar, BorderLayout.NORTH);

        // Center split: Left Palette, Right Question Canvas
        JPanel centerSplit = new JPanel(new BorderLayout(14, 0));
        centerSplit.setBackground(UITheme.BG_DARK);
        centerSplit.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel leftPalette = createPalettePanel();
        JPanel rightCanvas = createQuestionCanvas();

        centerSplit.add(leftPalette, BorderLayout.WEST);
        centerSplit.add(rightCanvas, BorderLayout.CENTER);

        contentPane.add(centerSplit, BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        // Left: Exam Title & Subtitle
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel brandLabel = new JLabel("TestForge  •  " + exam.getTitle());
        brandLabel.setFont(UITheme.FONT_SUBTITLE);
        brandLabel.setForeground(UITheme.ACCENT_CYAN);

        progressCountLabel = new JLabel("Question 1 of " + questions.size());
        progressCountLabel.setFont(UITheme.FONT_BODY);
        progressCountLabel.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(brandLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(progressCountLabel);

        // Right: Live Timer Box & Submit Button
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightBox.setOpaque(false);

        JPanel timerCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        timerCard.setBackground(UITheme.BG_INPUT);
        timerCard.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        JLabel clockIcon = new JLabel("⏱");
        clockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        clockIcon.setForeground(UITheme.ACCENT_CYAN);

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(UITheme.FONT_TIMER);
        timerLabel.setForeground(UITheme.TEXT_PRIMARY);

        timerCard.add(clockIcon);
        timerCard.add(timerLabel);

        ModernButton submitBtn = ModernButton.createSuccess("Submit Exam");
        submitBtn.setPreferredSize(new Dimension(135, 42));
        submitBtn.addActionListener(e -> confirmManualSubmit());

        rightBox.add(timerCard);
        rightBox.add(submitBtn);

        bar.add(titlePanel, BorderLayout.WEST);
        bar.add(rightBox, BorderLayout.EAST);
        return bar;
    }

    private JPanel createPalettePanel() {
        ModernCard card = new ModernCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(240, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel palTitle = new JLabel("Question Navigator");
        palTitle.setFont(UITheme.FONT_BOLD);
        palTitle.setForeground(UITheme.TEXT_PRIMARY);

        // Palette Button Grid
        paletteGrid = new JPanel(new GridLayout(0, 4, 6, 6));
        paletteGrid.setOpaque(false);

        paletteButtons.clear();
        for (int i = 0; i < questions.size(); i++) {
            final int index = i;
            ModernButton btn = new ModernButton(String.valueOf(i + 1), UITheme.BG_SURFACE, UITheme.BORDER);
            btn.setFont(UITheme.FONT_BOLD);
            btn.setPreferredSize(new Dimension(42, 38));
            btn.addActionListener(e -> displayQuestion(index));
            paletteButtons.add(btn);
            paletteGrid.add(btn);
        }

        JScrollPane gridScroll = new JScrollPane(paletteGrid);
        gridScroll.setOpaque(false);
        gridScroll.getViewport().setOpaque(false);
        gridScroll.setBorder(null);

        // Legend at bottom
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        legendPanel.add(createLegendItem("■ Answered", UITheme.SUCCESS));
        legendPanel.add(Box.createVerticalStrut(3));
        legendPanel.add(createLegendItem("■ Marked for Review", UITheme.WARNING));
        legendPanel.add(Box.createVerticalStrut(3));
        legendPanel.add(createLegendItem("■ Unanswered", UITheme.BG_SURFACE));

        card.add(palTitle, BorderLayout.NORTH);
        card.add(gridScroll, BorderLayout.CENTER);
        card.add(legendPanel, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createLegendItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(color);
        return l;
    }

    private JPanel createQuestionCanvas() {
        ModernCard card = new ModernCard();
        card.setLayout(new BorderLayout(14, 14));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Question Header: Question # and Marks
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        questionHeaderLabel = new JLabel("Question 1");
        questionHeaderLabel.setFont(UITheme.FONT_SUBTITLE);
        questionHeaderLabel.setForeground(UITheme.ACCENT_CYAN);

        headerPanel.add(questionHeaderLabel, BorderLayout.WEST);

        // Question Text
        questionTextLabel = new JLabel("Question text will load here...");
        questionTextLabel.setFont(UITheme.FONT_TITLE.deriveFont(Font.PLAIN, 16f));
        questionTextLabel.setForeground(UITheme.TEXT_PRIMARY);

        JPanel qTop = new JPanel(new BorderLayout(0, 10));
        qTop.setOpaque(false);
        qTop.add(headerPanel, BorderLayout.NORTH);
        qTop.add(questionTextLabel, BorderLayout.CENTER);

        // Options List Panel
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);

        JScrollPane optionsScroll = new JScrollPane(optionsPanel);
        optionsScroll.setOpaque(false);
        optionsScroll.getViewport().setOpaque(false);
        optionsScroll.setBorder(null);

        // Bottom Navigation Bar
        JPanel bottomNav = new JPanel(new BorderLayout());
        bottomNav.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        markReviewBtn = ModernButton.createSecondary("Mark for Review");
        markReviewBtn.setPreferredSize(new Dimension(150, 38));
        markReviewBtn.addActionListener(e -> toggleMarkForReview());

        ModernButton clearBtn = ModernButton.createSecondary("Clear Selection");
        clearBtn.setPreferredSize(new Dimension(135, 38));
        clearBtn.addActionListener(e -> clearCurrentSelection());

        leftActions.add(markReviewBtn);
        leftActions.add(clearBtn);

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightNav.setOpaque(false);

        prevBtn = ModernButton.createSecondary("← Previous");
        prevBtn.setPreferredSize(new Dimension(110, 38));
        prevBtn.addActionListener(e -> {
            if (currentIndex > 0) displayQuestion(currentIndex - 1);
        });

        nextBtn = new ModernButton("Next →");
        nextBtn.setPreferredSize(new Dimension(110, 38));
        nextBtn.addActionListener(e -> {
            if (currentIndex < questions.size() - 1) displayQuestion(currentIndex + 1);
        });

        rightNav.add(prevBtn);
        rightNav.add(nextBtn);

        bottomNav.add(leftActions, BorderLayout.WEST);
        bottomNav.add(rightNav, BorderLayout.EAST);

        card.add(qTop, BorderLayout.NORTH);
        card.add(optionsScroll, BorderLayout.CENTER);
        card.add(bottomNav, BorderLayout.SOUTH);
        return card;
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentIndex = index;

        Question q = questions.get(index);
        questionHeaderLabel.setText("Question " + (index + 1) + " of " + questions.size() + "  (" + q.getMarks() + " marks)");
        questionTextLabel.setText("<html><body style='width: 650px; line-height: 1.4;'>" + q.getQuestionText() + "</body></html>");
        progressCountLabel.setText("Question " + (index + 1) + " of " + questions.size());

        // Update Nav buttons
        prevBtn.setEnabled(currentIndex > 0);
        nextBtn.setEnabled(currentIndex < questions.size() - 1);

        // Load saved answer if any
        StudentAnswer saved = savedAnswers.get(q.getId());
        Integer selectedOptId = (saved != null) ? saved.getSelectedOptionId() : null;
        boolean isMarked = (saved != null && saved.isMarkedForReview());

        markReviewBtn.setText(isMarked ? "Unmark Review" : "Mark for Review");

        // Populate options
        optionsPanel.removeAll();
        currentOptionCards.clear();
        optionButtonGroup = new ButtonGroup();

        char optChar = 'A';
        for (AnswerOption opt : q.getOptions()) {
            OptionCard card = new OptionCard(optChar, opt, selectedOptId != null && selectedOptId == opt.getId());
            optionButtonGroup.add(card.getRadioButton());
            currentOptionCards.add(card);
            optionsPanel.add(card);
            optionsPanel.add(Box.createVerticalStrut(10));
            optChar++;
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();

        updatePaletteButtons();
    }

    private void onOptionSelected(AnswerOption option) {
        Question q = questions.get(currentIndex);
        StudentAnswer current = savedAnswers.computeIfAbsent(q.getId(),
                k -> new StudentAnswer(attempt.getId(), q.getId(), null, false));
        current.setSelectedOptionId(option.getId());

        // Immediate background autosave
        try {
            examService.saveStudentAnswer(attempt.getId(), q.getId(), option.getId(), current.isMarkedForReview());
        } catch (Exception e) {
            System.err.println("Autosave warning: " + e.getMessage());
        }

        updateOptionCardStyles();
        updatePaletteButtons();
    }

    private void clearCurrentSelection() {
        Question q = questions.get(currentIndex);
        StudentAnswer current = savedAnswers.get(q.getId());
        if (current != null) {
            current.setSelectedOptionId(null);
            try {
                examService.saveStudentAnswer(attempt.getId(), q.getId(), null, current.isMarkedForReview());
            } catch (Exception e) {
                System.err.println("Autosave warning: " + e.getMessage());
            }
        }
        optionButtonGroup.clearSelection();
        updateOptionCardStyles();
        updatePaletteButtons();
    }

    private void toggleMarkForReview() {
        Question q = questions.get(currentIndex);
        StudentAnswer current = savedAnswers.computeIfAbsent(q.getId(),
                k -> new StudentAnswer(attempt.getId(), q.getId(), null, false));
        boolean newMark = !current.isMarkedForReview();
        current.setMarkedForReview(newMark);

        try {
            examService.saveStudentAnswer(attempt.getId(), q.getId(), current.getSelectedOptionId(), newMark);
        } catch (Exception e) {
            System.err.println("Autosave warning: " + e.getMessage());
        }

        markReviewBtn.setText(newMark ? "Unmark Review" : "Mark for Review");
        updatePaletteButtons();
    }

    private void updateOptionCardStyles() {
        for (OptionCard card : currentOptionCards) {
            card.refreshStyle();
        }
    }

    private void updatePaletteButtons() {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            StudentAnswer ans = savedAnswers.get(q.getId());
            ModernButton btn = paletteButtons.get(i);

            boolean isCurrent = (i == currentIndex);
            boolean isAnswered = (ans != null && ans.getSelectedOptionId() != null);
            boolean isMarked = (ans != null && ans.isMarkedForReview());

            if (isMarked) {
                btn.setBackground(UITheme.WARNING);
                btn.setForeground(Color.BLACK);
            } else if (isAnswered) {
                btn.setBackground(UITheme.SUCCESS);
                btn.setForeground(Color.BLACK);
            } else {
                btn.setBackground(UITheme.BG_SURFACE);
                btn.setForeground(UITheme.TEXT_PRIMARY);
            }

            if (isCurrent) {
                btn.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_CYAN, 2));
            } else {
                btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
            }
        }
    }

    private void initTimer() {
        totalAllowedSeconds = exam.getDurationMinutes() * 60L;

        // Calculate accurate remaining time based on attempt start time
        LocalDateTime startTime = attempt.getStartTime() != null ? attempt.getStartTime() : LocalDateTime.now();
        long elapsedSeconds = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        long remaining = Math.max(0, totalAllowedSeconds - elapsedSeconds);

        updateTimerDisplay(remaining);

        if (remaining <= 0) {
            autoSubmitOnTimeout();
            return;
        }

        countdownTimer = new Timer(1000, e -> {
            long currentElapsed = Duration.between(startTime, LocalDateTime.now()).getSeconds();
            long rem = totalAllowedSeconds - currentElapsed;

            if (rem <= 0) {
                countdownTimer.stop();
                autoSubmitOnTimeout();
            } else {
                updateTimerDisplay(rem);
            }
        });
        countdownTimer.start();
    }

    private void updateTimerDisplay(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));

        // Alert visual cue when less than 5 minutes remain
        if (seconds < 300) {
            timerLabel.setForeground(UITheme.DANGER);
        } else if (seconds < 600) {
            timerLabel.setForeground(UITheme.WARNING);
        } else {
            timerLabel.setForeground(UITheme.TEXT_PRIMARY);
        }
    }

    private void autoSubmitOnTimeout() {
        if (isSubmitted) return;
        isSubmitted = true;
        if (countdownTimer != null) countdownTimer.stop();

        JOptionPane.showMessageDialog(this,
                "Time has expired!\nYour examination is being automatically submitted and evaluated.",
                "Time Expired", JOptionPane.WARNING_MESSAGE);

        finalizeAndShowResults("TIMED_OUT");
    }

    private void confirmManualSubmit() {
        int answeredCount = 0;
        for (StudentAnswer ans : savedAnswers.values()) {
            if (ans.getSelectedOptionId() != null) answeredCount++;
        }
        int total = questions.size();
        int unanswered = total - answeredCount;

        String msg = String.format("Submit Examination?\n\nAnswered: %d / %d questions\nUnanswered: %d questions\n\nOnce submitted, you cannot change your answers.",
                answeredCount, total, unanswered);

        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Submission",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (countdownTimer != null) countdownTimer.stop();
            isSubmitted = true;
            finalizeAndShowResults("SUBMITTED");
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Exam is currently in progress!\n\nLeaving now will submit your exam with current saved answers.\nAre you sure you want to exit?",
                "Leave Exam?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (countdownTimer != null) countdownTimer.stop();
            isSubmitted = true;
            finalizeAndShowResults("SUBMITTED");
        }
    }

    private void finalizeAndShowResults(String status) {
        try {
            ExamAttempt graded = gradingService.calculateAndSubmitExam(attempt.getId(), status);
            dispose();

            if (parentDashboard != null) {
                parentDashboard.setVisible(true);
                parentDashboard.toFront();
                parentDashboard.requestFocus();
            }

            ExamResultsDialog dialog = new ExamResultsDialog(parentDashboard, graded, exam, gradingService, false);
            dialog.setVisible(true);

            if (parentDashboard instanceof MainFrame) {
                ((MainFrame) parentDashboard).refreshStudentDashboard();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error evaluating exam: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            if (parentDashboard != null) {
                parentDashboard.setVisible(true);
                parentDashboard.toFront();
                parentDashboard.requestFocus();
            }
        }
    }

    /**
     * Interactive Option Card with Radio button and full-card click capability.
     */
    private class OptionCard extends ModernCard {
        private final char letter;
        private final AnswerOption option;
        private final JRadioButton radio;
        private final JLabel textLabel;

        public OptionCard(char letter, AnswerOption option, boolean selected) {
            this.letter = letter;
            this.option = option;

            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            radio = new JRadioButton();
            radio.setOpaque(false);
            radio.setSelected(selected);
            radio.addActionListener(e -> onOptionSelected(option));

            textLabel = new JLabel("<html><body style='width: 580px; font-size: 13px;'>" +
                    "<b>" + letter + ".</b>  " + option.getOptionText() + "</body></html>");
            textLabel.setFont(UITheme.FONT_BODY);
            textLabel.setForeground(UITheme.TEXT_PRIMARY);

            add(radio, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    radio.setSelected(true);
                    onOptionSelected(option);
                }
            });

            refreshStyle();
        }

        public JRadioButton getRadioButton() {
            return radio;
        }

        public void refreshStyle() {
            if (radio.isSelected()) {
                setCardBackground(new Color(0x1E, 0x3A, 0x8A, 90)); // Vibrant Electric Blue tint
                setBorderColor(UITheme.ACCENT_CYAN);
                setTopAccentColor(UITheme.ACCENT_CYAN);
            } else {
                setCardBackground(UITheme.BG_INPUT);
                setBorderColor(UITheme.BORDER);
                setTopAccentColor(null);
            }
            repaint();
        }
    }
}
