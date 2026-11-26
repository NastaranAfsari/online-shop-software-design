package shop.cli;

import shop.model.User;
import shop.service.AuthService;

/** Login, registration and password recovery screens. */
public class AuthMenu {
    private final ConsoleIO io;
    private final AuthService authService;

    public AuthMenu(ConsoleIO io, AuthService authService) {
        this.io = io;
        this.authService = authService;
    }

    public void login() {
        String identifier = io.readLine("Email or phone: ");
        String password = io.readLine("Password: ");
        User user = authService.login(identifier, password);
        io.println("Welcome back, " + user.getFullName() + "!");
    }

    public void register() {
        io.println("(Provide an email or a phone number; press Enter to skip one of them)");
        String name = io.readLine("Full Name: ");
        String email = io.readLine("Email: ");
        String phone = io.readLine("Phone (09xxxxxxxxx): ");
        String password = io.readLine("Password (min 6 characters): ");
        authService.register(name, email, phone, password);
        io.println("Registration successful! Please login.");
    }

    public void forgotPassword() {
        io.println("\n--- Password Recovery ---");
        String identifier = io.readLine("Enter your registered email or phone: ");
        String code = authService.requestPasswordReset(identifier);
        io.println("[SIMULATION] A recovery code was sent to you: " + code);
        String enteredCode = io.readLine("Enter the recovery code: ");
        String newPassword = io.readLine("New password: ");
        String confirmation = io.readLine("Confirm new password: ");
        if (!newPassword.equals(confirmation)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        authService.resetPassword(identifier, enteredCode, newPassword);
        io.println("Password reset successfully! You can now login with your new password.");
    }

    public void logout() {
        authService.logout();
        io.println("Logged out successfully.");
    }
}
