package com.testforge.model;

import java.time.LocalDateTime;

/**
 * Represents a single answer selected or saved by a student for a specific question during an attempt.
 */
public class StudentAnswer {
    private int id;
    private int attemptId;
    private int questionId;
    private Integer selectedOptionId; // Nullable if not selected or cleared
    private boolean isMarkedForReview;
    private LocalDateTime updatedAt;

    public StudentAnswer() {
    }

    public StudentAnswer(int id, int attemptId, int questionId, Integer selectedOptionId, boolean isMarkedForReview, LocalDateTime updatedAt) {
        this.id = id;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.isMarkedForReview = isMarkedForReview;
        this.updatedAt = updatedAt;
    }

    public StudentAnswer(int attemptId, int questionId, Integer selectedOptionId, boolean isMarkedForReview) {
        this(0, attemptId, questionId, selectedOptionId, isMarkedForReview, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public boolean isMarkedForReview() {
        return isMarkedForReview;
    }

    public void setMarkedForReview(boolean markedForReview) {
        isMarkedForReview = markedForReview;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
