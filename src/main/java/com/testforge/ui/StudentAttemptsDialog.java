package com.testforge.ui;

import com.testforge.model.Exam;
import com.testforge.model.ExamAttempt;
import com.testforge.service.ExamService;
import com.testforge.service.GradingService;
import com.testforge.ui.components.ModernButton;
import com.testforge.ui.components.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog displaying student attempts, scores, and submission records for an exam.
 */
public class StudentAttemptsDialog extends JDialog {

    private final Exam exam;
    private final ExamService examService;
    private final GradingService gradingService;

    private JTable attemptsTable;
    private DefaultTableModel tableModel;
    private List<ExamAttempt> attemptsList = new ArrayList<>();
    private JLabel statusReleaseLabel;

    public StudentAttemptsDialog(JFrame parent, Exam exam, ExamService examService, GradingService gradingService) {
        super(parent, "Submissions & Analytics — " + exam.getTitle(), true);
        this.exam = exam;
        this.examService = examService;
        this.gradingService = gradingService;

        setSize(950, 600);
        setLocationRelativeTo(parent);
        initUI();
        loadAttempts();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBackground(UITheme.BG_DARK);
        contentPane.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Submissions for: " + exam.getTitle());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.ACCENT_CYAN);

        statusReleaseLabel = new JLabel("Result Visibility: " + (exam.isReleaseResults() ? "RELEASED to Students" : "HELD by Creator"));
        statusReleaseLabel.setFont(UITheme.FONT_BODY);
        statusReleaseLabel.setForeground(exam.isReleaseResults() ? UITheme.SUCCESS : UITheme.WARNING);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statusReleaseLabel, BorderLayout.SOUTH);

        // Submissions Table
        String[] columns = {"#", "Student Name", "Email", "Score", "Total", "%", "Status", "Submitted At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attemptsTable = new JTable(tableModel);
        attemptsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attemptsTable.setRowHeight(28);
        attemptsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        attemptsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        attemptsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        attemptsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        attemptsTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        attemptsTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        attemptsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        attemptsTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(attemptsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);

        ModernButton inspectBtn = ModernButton.createSecondary("Inspect Answer Paper");
        inspectBtn.addActionListener(e -> inspectSelectedAttempt());

        ModernButton toggleReleaseBtn = new ModernButton(
                exam.isReleaseResults() ? "Hold Results from Students" : "Release Results to Students",
                UITheme.PRIMARY_BLUE, UITheme.PRIMARY_HOVER
        );
        toggleReleaseBtn.addActionListener(e -> {
            try {
                boolean newState = !exam.isReleaseResults();
                examService.toggleReleaseResults(exam.getId(), newState);
                exam.setReleaseResults(newState);
                statusReleaseLabel.setText("Result Visibility: " + (newState ? "RELEASED to Students" : "HELD by Creator"));
                statusReleaseLabel.setForeground(newState ? UITheme.SUCCESS : UITheme.WARNING);
                toggleReleaseBtn.setText(newState ? "Hold Results from Students" : "Release Results to Students");
                JOptionPane.showMessageDialog(this,
                        "Result visibility updated to: " + (newState ? "RELEASED" : "HELD"),
                        "Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        ModernButton refreshBtn = ModernButton.createSecondary("Refresh");
        refreshBtn.addActionListener(e -> loadAttempts());

        toolbar.add(inspectBtn);
        toolbar.add(toggleReleaseBtn);
        toolbar.add(refreshBtn);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        ModernButton closeBtn = new ModernButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);

        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(toolbar, BorderLayout.NORTH);
        southPanel.add(bottomBar, BorderLayout.SOUTH);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void loadAttempts() {
        try {
            attemptsList = examService.getAttemptsForExam(exam.getId());
            tableModel.setRowCount(0);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

            for (int i = 0; i < attemptsList.size(); i++) {
                ExamAttempt att = attemptsList.get(i);
                String submitDate = (att.getSubmitTime() != null)
                        ? att.getSubmitTime().format(dtf)
                        : (att.getStartTime() != null ? att.getStartTime().format(dtf) : "In Progress");

                String passStatus = att.getPercentage() >= exam.getPassPercentage() ? "PASSED" : "FAILED";
                if ("IN_PROGRESS".equalsIgnoreCase(att.getStatus())) {
                    passStatus = "IN PROGRESS";
                }

                tableModel.addRow(new Object[]{
                        i + 1,
                        att.getStudentName() != null ? att.getStudentName() : "Unknown",
                        att.getStudentEmail() != null ? att.getStudentEmail() : "-",
                        String.format("%.1f", att.getScore()),
                        String.format("%.1f", att.getTotalMarks()),
                        String.format("%.1f%%", att.getPercentage()),
                        passStatus,
                        submitDate
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading submissions: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inspectSelectedAttempt() {
        int row = attemptsTable.getSelectedRow();
        if (row < 0 || row >= attemptsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an attempt from the table to inspect.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ExamAttempt att = attemptsList.get(row);
        ExamResultsDialog dialog = new ExamResultsDialog((JFrame) getParent(), att, exam, gradingService, true);
        dialog.setVisible(true);
    }
}
