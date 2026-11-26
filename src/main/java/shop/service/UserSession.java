package shop.service;

import shop.model.User;

/**
 * Holds the user who is logged in to this console session (single user at a time).
 * Separated from AuthService so that session state has one place and one reason to change (SRP).
 */
public class UserSession {
    private User currentUser;

    void start(User user) {
        this.currentUser = user;
    }

    void end() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
