package com.testforge.ui;

import com.testforge.model.AnswerOption;
import com.testforge.model.Exam;
import com.testforge.model.Question;
import com.testforge.service.CsvImportService;
import com.testforge.service.ExamService;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.ModernCard;
import com.testforge.ui.components.ModernTextField;
import com.testforge.ui.components.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for managing questions within an exam: adding, editing, deleting,
 * CSV batch importing, and Groq AI question generation.
 */
public class ExamEditorDialog extends JDialog {

    private final Exam exam;
    private final ExamService examService;
    private final CsvImportService csvImportService;
    private final Runnable onExamUpdated;

    private JTable questionTable;
    private DefaultTableModel tableModel;
    private List<Question> currentQuestions = new ArrayList<>();

    // Form inputs for Add/Edit
    private JTextArea questionArea;
    private ModernTextField optAField;
    private ModernTextField optBField;
    private ModernTextField optCField;
    private ModernTextField optDField;
    private JRadioButton radioA;
    private JRadioButton radioB;
    private JRadioButton radioC;
    private JRadioButton radioD;
    private JSpinner marksSpinner;
    private ModernTextField explanationField;

    private ModernButton saveQuestionBtn;
    private ModernButton cancelEditBtn;
    private Integer editingQuestionId = null;

    public ExamEditorDialog(JFrame parent, Exam exam, ExamService examService, Runnable onExamUpdated) {
        super(parent, "Exam Question Builder — " + exam.getTitle(), true);
        this.exam = exam;
        this.examService = examService;
        this.csvImportService = new CsvImportService();
        this.onExamUpdated = onExamUpdated;

        setSize(1000, 720);
        setLocationRelativeTo(parent);
        initUI();
        loadQuestions();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(14, 14));
        contentPane.setBackground(UITheme.BG_DARK);
        contentPane.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel titleLabel = new JLabel("Exam Question Builder: " + exam.getTitle());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.ACCENT_CYAN);

        JLabel infoLabel = new JLabel(String.format("Access Code: %s | Duration: %d mins | Pass Mark: %.0f%%",
                exam.getAccessCode(), exam.getDurationMinutes(), exam.getPassPercentage()));
        infoLabel.setFont(UITheme.FONT_BODY);
        infoLabel.setForeground(UITheme.TEXT_SECONDARY);

        topHeader.add(titleLabel, BorderLayout.NORTH);
        topHeader.add(infoLabel, BorderLayout.SOUTH);

        // Center split: Table on Left/Center, Form on Bottom or Side
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setOpaque(false);

        // Questions Table
        String[] columns = {"#", "Question", "Options", "Correct", "Marks", "Explanation"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        questionTable = new JTable(tableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(28);
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        questionTable.getColumnModel().getColumn(5).setPreferredWidth(160);

        JScrollPane tableScroll = new JScrollPane(questionTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        tableScroll.setPreferredSize(new Dimension(960, 250));

        // Question action toolbar
        JPanel tableToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        tableToolbar.setOpaque(false);

        ModernButton editBtn = ModernButton.createSecondary("Edit Selected");
        editBtn.addActionListener(e -> populateForEdit());

        ModernButton deleteBtn = ModernButton.createDanger("Delete Question");
        deleteBtn.addActionListener(e -> deleteSelectedQuestion());

        ModernButton importCsvBtn = ModernButton.createSecondary("Import from CSV");
        importCsvBtn.addActionListener(e -> importFromCsv());

        ModernButton groqAiBtn = new ModernButton("Generate via Groq AI", UITheme.PRIMARY_BLUE, UITheme.PRIMARY_HOVER);
        groqAiBtn.addActionListener(e -> openGroqAiDialog());

        tableToolbar.add(editBtn);
        tableToolbar.add(deleteBtn);
        tableToolbar.add(importCsvBtn);
        tableToolbar.add(groqAiBtn);

        JPanel tableSection = new JPanel(new BorderLayout(4, 4));
        tableSection.setOpaque(false);
        tableSection.add(tableToolbar, BorderLayout.NORTH);
        tableSection.add(tableScroll, BorderLayout.CENTER);

        // Bottom Form: Add / Edit Question Card
        ModernCard formCard = createQuestionFormCard();

        mainPanel.add(tableSection, BorderLayout.CENTER);
        mainPanel.add(formCard, BorderLayout.SOUTH);

        // Bottom Dialog Action
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        ModernButton doneBtn = new ModernButton("Finished / Done");
        doneBtn.addActionListener(e -> {
            if (onExamUpdated != null) onExamUpdated.run();
            dispose();
        });
        bottomBar.add(doneBtn);

        contentPane.add(topHeader, BorderLayout.NORTH);
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private ModernCard createQuestionFormCard() {
        ModernCard card = new ModernCard();
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel formTitle = new JLabel("Add / Edit Multiple-Choice Question");
        formTitle.setFont(UITheme.FONT_BOLD);
        formTitle.setForeground(UITheme.ACCENT_CYAN);
        card.add(formTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Question Text
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Question Text:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 3;
        questionArea = new JTextArea(2, 40);
        questionArea.setFont(UITheme.FONT_BODY);
        questionArea.setBackground(UITheme.BG_INPUT);
        questionArea.setForeground(UITheme.TEXT_PRIMARY);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        formGrid.add(new JScrollPane(questionArea), gbc);

        // Row 1: Option A & B
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Option A:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        optAField = new ModernTextField();
        formGrid.add(optAField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Option B:"), gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.5;
        optBField = new ModernTextField();
        formGrid.add(optBField, gbc);

        // Row 2: Option C & D
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Option C:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.5;
        optCField = new ModernTextField();
        formGrid.add(optCField, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Option D:"), gbc);

        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.5;
        optDField = new ModernTextField();
        formGrid.add(optDField, gbc);

        // Row 3: Correct Answer Radio & Marks
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Correct Option:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.5;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setOpaque(false);
        radioA = new JRadioButton("A", true);
        radioB = new JRadioButton("B");
        radioC = new JRadioButton("C");
        radioD = new JRadioButton("D");
        radioA.setOpaque(false); radioB.setOpaque(false); radioC.setOpaque(false); radioD.setOpaque(false);
        radioA.setForeground(UITheme.TEXT_PRIMARY); radioB.setForeground(UITheme.TEXT_PRIMARY);
        radioC.setForeground(UITheme.TEXT_PRIMARY); radioD.setForeground(UITheme.TEXT_PRIMARY);

        ButtonGroup group = new ButtonGroup();
        group.add(radioA); group.add(radioB); group.add(radioC); group.add(radioD);
        radioPanel.add(radioA); radioPanel.add(radioB); radioPanel.add(radioC); radioPanel.add(radioD);
        formGrid.add(radioPanel, gbc);

        gbc.gridx = 2; gbc.gridy = 3; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Marks:"), gbc);

        gbc.gridx = 3; gbc.gridy = 3; gbc.weightx = 0.5;
        marksSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        marksSpinner.setPreferredSize(new Dimension(80, 32));
        formGrid.add(marksSpinner, gbc);

        // Row 4: Explanation
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formGrid.add(createFormLabel("Explanation:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; gbc.gridwidth = 3;
        explanationField = new ModernTextField();
        explanationField.setToolTipText("Optional explanation shown to student upon result release");
        formGrid.add(explanationField, gbc);

        card.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        formButtons.setOpaque(false);

        cancelEditBtn = ModernButton.createSecondary("Cancel");
        cancelEditBtn.setVisible(false);
        cancelEditBtn.addActionListener(e -> resetForm());

        saveQuestionBtn = ModernButton.createSuccess("💾  Save Question");
        saveQuestionBtn.setPreferredSize(new Dimension(160, 40));
        saveQuestionBtn.addActionListener(e -> saveQuestion());

        formButtons.add(cancelEditBtn);
        formButtons.add(saveQuestionBtn);
        card.add(formButtons, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createFormLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private void loadQuestions() {
        try {
            currentQuestions = examService.getQuestionsForExam(exam.getId());
            tableModel.setRowCount(0);
            for (int i = 0; i < currentQuestions.size(); i++) {
                Question q = currentQuestions.get(i);
                StringBuilder optsSummary = new StringBuilder();
                String correctLetter = "None";
                List<AnswerOption> opts = q.getOptions();
                char optChar = 'A';
                for (AnswerOption opt : opts) {
                    optsSummary.append(optChar).append(": ").append(opt.getOptionText()).append(" | ");
                    if (opt.isCorrect()) {
                        correctLetter = String.valueOf(optChar);
                    }
                    optChar++;
                }

                tableModel.addRow(new Object[]{
                        i + 1,
                        q.getQuestionText(),
                        optsSummary.toString(),
                        correctLetter,
                        q.getMarks(),
                        q.getExplanation() != null ? q.getExplanation() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveQuestion() {
        String qText = questionArea.getText().trim();
        String aText = optAField.getText().trim();
        String bText = optBField.getText().trim();
        String cText = optCField.getText().trim();
        String dText = optDField.getText().trim();
        int marks = (int) marksSpinner.getValue();
        String exp = explanationField.getText().trim();

        if (qText.isEmpty() || aText.isEmpty() || bText.isEmpty() || cText.isEmpty() || dText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter question text and all four options (A, B, C, D).",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Question q = new Question(exam.getId(), qText, marks, exp);
        q.addOption(new AnswerOption(aText, radioA.isSelected(), 1));
        q.addOption(new AnswerOption(bText, radioB.isSelected(), 2));
        q.addOption(new AnswerOption(cText, radioC.isSelected(), 3));
        q.addOption(new AnswerOption(dText, radioD.isSelected(), 4));

        try {
            if (editingQuestionId != null) {
                q.setId(editingQuestionId);
                examService.updateQuestion(q);
                JOptionPane.showMessageDialog(this, "Question updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                examService.addQuestion(q);
                JOptionPane.showMessageDialog(this, "Question added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            resetForm();
            loadQuestions();
            if (onExamUpdated != null) onExamUpdated.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save question: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateForEdit() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentQuestions.size()) {
            JOptionPane.showMessageDialog(this, "Please select a question from the table to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Question q = currentQuestions.get(selectedRow);
        editingQuestionId = q.getId();

        questionArea.setText(q.getQuestionText());
        marksSpinner.setValue(q.getMarks());
        explanationField.setText(q.getExplanation() != null ? q.getExplanation() : "");

        List<AnswerOption> opts = q.getOptions();
        optAField.setText(opts.size() > 0 ? opts.get(0).getOptionText() : "");
        optBField.setText(opts.size() > 1 ? opts.get(1).getOptionText() : "");
        optCField.setText(opts.size() > 2 ? opts.get(2).getOptionText() : "");
        optDField.setText(opts.size() > 3 ? opts.get(3).getOptionText() : "");

        if (opts.size() > 0 && opts.get(0).isCorrect()) radioA.setSelected(true);
        else if (opts.size() > 1 && opts.get(1).isCorrect()) radioB.setSelected(true);
        else if (opts.size() > 2 && opts.get(2).isCorrect()) radioC.setSelected(true);
        else if (opts.size() > 3 && opts.get(3).isCorrect()) radioD.setSelected(true);

        saveQuestionBtn.setText("Update Question");
        cancelEditBtn.setVisible(true);
    }

    private void deleteSelectedQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentQuestions.size()) {
            JOptionPane.showMessageDialog(this, "Please select a question to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Question q = currentQuestions.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this question?\n\n\"" + q.getQuestionText() + "\"",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                examService.deleteQuestion(q.getId());
                JOptionPane.showMessageDialog(this, "Question deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
                loadQuestions();
                if (onExamUpdated != null) onExamUpdated.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting question: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importFromCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Questions CSV File");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Spreadsheet (*.csv)", "csv"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                CsvImportService.ImportResult importResult = csvImportService.parseCsvFile(file, exam.getId());
                int successCount = 0;

                for (Question q : importResult.getValidQuestions()) {
                    examService.addQuestion(q);
                    successCount++;
                }

                StringBuilder msg = new StringBuilder();
                msg.append(String.format("CSV Import Finished:\n- Successfully imported: %d questions\n", successCount));
                if (importResult.getFailureCount() > 0) {
                    msg.append(String.format("- Encountered %d invalid rows:\n", importResult.getFailureCount()));
                    for (int i = 0; i < Math.min(5, importResult.getErrorMessages().size()); i++) {
                        msg.append("  * ").append(importResult.getErrorMessages().get(i)).append("\n");
                    }
                    if (importResult.getErrorMessages().size() > 5) {
                        msg.append("  * ...and ").append(importResult.getErrorMessages().size() - 5).append(" more errors.\n");
                    }
                }

                JOptionPane.showMessageDialog(this, msg.toString(),
                        importResult.getFailureCount() == 0 ? "CSV Import Success" : "CSV Import Summary",
                        importResult.getFailureCount() == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

                loadQuestions();
                if (onExamUpdated != null) onExamUpdated.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to read CSV file:\n" + ex.getMessage(),
                        "CSV Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openGroqAiDialog() {
        AiQuestionDialog aiDialog = new AiQuestionDialog(
                (JFrame) getParent(),
                exam.getId(),
                examService,
                () -> {
                    loadQuestions();
                    if (onExamUpdated != null) onExamUpdated.run();
                }
        );
        aiDialog.setVisible(true);
    }

    private void resetForm() {
        editingQuestionId = null;
        questionArea.setText("");
        optAField.setText("");
        optBField.setText("");
        optCField.setText("");
        optDField.setText("");
        radioA.setSelected(true);
        marksSpinner.setValue(1);
        explanationField.setText("");
        saveQuestionBtn.setText("Save Question");
        cancelEditBtn.setVisible(false);
    }
}
