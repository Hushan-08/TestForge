package com.testforge.service;

import com.testforge.dao.AttemptDao;
import com.testforge.dao.ExamDao;
import com.testforge.dao.QuestionDao;
import com.testforge.model.AnswerOption;
import com.testforge.model.Exam;
import com.testforge.model.ExamAttempt;
import com.testforge.model.Question;
import com.testforge.model.StudentAnswer;
import com.testforge.util.ValidationUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service managing exams, questions, and attempt lifecycle.
 */
public class ExamService {

    private final ExamDao examDao;
    private final QuestionDao questionDao;
    private final AttemptDao attemptDao;

    public ExamService() {
        this.examDao = new ExamDao();
        this.questionDao = new QuestionDao();
        this.attemptDao = new AttemptDao();
    }

    public ExamService(ExamDao examDao, QuestionDao questionDao, AttemptDao attemptDao) {
        this.examDao = examDao;
        this.questionDao = questionDao;
        this.attemptDao = attemptDao;
    }

    public Exam createExam(Exam exam) throws Exception {
        validateExam(exam, 0);
        try {
            return examDao.createExam(exam);
        } catch (SQLException e) {
            throw new Exception("Error creating exam: " + e.getMessage(), e);
        }
    }

    public boolean updateExam(Exam exam) throws Exception {
        validateExam(exam, exam.getId());
        try {
            return examDao.updateExam(exam);
        } catch (SQLException e) {
            throw new Exception("Error updating exam: " + e.getMessage(), e);
        }
    }

    public boolean deleteExam(int examId) throws Exception {
        try {
            return examDao.deleteExam(examId);
        } catch (SQLException e) {
            throw new Exception("Error deleting exam: " + e.getMessage(), e);
        }
    }

    public Exam getExamById(int examId) throws Exception {
        try {
            return examDao.findById(examId);
        } catch (SQLException e) {
            throw new Exception("Error retrieving exam: " + e.getMessage(), e);
        }
    }

    public Exam getExamByAccessCode(String code) throws Exception {
        if (!ValidationUtil.isNonEmpty(code)) {
            throw new IllegalArgumentException("Exam access code cannot be empty.");
        }
        try {
            return examDao.findByAccessCode(code.trim());
        } catch (SQLException e) {
            throw new Exception("Error searching exam: " + e.getMessage(), e);
        }
    }

    public List<Exam> getCreatorExams(int creatorId) throws Exception {
        try {
            return examDao.findByCreatorId(creatorId);
        } catch (SQLException e) {
            throw new Exception("Error fetching creator exams: " + e.getMessage(), e);
        }
    }

    public boolean togglePublish(int examId, boolean publish) throws Exception {
        try {
            return examDao.togglePublish(examId, publish);
        } catch (SQLException e) {
            throw new Exception("Error updating exam publish status: " + e.getMessage(), e);
        }
    }

    public boolean toggleReleaseResults(int examId, boolean release) throws Exception {
        try {
            return examDao.toggleReleaseResults(examId, release);
        } catch (SQLException e) {
            throw new Exception("Error updating result release setting: " + e.getMessage(), e);
        }
    }

    // Questions Management
    public Question addQuestion(Question question) throws Exception {
        validateQuestion(question);
        try {
            return questionDao.createQuestion(question);
        } catch (SQLException e) {
            throw new Exception("Error adding question: " + e.getMessage(), e);
        }
    }

    public boolean updateQuestion(Question question) throws Exception {
        validateQuestion(question);
        try {
            return questionDao.updateQuestion(question);
        } catch (SQLException e) {
            throw new Exception("Error updating question: " + e.getMessage(), e);
        }
    }

    public boolean deleteQuestion(int questionId) throws Exception {
        try {
            return questionDao.deleteQuestion(questionId);
        } catch (SQLException e) {
            throw new Exception("Error deleting question: " + e.getMessage(), e);
        }
    }

    public List<Question> getQuestionsForExam(int examId) throws Exception {
        try {
            return questionDao.findQuestionsByExamId(examId);
        } catch (SQLException e) {
            throw new Exception("Error loading exam questions: " + e.getMessage(), e);
        }
    }

    // Student Exam Flow & Attempt Setup
    /**
     * Prepares or resumes an exam attempt for a student.
     * Enforces single active attempt and prevents duplicate completed submissions.
     */
    public ExamAttempt startOrResumeExamAttempt(int examId, int studentId) throws Exception {
        Exam exam = examDao.findById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Exam not found.");
        }
        if (!exam.isPublished()) {
            throw new IllegalStateException("This exam is currently unpublished (draft) and cannot be attempted.");
        }

        // Check if student already submitted this exam
        if (attemptDao.hasCompletedAttempt(examId, studentId)) {
            throw new IllegalStateException("You have already submitted an attempt for this exam. Duplicate attempts are not permitted.");
        }

        // Check if an existing in-progress attempt is active
        ExamAttempt activeAttempt = attemptDao.findActiveAttempt(examId, studentId);
        if (activeAttempt != null) {
            return activeAttempt;
        }

        // Create new attempt with random seed
        int seed = new Random().nextInt(1_000_000);
        ExamAttempt newAttempt = new ExamAttempt();
        newAttempt.setExamId(examId);
        newAttempt.setStudentId(studentId);
        newAttempt.setQuestionOrderSeed(seed);

