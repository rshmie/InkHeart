package io.inkHeart.cli.util;

import org.jline.jansi.Ansi;
import picocli.CommandLine;

public class MessagePrinter {
    public static void title(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,yellow " + message + "|@"));
    }

    public static void info(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|cyan " + message + "|@"));
    }

    public static void prompt(String message) {
        System.out.print(CommandLine.Help.Ansi.AUTO.string("@|bold,cyan " + message + "|@ "));
    }

    public static void success(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,green " + message + "|@"));
    }

    public static void error(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,red [!] " + message + "|@")); // ⚠ ERROR
        waitForEnter();
    }

    public static void divider() {
//        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|blue ------------------------------------" +
//                "--------------------------------------------|@"));
        System.out.println(CommandLine.Help.Ansi.AUTO.string(
                "@|fg(blue),italic ---------------------------------------------------------------------------------|@"));
    }

    public static void waitForEnter() {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|italic,white Press Enter to continue...|@"));
        new java.util.Scanner(System.in).nextLine();
    }
    public static void farewell(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|cyan " + message + "|@"));
        System.out.println();
    }
    public static void warning(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|yellow WARNING: " + message + "|@"));
    }

    public static void logOutMessage() {
        MessagePrinter.success("You have been logged out.");
    }


}
