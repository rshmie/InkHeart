package io.inkHeart.cli.util;

import io.inkHeart.cli.dto.DecryptedJournalEntryResponse;
import io.inkHeart.cli.dto.DecryptedJournalGetResponse;
import picocli.CommandLine;

import java.util.List;

import static io.inkHeart.cli.service.JournalService.DATE_TIME_FORMATTER;

public class CLIMenu {
    public static void printWelcomeMenu() {
        // Clear screen for a cleaner look (optional, might not work on all terminals)
        // System.out.print("\033[H\033[2J");
        System.out.flush();
        //🌿
        System.out.println();
        MessagePrinter.divider();
        MessagePrinter.title("                            Welcome to InkHeart CLI");
        MessagePrinter.divider();
        System.out.println();
        System.out.println("A secure, encrypted journal where your thoughts stay truly yours.");
        System.out.println();
        MessagePrinter.showOption("[1]", "Register a new account"); // cyan bold or cyan?
        MessagePrinter.showOption("[2]", "Log in to your existing account");
        MessagePrinter.showOption("[3]", "Exit");
        MessagePrinter.divider();
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
        MessagePrinter.prompt("Select an action [1-3]: "); // Choose an option from above:
    }

    public static void showMyJournalMenu() {
        System.out.println();
        MessagePrinter.divider();
        MessagePrinter.title("---------- [ My Journal ] ----------");
        System.out.println(" Find an entry to view, edit, or delete.");
        System.out.println("Choose how you'd like to find it: ");
        MessagePrinter.divider();
        MessagePrinter.showOption("[1]", "List Recent Entries\n  -> Shows the 10 most recent entry titles.");
        MessagePrinter.showOption("[2]", "Filter by Date Range\n  ->  Finds all entries between two specific dates.");
        MessagePrinter.showOption("[3]", "Full-Text Search\n  -> Search for a word, mood or tag across your entire journal.");
        MessagePrinter.showOption("[4]", "Back to Main Menu\n");
        System.out.println();

    }


    // perhaps move into different class ?
    public static void showJournalEntriesTable(List<DecryptedJournalEntryResponse> entries) {
        if (entries.isEmpty()) {
            MessagePrinter.info("You have no journal entries yet.");
            return;
        }

        MessagePrinter.divider();
        System.out.printf("%-5s | %-25s | %-25s | %-25s%n", "ID", "Title", "Created At", "Updated At");
        MessagePrinter.divider();

        for (DecryptedJournalEntryResponse entry : entries) {
            System.out.printf("%-5d | %-25s | %-25s | %-25s%n", entry.id(), entry.title(), entry.createdAt(), entry.updatedAt());
        }

        MessagePrinter.divider();
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
}
