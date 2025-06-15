package io.inkHeart.cli.commad;

import io.inkHeart.cli.service.auth.LoginService;
import io.inkHeart.cli.crypto.CryptoUtils;
import io.inkHeart.cli.dto.FinalLoginResponse;
import io.inkHeart.cli.util.MessagePrinter;
import picocli.CommandLine;

import java.util.Scanner;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "login", description = "Log in to your InkHeart account.")
public class LoginCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-e", "--email"}, description = "Your email address")
    private String email;

    @Override
    public Integer call() {
        Scanner scanner = new Scanner(System.in);

        if (email == null) {
            MessagePrinter.prompt("Enter your email address:");
            email = scanner.nextLine().trim();
        }

        MessagePrinter.prompt("Enter your master password:");
        char[] passwordChars = readPasswordSafe(scanner);
        if (passwordChars == null) return 1;
        String password = new String(passwordChars);

        MessagePrinter.info("Initialising secure login...");

        FinalLoginResponse loginResponse;
        try {
            loginResponse = new LoginService().handleLogin(email, password);
            if (loginResponse.jwtToken().isEmpty()) {
                MessagePrinter.error("Login failed. Please check your credentials and try again.");
                return 1;
            }
        } catch (Exception e) {
            MessagePrinter.error("Login failed: " + e.getMessage());
            return 1;
        }

        MessagePrinter.success("Login successful - Welcome back, " + email + "!");

        //
        // TODO: Here you would proceed to the post-login functionality
        // For example, show a new menu: [1] New Entry, [2] List Entries, [3] Logout
        //
        try {
            new InteractiveJournalUserSession(loginResponse.jwtToken(),
                    CryptoUtils.deriveKeyFromPassword(password)).start();
        } catch (Exception e) {
            MessagePrinter.error("Error while processing journal operation " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private char[] readPasswordSafe(Scanner scanner) {
        char[] password = System.console() != null
                ? System.console().readPassword()
                : scanner.nextLine().toCharArray();
        if (password.length == 0) {
            MessagePrinter.error("Could not read password.");
            return null;
        }
        return password;
    }
}

