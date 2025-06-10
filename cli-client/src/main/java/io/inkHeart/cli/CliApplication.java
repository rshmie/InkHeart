package io.inkHeart.cli;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

public class CliApplication {
    public static final String API_URL = "http://localhost:8080/api/auth";

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        int exitCode = new CommandLine(new InkHeartCLI()).execute(args);
        AnsiConsole.systemUninstall();
        System.exit(exitCode);
    }


}
