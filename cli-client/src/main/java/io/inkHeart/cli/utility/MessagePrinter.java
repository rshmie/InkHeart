package io.inkHeart.cli.utility;

import picocli.CommandLine;

public class MessagePrinter {
    public static void title(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,green " + message + "|@"));
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
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,red ERROR: " + message + "|@"));
    }

    public static void divider() {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|blue ------------------------------------" +
                "--------------------------------------------|@"));
    }

    public static void waitForEnter() {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|italic,white Press Enter to continue...|@"));
        new java.util.Scanner(System.in).nextLine();
    }

}
