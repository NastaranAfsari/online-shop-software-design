package shop.service;

import shop.model.User;

/** Central access-control checks used by all services (authorization lives in the Service layer). */
final class Authorization {
    private Authorization() {
    }

    static User requireLoggedIn(User user) {
        if (user == null) {
            throw new SecurityException("You must log in first.");
        }
        return user;
    }

    static User requireAdmin(User user) {
        requireLoggedIn(user);
        if (!user.isAdmin()) {
            throw new SecurityException("Only an admin can perform this action.");
        }
        return user;
    }

    static User requireCustomer(User user) {
        requireLoggedIn(user);
        if (!user.isCustomer()) {
            throw new SecurityException("Only a customer can perform this action.");
        }
        return user;
    }
}
