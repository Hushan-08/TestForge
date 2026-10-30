package com.testforge.model;

import java.time.LocalDateTime;

/**
 * Represents a student's attempt at an exam.
 */
public class ExamAttempt {
    private int id;
    private int examId;
    private int studentId;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private double score;
    private double totalMarks;
    private double percentage;
    private String status; // "IN_PROGRESS", "SUBMITTED", "TIMED_OUT"
    private int questionOrderSeed;

    // Additional display fields
    private String studentName;
    private String studentEmail;
    private String examTitle;
    private int durationMinutes;

    public ExamAttempt() {
        this.status = "IN_PROGRESS";
    }

    public ExamAttempt(int id, int examId, int studentId, LocalDateTime startTime, LocalDateTime submitTime,
                       double score, double totalMarks, double percentage, String status, int questionOrderSeed) {
        this.id = id;
        this.examId = examId;
        this.studentId = studentId;
        this.startTime = startTime;
        this.submitTime = submitTime;
        this.score = score;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.status = status;
        this.questionOrderSeed = questionOrderSeed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuestionOrderSeed() {
        return questionOrderSeed;
    }

    public void setQuestionOrderSeed(int questionOrderSeed) {
        this.questionOrderSeed = questionOrderSeed;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isCompleted() {
        return "SUBMITTED".equalsIgnoreCase(status) || "TIMED_OUT".equalsIgnoreCase(status);
    }
}
