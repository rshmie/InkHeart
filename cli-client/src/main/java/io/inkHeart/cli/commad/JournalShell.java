package io.inkHeart.cli.commad;

import io.inkHeart.cli.auth.JournalService;
import io.inkHeart.cli.utility.CLIMenu;
import io.inkHeart.cli.utility.MessagePrinter;

import java.util.Scanner;

public class JournalShell {
    private final Scanner scanner = new Scanner(System.in);
    private final JournalService journalService = new JournalService(); // Your service class

    public void start(String userEmail) {
        while (true) {
            CLIMenu.showJournalMenu(userEmail);
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> createNewEntry();
                case "2" -> viewAllEntries();
                case "3" -> searchEntries();
                case "4" -> deleteEntry();
                case "5" -> {
                    // implement logout
                    MessagePrinter.success("You have been logged out. Goodbye!");
                    return;
                }
                default -> MessagePrinter.error("Invalid choice. Please enter 1–5.");
            }
        }
    }

    private void createNewEntry() {
        MessagePrinter.prompt("Enter your journal title:");
        String title = scanner.nextLine().trim();
        MessagePrinter.prompt("Start writing your entry:");
        String content = scanner.nextLine().trim();

        boolean success = journalService.createEntry(title, content); // Stub
        if (success) {
            MessagePrinter.success("Entry saved successfully!");
        } else {
            MessagePrinter.error("Failed to save entry.");
        }
    }

    private void viewAllEntries() {
        var entries = journalService.listEntries(); // Stub
        if (entries.isEmpty()) {
            MessagePrinter.info("No entries found.");
        } else {
            entries.forEach(entry -> {
//                System.out.println("\n " + entry.getTitle());
//                System.out.println("    " + entry.getSnippet());
            });
        }
    }

    private void searchEntries() {
        MessagePrinter.prompt("Enter keyword to search:");
        String keyword = scanner.nextLine().trim();
        var results = journalService.search(keyword); // Stub
//        if (results.isEmpty()) {
//            MessagePrinter.info("No entries matched your search.");
//        } else {
//            results.forEach(entry -> {
//                System.out.println("\n🔎 " + entry.getTitle());
//                System.out.println("    " + entry.getSnippet());
//            });
//        }
    }

    private void deleteEntry() {
        MessagePrinter.prompt("Enter title of entry to delete:");
        String title = scanner.nextLine().trim();
        boolean deleted = journalService.delete(title); // Stub
        if (deleted) {
            MessagePrinter.success("Entry deleted.");
        } else {
            MessagePrinter.error("Entry not found.");
        }
    }
}