        return attemptDao.createAttempt(newAttempt);
    }

    /**
     * Starts a fresh exam attempt, bypassing duplicate completion checks for demo / viva evaluation.
     */
    public ExamAttempt startFreshExamAttempt(int examId, int studentId) throws Exception {
        Exam exam = examDao.findById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Exam not found.");
        }
        if (!exam.isPublished()) {
            throw new IllegalStateException("This exam is currently unpublished (draft) and cannot be attempted.");
        }

        int seed = new Random().nextInt(1_000_000);
        ExamAttempt newAttempt = new ExamAttempt();
        newAttempt.setExamId(examId);
        newAttempt.setStudentId(studentId);
        newAttempt.setQuestionOrderSeed(seed);

        return attemptDao.createAttempt(newAttempt);
    }

    /**
     * Retrieves randomized question and option copies for a student attempt.
     * Note: Original question and option entities in the DB are never altered.
     */
    public List<Question> getRandomizedQuestionsForAttempt(int examId, int attemptSeed) throws Exception {
        List<Question> questions = questionDao.findQuestionsByExamId(examId);
        if (questions.isEmpty()) {
            return questions;
        }

        // Shuffle questions with attempt-specific random seed
        Random rng = new Random(attemptSeed);
        List<Question> shuffledQuestions = new ArrayList<>(questions);
        Collections.shuffle(shuffledQuestions, rng);

        // For each question, shuffle its options with a derivative seed
        for (int i = 0; i < shuffledQuestions.size(); i++) {
            Question q = shuffledQuestions.get(i);
            List<AnswerOption> originalOpts = q.getOptions();
            if (originalOpts != null && originalOpts.size() > 1) {
                List<AnswerOption> shuffledOpts = new ArrayList<>(originalOpts);
                Collections.shuffle(shuffledOpts, new Random(attemptSeed + (i * 31L) + q.getId()));
                q.setOptions(shuffledOpts);
            }
        }

        return shuffledQuestions;
    }

    public void saveStudentAnswer(int attemptId, int questionId, Integer selectedOptionId, boolean isMarked) throws Exception {
        try {
            attemptDao.saveOrUpdateAnswer(attemptId, questionId, selectedOptionId, isMarked);
        } catch (SQLException e) {
            throw new Exception("Error saving student answer: " + e.getMessage(), e);
        }
    }

    public Map<Integer, StudentAnswer> getSavedAnswers(int attemptId) throws Exception {
        try {
            return attemptDao.getSavedAnswers(attemptId);
        } catch (SQLException e) {
            throw new Exception("Error loading saved answers: " + e.getMessage(), e);
        }
    }

    public List<ExamAttempt> getStudentAttempts(int studentId) throws Exception {
        try {
            return attemptDao.findAttemptsByStudentId(studentId);
        } catch (SQLException e) {
            throw new Exception("Error loading student attempt history: " + e.getMessage(), e);
        }
    }

    public List<ExamAttempt> getAttemptsForExam(int examId) throws Exception {
        try {
            return attemptDao.findAttemptsByExamId(examId);
        } catch (SQLException e) {
            throw new Exception("Error loading exam submissions: " + e.getMessage(), e);
        }
    }

    public ExamAttempt getAttemptById(int attemptId) throws Exception {
        try {
            return attemptDao.findAttemptById(attemptId);
        } catch (SQLException e) {
            throw new Exception("Error loading attempt: " + e.getMessage(), e);
        }
    }

    private void validateExam(Exam exam, int excludeExamId) throws Exception {
        if (!ValidationUtil.isNonEmpty(exam.getTitle())) {
            throw new IllegalArgumentException("Exam Title cannot be empty.");
        }
        if (!ValidationUtil.isNonEmpty(exam.getAccessCode())) {
            throw new IllegalArgumentException("Exam Access Code cannot be empty.");
        }
        if (exam.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Exam Duration must be greater than 0 minutes.");
        }
        if (exam.getPassPercentage() <= 0 || exam.getPassPercentage() > 100) {
            throw new IllegalArgumentException("Passing percentage must be between 1% and 100%.");
        }
        if (examDao.existsByAccessCode(exam.getAccessCode(), excludeExamId)) {
            throw new IllegalArgumentException("Access Code '" + exam.getAccessCode() + "' is already in use by another exam. Please choose a unique code.");
        }
    }

    private void validateQuestion(Question question) {
        if (!ValidationUtil.isNonEmpty(question.getQuestionText())) {
            throw new IllegalArgumentException("Question text cannot be empty.");
        }
        if (question.getMarks() <= 0) {
            throw new IllegalArgumentException("Marks per question must be at least 1.");
        }
        if (question.getOptions() == null || question.getOptions().size() < 2) {
            throw new IllegalArgumentException("A question must have at least 2 options (preferably 4).");
        }
        boolean hasCorrect = false;
        for (AnswerOption opt : question.getOptions()) {
            if (!ValidationUtil.isNonEmpty(opt.getOptionText())) {
                throw new IllegalArgumentException("Option text cannot be empty.");
            }
            if (opt.isCorrect()) {
                hasCorrect = true;
            }
        }
        if (!hasCorrect) {
            throw new IllegalArgumentException("Please mark at least one option as the correct answer.");
        }
    }
}
