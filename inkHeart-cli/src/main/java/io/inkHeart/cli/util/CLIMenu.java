package io.inkHeart.cli.util;

import io.inkHeart.cli.dto.CreateEntryPromptResult;
import io.inkHeart.cli.dto.DecryptedJournalEntryResponse;
import io.inkHeart.cli.dto.DecryptedJournalGetResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static io.inkHeart.cli.service.JournalService.DATE_TIME_FORMATTER;
import static io.inkHeart.cli.service.JournalService.INPUT_DATE_TIME_FORMATTER;
import static io.inkHeart.cli.util.MessagePrinter.center;

public class CLIMenu {
    public static void printWelcomeMenu() {
        // Clear screen for a cleaner look (optional, might not work on all terminals)
        // System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println();
        MessagePrinter.divider();
        MessagePrinter.title(center("Welcome to InkHeart CLI", 81));
        MessagePrinter.divider();
        System.out.println();
        System.out.println("A secure, encrypted journal where your thoughts stay truly yours.");
        System.out.println();
        MessagePrinter.showOption("[1]", "Register a new account");
        MessagePrinter.showOption("[2]", "Log in to your existing account");
        MessagePrinter.showOption("[3]", "Exit");
        System.out.println();
        MessagePrinter.divider();
        System.out.println();
        MessagePrinter.prompt("Enter your choice:");

    }

    public static void showJournalMenu() {
        System.out.println();
        MessagePrinter.divider();
        MessagePrinter.title("--------- [ Secure Journal Dashboard ] ---------");
        System.out.println();
        MessagePrinter.showOption("[1]", "New Entry");
        MessagePrinter.showOption("[2]", "My Journal");
        MessagePrinter.showOption("[3]", "Logout");
        MessagePrinter.divider();
        System.out.println();
        MessagePrinter.prompt("Select an action [1-3]: ");
    }

    public static void showMyJournalMenu() {
        System.out.println();
        MessagePrinter.divider();
        MessagePrinter.title("---------- [ My Journal ] ----------");
        System.out.println(" Find an entry to view, edit, or delete.");
        System.out.println("Choose how you'd like to find it: ");
        MessagePrinter.divider();
        System.out.println();
        MessagePrinter.showOption("[1]", "List Recent Entries\n  -> Shows the 10 most recent entry titles.\n");
        MessagePrinter.showOption("[2]", "Filter by Date Range\n  -> Finds all entries between two specific dates.\n");
        MessagePrinter.showOption("[3]", "Full-Text Search\n  -> Search for a word, mood or tag across your entire journal.\n");
        MessagePrinter.showOption("[4]", "Back to Main Menu\n");
        System.out.println();

    }


    // perhaps move into different class ?
    public static void journalEntryActionMenu() {
        System.out.println(" What would you like to do?");
        System.out.println("-> Format: <ID> <Action> (e.g. 105 V or B)");
        MessagePrinter.formatPair("   - [V]", " ", "View Entry");
        MessagePrinter.formatPair("   - [D]", " ", "Delete Entry");
        MessagePrinter.formatPair("   - [E]", " ", "Edit Entry");
        MessagePrinter.formatPair("   - [B]", " ", "Back to Previous Menu");
    }

    public static void showJournalEntriesTable(List<DecryptedJournalEntryResponse> entries) {
        if (entries.isEmpty()) {
            MessagePrinter.info("You have no journal entries yet.");
            return;
        }

        MessagePrinter.divider();
        System.out.printf("%-5s  | %-30s | %-25s | %-25s%n", "ID", "Title", "Created At", "Updated At");
        MessagePrinter.divider();

        for (DecryptedJournalEntryResponse entry : entries) {
            System.out.printf("%-5d | %-30s | %-25s | %-25s%n", entry.id(), truncate(entry.title(), 25), entry.createdAt().format(INPUT_DATE_TIME_FORMATTER), entry.updatedAt().format(INPUT_DATE_TIME_FORMATTER));
        }

        MessagePrinter.divider();
    }

    /**
     * Truncate with ellipsis
     */
    private static String truncate(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    public static void printJournalViewEntries(DecryptedJournalGetResponse decryptedCompleteJournalEntry) {
        System.out.println();
        MessagePrinter.formatPair("ID", " : ", String.valueOf(decryptedCompleteJournalEntry.id()));
        MessagePrinter.formatPair("Title", " : ", decryptedCompleteJournalEntry.decryptedTitle());
        if (decryptedCompleteJournalEntry.decryptedMood() != null && !decryptedCompleteJournalEntry.decryptedMood().isBlank()) {
            MessagePrinter.formatPair("Mood", " : ", decryptedCompleteJournalEntry.decryptedMood());
        }
        if (decryptedCompleteJournalEntry.decryptedTags() != null  && !decryptedCompleteJournalEntry.decryptedTags().isEmpty()) {
            MessagePrinter.formatPair("Tag", " : ", String.join(", ", decryptedCompleteJournalEntry.decryptedTags()));
        }
        if (decryptedCompleteJournalEntry.expiresAt() != null) {
            MessagePrinter.formatPair("Expires At", " : ", String.join(", ", decryptedCompleteJournalEntry.expiresAt().format(DATE_TIME_FORMATTER)));
        }
        MessagePrinter.formatPair("Created At", " : " , decryptedCompleteJournalEntry.createdAt().format(DATE_TIME_FORMATTER)); // change formatter?
        if (!decryptedCompleteJournalEntry.createdAt().isEqual(decryptedCompleteJournalEntry.updatedAt())) {
            MessagePrinter.formatPair("Updated At", " : " , decryptedCompleteJournalEntry.updatedAt().format(DATE_TIME_FORMATTER)); // print only if its updated
        }
        MessagePrinter.formatPair("Content", " : ", "");
        System.out.println();
        System.out.println(decryptedCompleteJournalEntry.decryptedContent());

        System.out.println();
    }

    public static CreateEntryPromptResult getCreateEntryPromptResult(Scanner scanner) {
        MessagePrinter.prompt("Title: ");
        String title = scanner.nextLine();

        MessagePrinter.prompt("Content (type '::done' to finish): ");
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equalsIgnoreCase("::done")) {
            contentBuilder.append(line).append(System.lineSeparator());
        }
        String content = contentBuilder.toString().trim();
        System.out.println();

        MessagePrinter.prompt("Mood (optional): ");
        String mood = scanner.nextLine();

        MessagePrinter.prompt("Tags (comma-separated, optional): ");
        String tagInput = scanner.nextLine();
        List<String> tags = Arrays.stream(tagInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        MessagePrinter.prompt("Visible After (yyyy-MM-dd HH:mm, optional - blank = now): ");
        String visibleAfterStr = scanner.nextLine();

        MessagePrinter.prompt("Expires At (yyyy-MM-dd HH:mm, optional - blank = never): ");
        String expiresAtStr = scanner.nextLine();
        return new CreateEntryPromptResult(title, content, mood, tags, visibleAfterStr, expiresAtStr);
    }

    public static CreateEntryPromptResult promptForEditingJournalEntry(Long id, Scanner scanner) {
        System.out.println();
        MessagePrinter.info("Edit the required journal fields. If left blank, the respective fields remains same as before");
        MessagePrinter.formatPair("ID", ": ", String.valueOf(id));
        return getCreateEntryPromptResult(scanner);
    }

}
