package com.testforge.model;

/**
 * Represents an answer option for a multiple-choice question.
 */
public class AnswerOption {
    private int id;
    private int questionId;
    private String optionText;
    private boolean isCorrect;
    private int optionOrder;

    public AnswerOption() {
    }

    public AnswerOption(int id, int questionId, String optionText, boolean isCorrect, int optionOrder) {
        this.id = id;
        this.questionId = questionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.optionOrder = optionOrder;
    }

    public AnswerOption(String optionText, boolean isCorrect, int optionOrder) {
        this(0, 0, optionText, isCorrect, optionOrder);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(int optionOrder) {
        this.optionOrder = optionOrder;
    }

    @Override
    public String toString() {
        return optionText + (isCorrect ? " [Correct]" : "");
    }
}
