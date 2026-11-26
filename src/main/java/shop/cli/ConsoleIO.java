package shop.cli;

import shop.model.Money;

import java.math.BigDecimal;
import java.util.Scanner;

/** All console input/output helpers. Menus never touch Scanner/System.in directly. */
public class ConsoleIO {
    private final Scanner scanner = new Scanner(System.in);

    public void println(String text) {
        System.out.println(text);
    }

    public void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    /** Reads a trimmed line. At end of input a NoSuchElementException ends the application. */
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        String text = readLine(prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid whole number.");
        }
    }

    /** Blank input returns null. */
    public Integer readOptionalInt(String prompt) {
        String text = readLine(prompt);
        return text.isEmpty() ? null : parseInt(text);
    }

    public BigDecimal readMoney(String prompt, String fieldName) {
        return Money.parse(readLine(prompt), fieldName);
    }

    /** Blank input returns null. */
    public BigDecimal readOptionalMoney(String prompt, String fieldName) {
        String text = readLine(prompt);
        return text.isEmpty() ? null : Money.parse(text, fieldName);
    }

    /** Blank input returns null. */
    public Double readOptionalDouble(String prompt) {
        String text = readLine(prompt);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }
    }

    public boolean confirm(String prompt) {
        String answer = readLine(prompt + " (y/n): ").toLowerCase();
        return answer.equals("y") || answer.equals("yes");
    }

    /**
     * Asks for a 1-based choice in a list. Returns the 0-based index, or -1 when the user enters 0 / cancels.
     * Invalid numbers and out-of-range values are reported with a clear message.
     */
    public int chooseIndex(String prompt, int size) {
        int choice = readInt(prompt + " (0 to cancel): ");
        if (choice == 0) {
            return -1;
        }
        if (choice < 1 || choice > size) {
            throw new IllegalArgumentException("Invalid selection. Enter a number between 1 and " + size + ".");
        }
        return choice - 1;
    }

    /** Runs one menu action; business errors are shown as messages instead of crashing the program. */
    public void runSafely(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            println("Error: " + e.getMessage());
        }
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid whole number.");
        }
    }
}
