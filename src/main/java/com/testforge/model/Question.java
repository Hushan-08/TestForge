package com.testforge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a multiple-choice question in an exam.
 */
public class Question {
    private int id;
    private int examId;
    private String questionText;
    private int marks;
    private String explanation;
    private LocalDateTime createdAt;
    private List<AnswerOption> options = new ArrayList<>();

    public Question() {
    }

    public Question(int id, int examId, String questionText, int marks, String explanation, LocalDateTime createdAt) {
        this.id = id;
        this.examId = examId;
        this.questionText = questionText;
        this.marks = marks;
        this.explanation = explanation;
        this.createdAt = createdAt;
    }

    public Question(int examId, String questionText, int marks, String explanation) {
        this(0, examId, questionText, marks, explanation, LocalDateTime.now());
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AnswerOption> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOption> options) {
        this.options = options;
    }

    public void addOption(AnswerOption option) {
        this.options.add(option);
    }

    public AnswerOption getCorrectOption() {
        for (AnswerOption opt : options) {
            if (opt.isCorrect()) {
                return opt;
            }
        }
        return null;
    }
}
