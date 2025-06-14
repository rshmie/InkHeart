package io.inkHeart.cli.util;

import picocli.CommandLine;

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
        System.out.println(CommandLine.Help.Ansi.AUTO.string("""
            @|cyan [1]|@ Register a new account
            @|cyan [2]|@ Log in to your existing account
            @|cyan [3]|@ Exit
        """));
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
}
