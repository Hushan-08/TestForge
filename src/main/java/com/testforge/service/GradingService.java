package com.testforge.service;

import com.testforge.dao.AttemptDao;
import com.testforge.dao.ExamDao;
import com.testforge.dao.QuestionDao;
import com.testforge.model.AnswerOption;
import com.testforge.model.ExamAttempt;
import com.testforge.model.Question;
import com.testforge.model.QuestionReviewItem;
import com.testforge.model.StudentAnswer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service managing server/database-side exam grading, scoring calculation,
 * and detailed result generation. Scores are never calculated solely by the client UI.
 */
public class GradingService {

    private final AttemptDao attemptDao;
    private final QuestionDao questionDao;
    private final ExamDao examDao;

    public GradingService() {
        this.attemptDao = new AttemptDao();
        this.questionDao = new QuestionDao();
        this.examDao = new ExamDao();
    }

    public GradingService(AttemptDao attemptDao, QuestionDao questionDao, ExamDao examDao) {
        this.attemptDao = attemptDao;
        this.questionDao = questionDao;
        this.examDao = examDao;
    }

    /**
     * Grades the attempt against database ground truth.
     *
     * @param attemptId ID of the attempt to submit and grade
     * @param status    "SUBMITTED" or "TIMED_OUT"
     * @return Graded ExamAttempt
     */
    public synchronized ExamAttempt calculateAndSubmitExam(int attemptId, String status) throws Exception {
        ExamAttempt attempt = attemptDao.findAttemptById(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found with ID: " + attemptId);
        }

        if (attempt.isCompleted()) {
            return attempt; // Already graded and submitted
        }

        // Fetch all questions and their ground truth correct options from database
        List<Question> questions = questionDao.findQuestionsByExamId(attempt.getExamId());
        Map<Integer, StudentAnswer> savedAnswers = attemptDao.getSavedAnswers(attemptId);

        double totalMarks = 0.0;
        double obtainedScore = 0.0;

        for (Question q : questions) {
            totalMarks += q.getMarks();
            AnswerOption correctOpt = q.getCorrectOption();
            StudentAnswer studentAns = savedAnswers.get(q.getId());

            if (correctOpt != null && studentAns != null && studentAns.getSelectedOptionId() != null) {
                if (correctOpt.getId() == studentAns.getSelectedOptionId()) {
                    obtainedScore += q.getMarks();
                }
            }
        }

        double percentage = (totalMarks > 0) ? (obtainedScore / totalMarks) * 100.0 : 0.0;

        boolean updated = attemptDao.finalizeAttempt(attemptId, obtainedScore, totalMarks, percentage, status);
        if (!updated) {
            throw new SQLException("Failed to finalize exam attempt.");
        }

        return attemptDao.findAttemptById(attemptId);
    }

    /**
     * Builds a detailed question-by-question review for this attempt.
     */
    public List<QuestionReviewItem> getDetailedReview(int attemptId) throws Exception {
        ExamAttempt attempt = attemptDao.findAttemptById(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found with ID: " + attemptId);
        }

        List<Question> questions = questionDao.findQuestionsByExamId(attempt.getExamId());
        Map<Integer, StudentAnswer> savedAnswers = attemptDao.getSavedAnswers(attemptId);
        List<QuestionReviewItem> reviewItems = new ArrayList<>();

        for (Question q : questions) {
            AnswerOption correctOpt = q.getCorrectOption();
            StudentAnswer studentAns = savedAnswers.get(q.getId());

            Integer selectedOptId = (studentAns != null) ? studentAns.getSelectedOptionId() : null;
            boolean marked = (studentAns != null) && studentAns.isMarkedForReview();

            boolean isCorrect = false;
            int marksAwarded = 0;

            if (correctOpt != null && selectedOptId != null && correctOpt.getId() == selectedOptId) {
                isCorrect = true;
                marksAwarded = q.getMarks();
            }

            QuestionReviewItem item = new QuestionReviewItem(
                    q,
                    q.getOptions(),
                    selectedOptId,
                    isCorrect,
                    marksAwarded,
                    marked
            );
            reviewItems.add(item);
        }

        return reviewItems;
    }
}
