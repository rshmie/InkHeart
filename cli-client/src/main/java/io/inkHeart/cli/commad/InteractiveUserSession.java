package io.inkHeart.cli.commad;

import io.inkHeart.cli.auth.JournalService;
import io.inkHeart.cli.util.CLIMenu;
import io.inkHeart.cli.util.MessagePrinter;

import javax.crypto.SecretKey;
import java.net.http.HttpClient;
import java.util.Scanner;

public class InteractiveUserSession {
    private final JournalService journalService;
    private final Scanner scanner;

    // jwt token expiry handling - TO DO
    public InteractiveUserSession(String jwtToken, SecretKey encryptionKey) {
        this.journalService = new JournalService(encryptionKey, jwtToken, HttpClient.newHttpClient());
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        MessagePrinter.info("\n\nWelcome to your private journal. Let's get started.");
        while (running) {
            CLIMenu.showJournalMenu();
            String choice = scanner.nextLine();
            switch (choice.toLowerCase()) {
                case "1", "n", "new" -> journalService.createEntry();
                case "2", "l", "list" -> journalService.listEntries(); // You'll need to create this
               // case "3", "s", "search" -> journalService.searchEntries(); // And this
                case "5" -> {
                    running = false;
                    MessagePrinter.success("Logging you out. Goodbye!");
                }
                default -> MessagePrinter.error("Invalid option. Please try again.");
            }
        }
    }

}
