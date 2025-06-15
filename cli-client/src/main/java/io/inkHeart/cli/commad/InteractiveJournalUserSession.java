package io.inkHeart.cli.commad;

import io.inkHeart.cli.dto.DecryptedJournalEntryResponse;
import io.inkHeart.cli.dto.DecryptedJournalGetResponse;
import io.inkHeart.cli.service.JournalService;
import io.inkHeart.cli.util.CLIMenu;
import io.inkHeart.cli.util.MessagePrinter;
import org.bouncycastle.util.test.FixedSecureRandom;

import javax.crypto.SecretKey;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Scanner;

import static io.inkHeart.cli.service.JournalService.DATE_TIME_FORMATTER;

public class InteractiveJournalUserSession {
    private final JournalService journalService;
    private final Scanner scanner;

    // jwt token expiry handling - TO DO
    public InteractiveJournalUserSession(String jwtToken, SecretKey encryptionKey) {
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
                case "1" -> journalService.createEntry();
                case "2" -> myJournalSubMenu();
                case "3" -> {
                    running = false;
                    MessagePrinter.success("Logging you out. Goodbye!");
                }
                default -> MessagePrinter.error("Invalid option. Please try again.");
            }
        }
    }

    public void myJournalSubMenu() {
        boolean inSubMenu = true;
        while (inSubMenu) {
            CLIMenu.showMyJournalMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    handleListingRecentEntries();
                    break;
                case "2":
                    MessagePrinter.info("Filter by Date Range - Not implemented yet.");
                    break;
                case "3":
                    MessagePrinter.info("Full-Text Search - Not implemented yet.");
                    break;
                case "4":
                    // This is how you "go back to the previous menu".
                    // By exiting this loop, control returns to the main `start()` loop.
                    inSubMenu = false;
                    break;
                default:
                    MessagePrinter.error("Invalid option.");
                    break;
            }
        }
    }

    private void handleListingRecentEntries() {
        try {
            List<DecryptedJournalEntryResponse> entries = journalService.listRecentUserEntries();

            if (entries.isEmpty()) {
                MessagePrinter.info("No journal entries found.");
                return; // Go back to the sub-menu
            }
            CLIMenu.showJournalEntriesTable(entries);
            handleEntryActions();

        } catch (Exception e) {
            MessagePrinter.error("Could not retrieve journal entries: " + e.getMessage());
        }
    }

    /**
     * Handle actions such as View, Edit, Delete
     * What would you like to do?
     * → Format: <ID> <Action> (e.g. 105 V)
     *    - [V] View Entry
     *    - [D] Delete Entry
     *    - [E] Edit Entry
     *    - [B] Back to Previous Menu
     * Enter your choice:
     */
    public void handleEntryActions() {
        while (true) {
            CLIMenu.journalEntryActionMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("B")) return;
            String[] parts = input.split("\\s+");

            if (parts.length != 2) {
                MessagePrinter.error("Invalid format. Use: [ID] [Action] (e.g. 105 V)");
                continue;
            }
            try {
                Long id = Long.parseLong(parts[0]);
                String action = parts[1].toLowerCase();

                switch (action) {
                    case "v" -> handleViewEntryAction(id);
                    case "e" -> journalService.editEntry(id);
                    case "d" -> handleDeleteEntryAction(id);
                    default -> {
                        MessagePrinter.error("Unknown action. Use V, E, or D.");
                        continue;
                    }
                }
                break;

            } catch (NumberFormatException e) {
                MessagePrinter.error("Invalid ID. Must be a number.");
            }
        }
    }

    private void handleViewEntryAction(Long id) {
        try {
            DecryptedJournalGetResponse decryptedCompleteJournalEntry = journalService.viewEntry(id);
            if (decryptedCompleteJournalEntry != null) {
                CLIMenu.printJournalViewEntries(decryptedCompleteJournalEntry);
            }
        } catch (Exception ex) {
            MessagePrinter.error("Unable to view journal entry with id : " + id +  " :" + ex.getMessage());
        }
    }

    private void handleDeleteEntryAction(Long id) {
        try {
            MessagePrinter.prompt("Are you sure you want to permanently delete this entry? (Y/N) ");
            String answer = this.scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("N") || answer.equalsIgnoreCase("No")) {
                return;
            }
            DecryptedJournalEntryResponse deleteEntry = journalService.deleteEntry(id);
            if (deleteEntry != null) {
                MessagePrinter.success("\nEntry deleted successfully.");
                MessagePrinter.info("ID: " + deleteEntry.id() + " | " + "Title: \"" + deleteEntry.title() + "\"" +
                        " | " + "Created on: " + deleteEntry.createdAt().format(DATE_TIME_FORMATTER));
            }
        } catch (Exception ex) {
            MessagePrinter.error("Unable to view journal entry with id : " + id +  " :" + ex.getMessage());
        }
    }


}
