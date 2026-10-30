package com.testforge.ui;

import com.testforge.model.AnswerOption;
import com.testforge.model.Exam;
import com.testforge.model.ExamAttempt;
import com.testforge.model.Question;
import com.testforge.model.QuestionReviewItem;
import com.testforge.service.ExamService;
import com.testforge.service.GradingService;
import com.testforge.ui.components.Badge;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.ModernCard;
import com.testforge.ui.components.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Scorecard and detailed question-by-question review dialog.
 * Respects creator result release permissions.
 */
public class ExamResultsDialog extends JDialog {

    private final ExamAttempt attempt;
    private final Exam exam;
    private final GradingService gradingService;
    private final boolean isCreatorView;

    public ExamResultsDialog(JFrame parent, ExamAttempt attempt, Exam exam, GradingService gradingService, boolean isCreatorView) {
        super(parent, "Exam Scorecard & Results — " + (exam != null ? exam.getTitle() : "TestForge"), true);
        this.attempt = attempt;
        this.exam = exam;
        this.gradingService = gradingService;
        this.isCreatorView = isCreatorView;

        setSize(900, 720);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBackground(UITheme.BG_DARK);
        contentPane.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top Header Card: Score & Pass/Fail status
        ModernCard summaryCard = createSummaryCard();

        // Question Breakdown Scroll Panel
        JPanel reviewListPanel = new JPanel();
        reviewListPanel.setLayout(new BoxLayout(reviewListPanel, BoxLayout.Y_AXIS));
        reviewListPanel.setBackground(UITheme.BG_DARK);

        boolean canViewDetails = isCreatorView || (exam != null && exam.isReleaseResults());

        if (canViewDetails) {
            try {
                List<QuestionReviewItem> items = gradingService.getDetailedReview(attempt.getId());
                for (int i = 0; i < items.size(); i++) {
                    reviewListPanel.add(createQuestionCard(i + 1, items.get(i)));
                    reviewListPanel.add(Box.createVerticalStrut(10));
                }
            } catch (Exception ex) {
                JLabel err = new JLabel("Error loading question breakdown: " + ex.getMessage());
                err.setForeground(UITheme.DANGER);
                reviewListPanel.add(err);
            }
        } else {
            ModernCard withheldCard = new ModernCard();
            withheldCard.setLayout(new BoxLayout(withheldCard, BoxLayout.Y_AXIS));
            withheldCard.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

            JLabel lockLabel = new JLabel("Detailed Answers Withheld by Exam Creator");
            lockLabel.setFont(UITheme.FONT_SUBTITLE);
            lockLabel.setForeground(UITheme.WARNING);
            lockLabel.setAlignmentX(CENTER_ALIGNMENT);

            JLabel subLock = new JLabel("The instructor has set results to release at a later date. Please check back soon.");
            subLock.setFont(UITheme.FONT_BODY);
            subLock.setForeground(UITheme.TEXT_SECONDARY);
            subLock.setAlignmentX(CENTER_ALIGNMENT);

            withheldCard.add(lockLabel);
            withheldCard.add(Box.createVerticalStrut(8));
            withheldCard.add(subLock);
            reviewListPanel.add(withheldCard);
        }

        JScrollPane scrollPane = new JScrollPane(reviewListPanel);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        // Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        ModernButton closeBtn = new ModernButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);

        contentPane.add(summaryCard, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private ModernCard createSummaryCard() {
        ModernCard card = new ModernCard();
        card.setLayout(new BorderLayout(14, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Left info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(exam != null ? exam.getTitle() : "Exam Results");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.ACCENT_CYAN);

        String candidate = attempt.getStudentName() != null ? attempt.getStudentName() : "Student";
        JLabel candidateLbl = new JLabel("Candidate: " + candidate + " (" + attempt.getStatus() + ")");
        candidateLbl.setFont(UITheme.FONT_BODY);
        candidateLbl.setForeground(UITheme.TEXT_SECONDARY);

        leftPanel.add(titleLbl);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(candidateLbl);

        // Right metrics
        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        metricsPanel.setOpaque(false);

        boolean passed = exam != null && attempt.getPercentage() >= exam.getPassPercentage();
        Badge statusBadge = passed ? Badge.success("PASSED") : Badge.danger("FAILED");

        JPanel scoreBox = new JPanel();
        scoreBox.setLayout(new BoxLayout(scoreBox, BoxLayout.Y_AXIS));
        scoreBox.setOpaque(false);

        JLabel scoreLbl = new JLabel(String.format("%.1f / %.1f Marks", attempt.getScore(), attempt.getTotalMarks()));
        scoreLbl.setFont(UITheme.FONT_SUBTITLE);
        scoreLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel pctLbl = new JLabel(String.format("%.1f%% Score", attempt.getPercentage()));
        pctLbl.setFont(UITheme.FONT_BODY);
        pctLbl.setForeground(passed ? UITheme.SUCCESS : UITheme.DANGER);

        scoreBox.add(scoreLbl);
        scoreBox.add(pctLbl);

        metricsPanel.add(scoreBox);
        metricsPanel.add(statusBadge);

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(metricsPanel, BorderLayout.EAST);
        return card;
    }

    private JPanel createQuestionCard(int index, QuestionReviewItem item) {
        ModernCard card = new ModernCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setCardBackground(UITheme.BG_CARD);

        // Header: Question index, marks awarded, badge
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel qNum = new JLabel("Question " + index + " (" + item.getMarksAwarded() + " / " + item.getQuestion().getMarks() + " marks)");
        qNum.setFont(UITheme.FONT_BOLD);
        qNum.setForeground(item.isCorrect() ? UITheme.SUCCESS : (item.getSelectedOptionId() == null ? UITheme.TEXT_MUTED : UITheme.DANGER));

        Badge resultBadge = item.isCorrect()
                ? Badge.success("CORRECT")
                : (item.getSelectedOptionId() == null ? Badge.warning("UNANSWERED") : Badge.danger("INCORRECT"));

        header.add(qNum, BorderLayout.WEST);
        header.add(resultBadge, BorderLayout.EAST);

        // Question text
        JLabel qText = new JLabel("<html><body style='width: 650px;'>" + item.getQuestion().getQuestionText() + "</body></html>");
        qText.setFont(UITheme.FONT_BODY);
        qText.setForeground(UITheme.TEXT_PRIMARY);

        card.add(header);
        card.add(Box.createVerticalStrut(6));
        card.add(qText);
        card.add(Box.createVerticalStrut(10));

        // Options
        char optChar = 'A';
        for (AnswerOption opt : item.getOptions()) {
            boolean isSelectedByStudent = (item.getSelectedOptionId() != null && item.getSelectedOptionId() == opt.getId());
            boolean isCorrectOpt = opt.isCorrect();

            StringBuilder optDisplay = new StringBuilder();
            optDisplay.append("<b>").append(optChar).append(".</b> ").append(opt.getOptionText());

            Color optBg = UITheme.BG_INPUT;
            Color optFg = UITheme.TEXT_PRIMARY;

            if (isSelectedByStudent && isCorrectOpt) {
                optDisplay.append(" <span style='color: #10B981;'>✓ (Your Correct Answer)</span>");
                optBg = new Color(0x10, 0xB9, 0x81, 35);
            } else if (isSelectedByStudent && !isCorrectOpt) {
                optDisplay.append(" <span style='color: #EF4444;'>✗ (Your Incorrect Answer)</span>");
                optBg = new Color(0xEF, 0x44, 0x44, 35);
            } else if (!isSelectedByStudent && isCorrectOpt) {
                optDisplay.append(" <span style='color: #38BDF8;'>✓ (Correct Answer)</span>");
                optBg = new Color(0x38, 0xBD, 0xF8, 25);
            }

            JPanel optRow = new JPanel(new BorderLayout());
            optRow.setBackground(optBg);
            optRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isSelectedByStudent ? (isCorrectOpt ? UITheme.SUCCESS : UITheme.DANGER) : UITheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));

            JLabel optLabel = new JLabel("<html>" + optDisplay.toString() + "</html>");
            optLabel.setFont(UITheme.FONT_BODY);
            optLabel.setForeground(optFg);
            optRow.add(optLabel, BorderLayout.CENTER);

            card.add(optRow);
            card.add(Box.createVerticalStrut(4));
            optChar++;
        }

        // Explanation if available
        if (item.getQuestion().getExplanation() != null && !item.getQuestion().getExplanation().trim().isEmpty()) {
            card.add(Box.createVerticalStrut(4));
            JLabel exp = new JLabel("<html><i>Explanation: " + item.getQuestion().getExplanation() + "</i></html>");
            exp.setFont(UITheme.FONT_SMALL);
            exp.setForeground(UITheme.TEXT_MUTED);
            card.add(exp);
        }

        return card;
    }
}
