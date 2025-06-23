package io.inkHeart.cli.commad;

import com.fasterxml.jackson.core.type.TypeReference;
import io.inkHeart.cli.service.auth.RegisterService;
import io.inkHeart.cli.util.JsonUtil;
import io.inkHeart.cli.util.MessagePrinter;
import picocli.CommandLine;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "register", description = "Create a new InkHeart account.")
public class RegisterCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-e", "--email"}, description = "Your email address")
    private String email;

    @Override
    public Integer call() {
        Scanner scanner = new Scanner(System.in);

        if (email == null) {
            MessagePrinter.prompt("Enter your email address:");
            email = scanner.nextLine().trim();
        }

        MessagePrinter.prompt("Choose a strong password:");
        char[] passwordChars = readPasswordSafe(scanner);
        if (passwordChars == null) {
            MessagePrinter.error("Could not read your password.");
            return 1;
        }

        MessagePrinter.prompt("Confirm password:");
        char[] confirmPasswordChars = readPasswordSafe(scanner);
        if (!Arrays.equals(passwordChars, confirmPasswordChars)) {
            MessagePrinter.error("Passwords do not match. Please try again.");
            return 1;
        }

        MessagePrinter.info("Registering account for " + email + "...");

        try {
            var response = new RegisterService(HttpClient.newHttpClient()).handleSignUp(email, new String(passwordChars));
            if (response.statusCode() == 201) {
                MessagePrinter.success("Account created successfully! You can now log in.");
            } else {
                String errorMessage;
                try {
                    Map<String, String> errorMap = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<>() {});
                    errorMessage = errorMap.getOrDefault("error", "Registration failed!");
                } catch (Exception ex) {
                    errorMessage = "Registration failed. Status :" + response.statusCode();
                }
                MessagePrinter.error(errorMessage);
                return 1;
            }
        } catch (IOException | InterruptedException e) {
            MessagePrinter.error("We encountered an error while creating your account: " + e.getMessage());
            return 1;
        }

        return 0;
    }

    public static char[] readPasswordSafe(Scanner scanner) {
        char[] password = System.console() != null
                ? System.console().readPassword()
                : scanner.nextLine().toCharArray();
        if (password.length == 0) {
            return null;
        }
        return password;
    }
}

