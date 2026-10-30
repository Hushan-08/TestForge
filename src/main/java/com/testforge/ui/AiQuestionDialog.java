package com.testforge.ui;

import com.testforge.model.AnswerOption;
import com.testforge.model.Question;
import com.testforge.service.ExamService;
import com.testforge.service.GroqAiService;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.ModernCard;
import com.testforge.ui.components.ModernTextField;
import com.testforge.ui.components.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for generating multiple-choice questions using the Groq AI API.
 * Review panel allows creators to verify, edit, and approve questions before saving.
 */
public class AiQuestionDialog extends JDialog {

    private final int examId;
    private final ExamService examService;
    private final GroqAiService aiService;
    private final Runnable onQuestionsAdded;

    private JTextArea notesArea;
    private JSpinner countSpinner;
    private JComboBox<String> difficultyCombo;
    private ModernTextField apiKeyField;
    private ModernButton generateBtn;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private JPanel reviewPanel;
    private JScrollPane reviewScrollPane;
    private ModernButton saveSelectedBtn;
    private final List<QuestionReviewCard> generatedCards = new ArrayList<>();

    public AiQuestionDialog(JFrame parent, int examId, ExamService examService, Runnable onQuestionsAdded) {
        super(parent, "TestForge AI — Generate Questions (Groq)", true);
        this.examId = examId;
        this.examService = examService;
        this.aiService = new GroqAiService();
        this.onQuestionsAdded = onQuestionsAdded;

        setSize(850, 700);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBackground(UITheme.BG_DARK);
        contentPane.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("TestForge AI Question Generator");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.ACCENT_CYAN);

        JLabel subLabel = new JLabel("Paste study notes or syllabus text to synthesize 4-option MCQs using Groq AI.");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        // Input Configuration Panel (Card)
        ModernCard configCard = new ModernCard();
        configCard.setLayout(new BoxLayout(configCard, BoxLayout.Y_AXIS));
        configCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel notesLabel = new JLabel("Study Notes / Topic Material:");
        notesLabel.setFont(UITheme.FONT_BOLD);
        notesLabel.setForeground(UITheme.TEXT_PRIMARY);

