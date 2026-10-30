package com.testforge.util;

import com.testforge.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the current authenticated user session in memory.
 */
public class SessionManager {

    private static User currentUser = null;
    private static final List<SessionListener> listeners = new ArrayList<>();

    public interface SessionListener {
        void onSessionChanged(User user);
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
        for (SessionListener listener : listeners) {
            try {
                listener.onSessionChanged(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized void logout() {
        setCurrentUser(null);
    }

    public static synchronized void addListener(SessionListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(SessionListener listener) {
        listeners.remove(listener);
    }
}
