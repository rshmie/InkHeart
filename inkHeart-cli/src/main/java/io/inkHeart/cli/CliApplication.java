package io.inkHeart.cli;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

public class CliApplication {
    public static final String API_URL = "http://localhost:8080/api/auth";
    public static final String JOURNAL_BASE_URl = "http://localhost:8080/api/journal";

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        int exitCode = new CommandLine(new InkHeartCli()).execute(args);
        AnsiConsole.systemUninstall();
        System.exit(exitCode);
    }


}