        notesArea = new JTextArea(4, 50);
        notesArea.setFont(UITheme.FONT_BODY);
        notesArea.setBackground(UITheme.BG_INPUT);
        notesArea.setForeground(UITheme.TEXT_PRIMARY);
        notesArea.setCaretColor(UITheme.ACCENT_CYAN);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setText("Java 21 introduces Virtual Threads as part of Project Loom to deliver high-throughput lightweight concurrency. Unlike platform threads managed by the OS kernel, virtual threads are scheduled by the JVM in user mode on top of a pool of carrier threads. Pattern Matching for switch simplifies type queries and guarded clauses.");

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(800, 90));

        // Settings Row
        JPanel settingsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        settingsRow.setOpaque(false);

        settingsRow.add(createLabel("Questions:"));
        countSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        countSpinner.setPreferredSize(new Dimension(60, 32));
        settingsRow.add(countSpinner);

        settingsRow.add(createLabel("Difficulty:"));
        difficultyCombo = new JComboBox<>(new String[]{"Medium", "Easy", "Hard"});
        difficultyCombo.setPreferredSize(new Dimension(90, 32));
        settingsRow.add(difficultyCombo);

        settingsRow.add(createLabel("Groq API Key:"));
        String envKey = System.getenv("GROQ_API_KEY");
        apiKeyField = new ModernTextField(envKey != null ? envKey : "");
        apiKeyField.setPreferredSize(new Dimension(240, 32));
        apiKeyField.setToolTipText("Enter Groq API Key or set GROQ_API_KEY environment variable");
        settingsRow.add(apiKeyField);

        generateBtn = new ModernButton("Generate MCQs");
        generateBtn.setPreferredSize(new Dimension(140, 34));
        generateBtn.addActionListener(e -> startGeneration());
        settingsRow.add(generateBtn);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 20));

        statusLabel = new JLabel("");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel progressRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        progressRow.setOpaque(false);
        progressRow.add(progressBar);
        progressRow.add(statusLabel);

        configCard.add(notesLabel);
        configCard.add(Box.createVerticalStrut(4));
        configCard.add(notesScroll);
        configCard.add(Box.createVerticalStrut(6));
        configCard.add(settingsRow);
        configCard.add(progressRow);

        // Center: Review Section
        JPanel reviewContainer = new JPanel(new BorderLayout(8, 8));
        reviewContainer.setOpaque(false);

        JLabel reviewTitle = new JLabel("Generated Questions Review (Verify & Approve Before Saving)");
        reviewTitle.setFont(UITheme.FONT_BOLD);
        reviewTitle.setForeground(UITheme.TEXT_PRIMARY);

        reviewPanel = new JPanel();
        reviewPanel.setLayout(new BoxLayout(reviewPanel, BoxLayout.Y_AXIS));
        reviewPanel.setBackground(UITheme.BG_DARK);

        reviewScrollPane = new JScrollPane(reviewPanel);
        reviewScrollPane.getViewport().setBackground(UITheme.BG_DARK);
        reviewScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        reviewContainer.add(reviewTitle, BorderLayout.NORTH);
        reviewContainer.add(reviewScrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottomBar.setOpaque(false);

        saveSelectedBtn = ModernButton.createSuccess("Add Approved to Exam");
        saveSelectedBtn.setEnabled(false);
        saveSelectedBtn.addActionListener(e -> saveApprovedQuestions());

        ModernButton closeBtn = ModernButton.createSecondary("Close");
        closeBtn.addActionListener(e -> dispose());

        bottomBar.add(saveSelectedBtn);
        bottomBar.add(closeBtn);

        // Assemble Dialog
        JPanel centerPanel = new JPanel(new BorderLayout(8, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(configCard, BorderLayout.NORTH);
        centerPanel.add(reviewContainer, BorderLayout.CENTER);

        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    private void startGeneration() {
        String apiKey = apiKeyField.getText().trim();
        String notes = notesArea.getText().trim();
        int count = (int) countSpinner.getValue();
        String difficulty = (String) difficultyCombo.getSelectedItem();

        generateBtn.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Connecting to Groq AI...");
        reviewPanel.removeAll();
        generatedCards.clear();
        saveSelectedBtn.setEnabled(false);
        reviewPanel.revalidate();
        reviewPanel.repaint();

        SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Question> doInBackground() throws Exception {
                return aiService.generateQuestions(apiKey, notes, count, difficulty, examId);
            }

            @Override
            protected void done() {
                generateBtn.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    List<Question> questions = get();
                    displayGeneratedQuestions(questions);
                    statusLabel.setText("Generated " + questions.size() + " questions successfully.");
                    saveSelectedBtn.setEnabled(!questions.isEmpty());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText("Generation failed.");
                    JOptionPane.showMessageDialog(AiQuestionDialog.this,
                            cause.getMessage(),
                            "Groq AI Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void displayGeneratedQuestions(List<Question> questions) {
        reviewPanel.removeAll();
        generatedCards.clear();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            QuestionReviewCard card = new QuestionReviewCard(i + 1, q);
            generatedCards.add(card);
            reviewPanel.add(card);
            reviewPanel.add(Box.createVerticalStrut(8));
        }

        reviewPanel.revalidate();
        reviewPanel.repaint();
    }

    private void saveApprovedQuestions() {
        List<Question> approved = new ArrayList<>();
        for (QuestionReviewCard card : generatedCards) {
            if (card.isSelected()) {
                approved.add(card.buildUpdatedQuestion());
            }
        }

        if (approved.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please check at least one question to add to the exam.",
                    "No Questions Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int savedCount = 0;
        for (Question q : approved) {
            try {
                examService.addQuestion(q);
                savedCount++;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving question: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        JOptionPane.showMessageDialog(this,
                "Successfully added " + savedCount + " questions to the exam!",
                "Questions Added", JOptionPane.INFORMATION_MESSAGE);

        if (onQuestionsAdded != null) {
            onQuestionsAdded.run();
        }
        dispose();
    }

    /**
     * Card component representing a single editable AI-generated question during review.
     */
    private static class QuestionReviewCard extends ModernCard {
        private final JCheckBox includeCheckBox;
        private final JTextArea questionTextArea;
        private final ModernTextField optAText;
        private final ModernTextField optBText;
        private final ModernTextField optCText;
        private final ModernTextField optDText;
        private final JComboBox<String> correctCombo;
        private final ModernTextField explanationText;
        private final int examId;

        public QuestionReviewCard(int index, Question q) {
            this.examId = q.getExamId();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            setCardBackground(UITheme.BG_SURFACE);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            topRow.setOpaque(false);

            includeCheckBox = new JCheckBox("Include Question #" + index, true);
            includeCheckBox.setFont(UITheme.FONT_BOLD);
            includeCheckBox.setForeground(UITheme.ACCENT_CYAN);
            includeCheckBox.setOpaque(false);

            topRow.add(includeCheckBox);

            // Question Text
            questionTextArea = new JTextArea(q.getQuestionText(), 2, 45);
            questionTextArea.setFont(UITheme.FONT_BODY);
            questionTextArea.setBackground(UITheme.BG_INPUT);
            questionTextArea.setForeground(UITheme.TEXT_PRIMARY);
            questionTextArea.setLineWrap(true);
            questionTextArea.setWrapStyleWord(true);

            // Options
            List<AnswerOption> opts = q.getOptions();
            optAText = new ModernTextField(opts.size() > 0 ? opts.get(0).getOptionText() : "");
            optBText = new ModernTextField(opts.size() > 1 ? opts.get(1).getOptionText() : "");
            optCText = new ModernTextField(opts.size() > 2 ? opts.get(2).getOptionText() : "");
            optDText = new ModernTextField(opts.size() > 3 ? opts.get(3).getOptionText() : "");

            JPanel optsGrid = new JPanel(new java.awt.GridLayout(2, 2, 8, 6));
            optsGrid.setOpaque(false);
            optsGrid.add(createOptPanel("A: ", optAText));
            optsGrid.add(createOptPanel("B: ", optBText));
            optsGrid.add(createOptPanel("C: ", optCText));
            optsGrid.add(createOptPanel("D: ", optDText));

            // Bottom row: Correct option selector & explanation
            JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            bottomRow.setOpaque(false);

            JLabel corrLabel = new JLabel("Correct Option:");
            corrLabel.setFont(UITheme.FONT_BOLD);
            corrLabel.setForeground(UITheme.TEXT_PRIMARY);

            correctCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
            for (int i = 0; i < opts.size(); i++) {
                if (opts.get(i).isCorrect()) {
                    correctCombo.setSelectedIndex(i);
                    break;
                }
            }

            JLabel expLabel = new JLabel("Explanation:");
            expLabel.setFont(UITheme.FONT_BOLD);
            expLabel.setForeground(UITheme.TEXT_PRIMARY);

            explanationText = new ModernTextField(q.getExplanation() != null ? q.getExplanation() : "");
            explanationText.setPreferredSize(new Dimension(300, 30));

            bottomRow.add(corrLabel);
            bottomRow.add(correctCombo);
            bottomRow.add(expLabel);
            bottomRow.add(explanationText);

            add(topRow);
            add(Box.createVerticalStrut(4));
            add(new JScrollPane(questionTextArea));
            add(Box.createVerticalStrut(6));
            add(optsGrid);
            add(Box.createVerticalStrut(4));
            add(bottomRow);
        }

        private JPanel createOptPanel(String label, ModernTextField field) {
            JPanel p = new JPanel(new BorderLayout(4, 0));
            p.setOpaque(false);
            JLabel l = new JLabel(label);
            l.setFont(UITheme.FONT_BOLD);
            l.setForeground(UITheme.TEXT_SECONDARY);
            p.add(l, BorderLayout.WEST);
            p.add(field, BorderLayout.CENTER);
            return p;
        }

        public boolean isSelected() {
            return includeCheckBox.isSelected();
        }

        public Question buildUpdatedQuestion() {
            Question q = new Question(examId, questionTextArea.getText().trim(), 1, explanationText.getText().trim());
            int correctIdx = correctCombo.getSelectedIndex();
            q.addOption(new AnswerOption(optAText.getText().trim(), correctIdx == 0, 1));
            q.addOption(new AnswerOption(optBText.getText().trim(), correctIdx == 1, 2));
            q.addOption(new AnswerOption(optCText.getText().trim(), correctIdx == 2, 3));
            q.addOption(new AnswerOption(optDText.getText().trim(), correctIdx == 3, 4));
            return q;
        }
    }
}
