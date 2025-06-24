package io.inkHeart.cli;

import io.inkHeart.cli.commad.LoginCommand;
import io.inkHeart.cli.commad.RegisterCommand;
import io.inkHeart.cli.util.CLIMenu;
import io.inkHeart.cli.util.MessagePrinter;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.util.Scanner;

@CommandLine.Command(name = "inkHeart",
        mixinStandardHelpOptions = true,
        version = "InkHeart CLI 1.0",
        description = "A secure, encrypted journal - Privacy-first, empathetic digital journal where your deepest thoughts stay truly just yours.")
public class InkHeartCLI implements Runnable {

    @Override
    public void run() {
        // This is the entry point when no subcommands are specified.
        // We will now make it interactive.
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println();
            CLIMenu.printWelcomeMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    // Manually create and execute the RegisterCommand
                    new CommandLine(new RegisterCommand()).execute();
                    break;
                case "2":
                    // Manually create and execute the LoginCommand
                    new CommandLine(new LoginCommand()).execute();
                    break;
                case "3":
                    MessagePrinter.farewell("\nThank you for using InkHeart - your thoughts remain yours.");
                    return; // Exit the loop and the application
                default:
                    MessagePrinter.error("Invalid option. Please try again.");
            }
        }
    }

}