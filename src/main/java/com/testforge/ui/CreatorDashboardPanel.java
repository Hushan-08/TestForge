package com.testforge.ui;

import com.testforge.model.Exam;
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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modern dashboard for Exam Creators to author exams, manage questions,
 * monitor attempts, and inspect scores with clear action buttons and stats.
 */
public class CreatorDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ExamService examService;
    private final GradingService gradingService;

    private JTabbedPane tabbedPane;
    private JTable examsTable;
    private DefaultTableModel tableModel;
    private List<Exam> examList = new ArrayList<>();

    // Quick Stats Cards
    private JLabel totalExamsStat;
    private JLabel totalQuestionsStat;
    private JLabel totalSubmissionsStat;

    // Create Exam Form Fields
    private ModernTextField titleField;
    private JTextArea descArea;
    private ModernTextField accessCodeField;
    private JSpinner durationSpinner;
    private JSpinner passPctSpinner;
    private JCheckBox publishNowCheck;
    private JCheckBox releaseResultsCheck;

    public CreatorDashboardPanel(MainFrame mainFrame, ExamService examService, GradingService gradingService) {
        this.mainFrame = mainFrame;
        this.examService = examService;
        this.gradingService = gradingService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        // Top Banner + Stats Strip
        JPanel topSection = new JPanel(new BorderLayout(12, 12));
        topSection.setOpaque(false);

        // Header Title
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel("Exam Creator Studio");
        titleLabel.setFont(UITheme.FONT_BRAND);
        titleLabel.setForeground(UITheme.ACCENT_CYAN);

        JLabel subLabel = new JLabel("Author exams, randomize question sequences, import CSVs, and inspect results.");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subLabel);

        // Stats Counters Row
        JPanel statsRow = createStatsRow();

        topSection.add(titleBox, BorderLayout.WEST);
        topSection.add(statsRow, BorderLayout.EAST);

        // Tabs: "Manage My Exams" and "Create New Exam"
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.BG_CARD);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);

        tabbedPane.addTab("📋  Manage My Exams", createExamsListPanel());
        tabbedPane.addTab("✨  Create New Exam", createNewExamPanel());

        add(topSection, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        row.setOpaque(false);

        totalExamsStat = new JLabel("0");
        totalQuestionsStat = new JLabel("0");
        totalSubmissionsStat = new JLabel("0");

        row.add(createMiniStatCard("Total Exams", totalExamsStat, UITheme.ACCENT_CYAN));
        row.add(createMiniStatCard("Total Questions", totalQuestionsStat, UITheme.SUCCESS));
        row.add(createMiniStatCard("Total Submissions", totalSubmissionsStat, UITheme.WARNING));

        return row;
    }

    private JPanel createMiniStatCard(String label, JLabel valueLabel, Color accent) {
        ModernCard card = new ModernCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(135, 62));
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

    private JPanel createExamsListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Table
        String[] columns = {"#", "Exam Title", "Access Code", "Duration", "Questions", "Total Marks", "Status", "Results"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examsTable = new JTable(tableModel);
        examsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examsTable.setRowHeight(34);
        examsTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        examsTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        examsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        examsTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        examsTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        examsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        examsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        examsTable.getColumnModel().getColumn(7).setPreferredWidth(95);

        // Center-align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        examsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        examsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        examsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        examsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        examsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        examsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        examsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(examsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolbar.setOpaque(false);

        ModernButton newExamShortcutBtn = ModernButton.createSuccess("➕  New Exam");
        newExamShortcutBtn.setPreferredSize(new Dimension(130, 38));
        newExamShortcutBtn.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        ModernButton manageQuestionsBtn = ModernButton.createPrimary("📝  Question Builder");
        manageQuestionsBtn.setPreferredSize(new Dimension(160, 38));
        manageQuestionsBtn.addActionListener(e -> openQuestionEditorForSelected());

        ModernButton viewSubmissionsBtn = ModernButton.createSecondary("📊  Submissions");
        viewSubmissionsBtn.setPreferredSize(new Dimension(135, 38));
        viewSubmissionsBtn.addActionListener(e -> openSubmissionsForSelected());

        ModernButton togglePublishBtn = ModernButton.createSecondary("🔄  Publish / Draft");
        togglePublishBtn.setPreferredSize(new Dimension(145, 38));
        togglePublishBtn.addActionListener(e -> toggleSelectedPublish());

        ModernButton deleteBtn = ModernButton.createDanger("🗑  Delete");
        deleteBtn.setPreferredSize(new Dimension(100, 38));
        deleteBtn.addActionListener(e -> deleteSelectedExam());

        ModernButton refreshBtn = ModernButton.createSecondary("Refresh");
        refreshBtn.setPreferredSize(new Dimension(90, 38));
        refreshBtn.addActionListener(e -> refreshExams());

        toolbar.add(newExamShortcutBtn);
        toolbar.add(manageQuestionsBtn);
        toolbar.add(viewSubmissionsBtn);
        toolbar.add(togglePublishBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNewExamPanel() {
        // Wrapped in JScrollPane to guarantee responsive display on any screen resolution!
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);

        JPanel scrollContent = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        scrollContent.setOpaque(false);

        ModernCard card = new ModernCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(720, 560));
        card.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        card.setTopAccentColor(UITheme.ACCENT_CYAN);

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JLabel header = new JLabel("✨ Create New Examination");
        header.setFont(UITheme.FONT_TITLE);
        header.setForeground(UITheme.ACCENT_CYAN);

        JLabel subHeader = new JLabel("Fill in the details below and click Confirm to launch the question builder.");
        subHeader.setFont(UITheme.FONT_BODY);
        subHeader.setForeground(UITheme.TEXT_SECONDARY);

        cardHeader.add(header, BorderLayout.NORTH);
        cardHeader.add(subHeader, BorderLayout.SOUTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formGrid.add(createLabel("Exam Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        titleField = new ModernTextField();
        titleField.setToolTipText("e.g. Data Structures & Algorithms Midterm");
        titleField.addActionListener(e -> performCreateExam());
        formGrid.add(titleField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formGrid.add(createLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        descArea = new JTextArea(3, 30);
        descArea.setFont(UITheme.FONT_BODY);
        descArea.setBackground(UITheme.BG_INPUT);
        descArea.setForeground(UITheme.TEXT_PRIMARY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        formGrid.add(new JScrollPane(descArea), gbc);

        // Access Code with Generate button
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formGrid.add(createLabel("Secret Access Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;

        JPanel codePanel = new JPanel(new BorderLayout(8, 0));
        codePanel.setOpaque(false);
        accessCodeField = new ModernTextField();
        accessCodeField.setText(generateRandomAccessCode());
        accessCodeField.addActionListener(e -> performCreateExam());

        ModernButton genCodeBtn = ModernButton.createSecondary("🎲 Auto-Generate");
        genCodeBtn.setPreferredSize(new Dimension(145, 38));
        genCodeBtn.addActionListener(e -> accessCodeField.setText(generateRandomAccessCode()));

        codePanel.add(accessCodeField, BorderLayout.CENTER);
        codePanel.add(genCodeBtn, BorderLayout.EAST);
        formGrid.add(codePanel, gbc);

        // Duration & Passing %
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formGrid.add(createLabel("Duration (Minutes):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        durationSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        durationSpinner.setPreferredSize(new Dimension(120, 36));
        formGrid.add(durationSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formGrid.add(createLabel("Passing Mark (%):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        passPctSpinner = new JSpinner(new SpinnerNumberModel(50.0, 10.0, 100.0, 5.0));
        passPctSpinner.setPreferredSize(new Dimension(120, 36));
        formGrid.add(passPctSpinner, gbc);

        // Checkboxes
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        checkRow.setOpaque(false);

        publishNowCheck = new JCheckBox("Publish Immediately (Students can attempt)", false);
        publishNowCheck.setFont(UITheme.FONT_BODY);
        publishNowCheck.setForeground(UITheme.TEXT_PRIMARY);
        publishNowCheck.setOpaque(false);

        releaseResultsCheck = new JCheckBox("Release Results upon Student Submission", true);
        releaseResultsCheck.setFont(UITheme.FONT_BODY);
        releaseResultsCheck.setForeground(UITheme.TEXT_PRIMARY);
        releaseResultsCheck.setOpaque(false);

        checkRow.add(publishNowCheck);
        checkRow.add(releaseResultsCheck);
        formGrid.add(checkRow, gbc);

        // Prominent Confirmation Button Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        actionRow.setOpaque(false);

        ModernButton createBtn = ModernButton.createSuccess("🚀  CONFIRM & CREATE EXAM  (Press Enter)");
        createBtn.setFont(UITheme.FONT_SUBTITLE);
        createBtn.setPreferredSize(new Dimension(380, 48));
        createBtn.addActionListener(e -> performCreateExam());

        ModernButton cancelBtn = ModernButton.createSecondary("Cancel");
        cancelBtn.setPreferredSize(new Dimension(110, 48));
        cancelBtn.addActionListener(e -> tabbedPane.setSelectedIndex(0));

        actionRow.add(createBtn);
        actionRow.add(cancelBtn);

        card.add(cardHeader);
        card.add(Box.createVerticalStrut(14));
        card.add(formGrid);
        card.add(Box.createVerticalStrut(18));
        card.add(actionRow);

        scrollContent.add(card);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        outerPanel.add(scrollPane, BorderLayout.CENTER);
        return outerPanel;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private String generateRandomAccessCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random rng = new Random();
        StringBuilder sb = new StringBuilder("TF-");
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void refreshExams() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        try {
            examList = examService.getCreatorExams(user.getId());
            tableModel.setRowCount(0);

            int totalQuestions = 0;
            int totalSubmissions = 0;

            for (int i = 0; i < examList.size(); i++) {
                Exam exam = examList.get(i);
                totalQuestions += exam.getQuestionCount();

                try {
                    totalSubmissions += examService.getAttemptsForExam(exam.getId()).size();
                } catch (Exception ignored) {}

                tableModel.addRow(new Object[]{
                        i + 1,
                        exam.getTitle(),
                        exam.getAccessCode(),
                        exam.getDurationMinutes() + " mins",
                        exam.getQuestionCount(),
                        exam.getTotalMarks(),
                        exam.isPublished() ? "PUBLISHED" : "DRAFT",
                        exam.isReleaseResults() ? "RELEASED" : "HELD"
                });
            }

            // Update stats
            totalExamsStat.setText(String.valueOf(examList.size()));
            totalQuestionsStat.setText(String.valueOf(totalQuestions));
            totalSubmissionsStat.setText(String.valueOf(totalSubmissions));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load exams: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performCreateExam() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String title = titleField.getText().trim();
        String desc = descArea.getText().trim();
        String code = accessCodeField.getText().trim();
        int duration = (int) durationSpinner.getValue();
        double passPct = (double) passPctSpinner.getValue();
        boolean publish = publishNowCheck.isSelected();
        boolean release = releaseResultsCheck.isSelected();

        if (title.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an Exam Title and Access Code before confirming.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocusInWindow();
            return;
        }

        Exam exam = new Exam();
        exam.setCreatorId(user.getId());
        exam.setTitle(title);
        exam.setDescription(desc);
        exam.setAccessCode(code);
        exam.setDurationMinutes(duration);
        exam.setPassPercentage(passPct);
        exam.setPublished(publish);
        exam.setReleaseResults(release);

        try {
            Exam created = examService.createExam(exam);
            JOptionPane.showMessageDialog(this,
                    "🎉 Exam '" + created.getTitle() + "' successfully created with Code: " + created.getAccessCode() + "!\nNow add questions using the builder.",
                    "Exam Created Successfully", JOptionPane.INFORMATION_MESSAGE);

            // Clear inputs
            titleField.setText("");
            descArea.setText("");
            accessCodeField.setText(generateRandomAccessCode());
            publishNowCheck.setSelected(false);

            refreshExams();
            tabbedPane.setSelectedIndex(0);

            // Directly open question builder
            ExamEditorDialog dialog = new ExamEditorDialog((mainFrame), created, examService, this::refreshExams);
            dialog.setVisible(true);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create exam:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openQuestionEditorForSelected() {
        int row = examsTable.getSelectedRow();
        if (row < 0 || row >= examList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an exam from the table to manage questions.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Exam exam = examList.get(row);
        ExamEditorDialog dialog = new ExamEditorDialog((mainFrame), exam, examService, this::refreshExams);
        dialog.setVisible(true);
    }

    private void openSubmissionsForSelected() {
        int row = examsTable.getSelectedRow();
        if (row < 0 || row >= examList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an exam from the table to view submissions.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Exam exam = examList.get(row);
        StudentAttemptsDialog dialog = new StudentAttemptsDialog(mainFrame, exam, examService, gradingService);
        dialog.setVisible(true);
    }

    private void toggleSelectedPublish() {
        int row = examsTable.getSelectedRow();
        if (row < 0 || row >= examList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an exam to toggle publish status.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Exam exam = examList.get(row);
        boolean newStatus = !exam.isPublished();
        try {
            examService.togglePublish(exam.getId(), newStatus);
            exam.setPublished(newStatus);
            refreshExams();
            JOptionPane.showMessageDialog(this,
                    "Exam status changed to: " + (newStatus ? "PUBLISHED (Students can now enter)" : "DRAFT (Hidden from students)"),
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedExam() {
        int row = examsTable.getSelectedRow();
        if (row < 0 || row >= examList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an exam to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Exam exam = examList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete exam:\n\"" + exam.getTitle() + "\" (Code: " + exam.getAccessCode() + ")?\n\nThis will permanently delete all associated questions and student submission records.",
                "Confirm Exam Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                examService.deleteExam(exam.getId());
                JOptionPane.showMessageDialog(this, "Exam deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshExams();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete exam: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
