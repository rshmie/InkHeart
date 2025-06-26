package io.inkHeart.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

/**
 * As currently project doesn't use any secret manager to store and manage the JWT secret, it is rather generated
 * automatically during the initial build process and stored in the jwt.properties
 */
public class JwtSecretGenerator {
    private static final String SECRET_PROPERTIES_FILE = "jwt.properties";

    public static void main(String[] args) throws IOException {
        // The first argument will be the output directory provided by Maven.
        if (args.length == 0) {
            System.err.println("Error: Output directory not provided.");
            System.exit(1);
        }
        String outputDir = args[0];
        File secretFile = Paths.get(outputDir, SECRET_PROPERTIES_FILE).toFile();

        if (!secretFile.exists()) {
            System.out.println("JWT properties file not found. Generating a new one...");
            // Generate the secret
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String secret = Base64.getEncoder().encodeToString(bytes);

            // Create and store in a properties format
            Properties props = new Properties();
            props.setProperty("jwt.secret", secret);

            try (FileWriter writer = new FileWriter(secretFile)) {
                props.store(writer, "JWT Secret - Auto-generated. DO NOT COMMIT!");
            }
            System.out.println("New JWT secret generated and saved to " + secretFile.getAbsolutePath());
        } else {
            System.out.println("Existing JWT properties file found. Skipping generation.");
        }
    }
}