package io.inkHeart.cli.util;

import picocli.CommandLine;

public class MessagePrinter {
    public static void title(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,yellow " + message + "|@"));
    }
    public static void showOption(String option, String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,cyan " + option + "|@" + " " + message));
    }

    public static void formatPair(String input1, String separator, String input2) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,cyan " + input1 + "|@" + separator + input2));
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

    public static void printAsciiBanner() {
        System.out.println(CommandLine.Help.Ansi.AUTO.string(
                """
                        @|bold,green\s
                          _____       _    _    _                 _  \s
                         |_   _|     | |  | |  | |               | | \s
                           | |  _ __ | | _| |__| | ___  __ _ _ __| |_\s
                           | | | '_ \\| |/ /  __  |/ _ \\/ _` | '__| __|
                          _| |_| | | |   <| |  | |  __/ (_| | |  | |_\s
                         |_____|_| |_|_|\\_\\_|  |_|\\___|\\__,_|_|   \\__|
                                                                     \s
                        |@"""
        ));
    }

    static String center(String message, int width) {
        int padding = (width - message.length()) / 2;
        if (padding <= 0) return message;
        return " ".repeat(padding) + message;
    }

}
