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
        MessagePrinter.title("-------- [ Secure Journal Dashboard ] --------");
        System.out.println();
        System.out.println(CommandLine.Help.Ansi.AUTO.string("""
            @|cyan [1]|@ New Entry
            @|cyan [2]|@ View Entries
            @|cyan [3]|@ Search Entries
            @|cyan [4]|@ Delete Entry
            @|cyan [5]|@ Logout
        """));
        MessagePrinter.divider();
        MessagePrinter.prompt("Select an action [1-5]: "); // Choose an option from above:
    }
}
