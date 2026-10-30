package com.testforge.service;

import com.testforge.dao.UserDao;
import com.testforge.model.User;
import com.testforge.util.PasswordUtil;
import com.testforge.util.ValidationUtil;

import java.sql.SQLException;

/**
 * Service managing user authentication, registration, and role checks.
 */
public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates user using username or email and password.
     */
    public User login(String identifier, String password) throws Exception {
        if (!ValidationUtil.isNonEmpty(identifier)) {
            throw new IllegalArgumentException("Username or Email is required.");
        }
        if (!ValidationUtil.isNonEmpty(password)) {
            throw new IllegalArgumentException("Password is required.");
        }

        User user;
        try {
            user = userDao.findByEmailOrUsername(identifier);
        } catch (SQLException e) {
            throw new Exception("Database error during login: " + e.getMessage(), e);
        }

        if (user == null) {
            throw new IllegalArgumentException("Invalid login credentials. User not found.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password. Please try again.");
        }

        return user;
    }

    /**
     * Registers a new user with BCrypt password hashing.
     */
    public User register(String username, String fullName, String email, String password, String role) throws Exception {
        if (!ValidationUtil.isNonEmpty(fullName)) {
            throw new IllegalArgumentException("Full Name cannot be empty.");
        }
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be 3-30 characters (letters, numbers, underscores).");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address (e.g. user@example.com).");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!"CREATOR".equalsIgnoreCase(role) && !"STUDENT".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Please select a valid role (Exam Creator or Student).");
        }

        try {
            if (userDao.existsByUsername(username)) {
                throw new IllegalArgumentException("Username '" + username + "' is already taken. Please choose another.");
            }
            if (userDao.existsByEmail(email)) {
                throw new IllegalArgumentException("An account with email '" + email + "' already exists. Please log in.");
            }

            String passwordHash = PasswordUtil.hashPassword(password);
            User user = new User(username, fullName, email, passwordHash, role);
            return userDao.createUser(user);

        } catch (SQLException e) {
            throw new Exception("Database error during registration: " + e.getMessage(), e);
        }
    }
}
