package com.testforge.model;

import java.util.List;

/**
 * Model helper for rendering a graded question in results or review screens.
 */
public class QuestionReviewItem {
    private Question question;
    private List<AnswerOption> options;
    private Integer selectedOptionId;
    private boolean isCorrect;
    private int marksAwarded;
    private boolean isMarkedForReview;

    public QuestionReviewItem() {
    }

    public QuestionReviewItem(Question question, List<AnswerOption> options, Integer selectedOptionId,
                              boolean isCorrect, int marksAwarded, boolean isMarkedForReview) {
        this.question = question;
        this.options = options;
        this.selectedOptionId = selectedOptionId;
        this.isCorrect = isCorrect;
        this.marksAwarded = marksAwarded;
        this.isMarkedForReview = isMarkedForReview;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<AnswerOption> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOption> options) {
        this.options = options;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(int marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public boolean isMarkedForReview() {
        return isMarkedForReview;
    }

    public void setMarkedForReview(boolean markedForReview) {
        isMarkedForReview = markedForReview;
    }

    public AnswerOption getSelectedOption() {
        if (selectedOptionId == null || options == null) return null;
        for (AnswerOption opt : options) {
            if (opt.getId() == selectedOptionId) {
                return opt;
            }
        }
        return null;
    }

    public AnswerOption getCorrectOption() {
        if (options == null) return null;
        for (AnswerOption opt : options) {
            if (opt.isCorrect()) {
                return opt;
            }
        }
        return null;
    }
}
