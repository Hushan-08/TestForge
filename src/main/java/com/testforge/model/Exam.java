package com.testforge.model;

import java.time.LocalDateTime;

/**
 * Represents an exam created by an Exam Creator.
 */
public class Exam {
    private int id;
    private int creatorId;
    private String title;
    private String description;
    private String accessCode;
    private int durationMinutes;
    private double passPercentage;
    private boolean isPublished;
    private boolean releaseResults;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional display fields (joined from user or question counts)
    private String creatorName;
    private int questionCount;
    private int totalMarks;

    public Exam() {
        this.durationMinutes = 30;
        this.passPercentage = 50.0;
        this.isPublished = false;
        this.releaseResults = true;
    }

    public Exam(int id, int creatorId, String title, String description, String accessCode,
                int durationMinutes, double passPercentage, boolean isPublished, boolean releaseResults,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.accessCode = accessCode;
        this.durationMinutes = durationMinutes;
        this.passPercentage = passPercentage;
        this.isPublished = isPublished;
        this.releaseResults = releaseResults;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public boolean isReleaseResults() {
        return releaseResults;
    }

    public void setReleaseResults(boolean releaseResults) {
        this.releaseResults = releaseResults;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    @Override
    public String toString() {
        return title + " (" + accessCode + ")";
    }
}
