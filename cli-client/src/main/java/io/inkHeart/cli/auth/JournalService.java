package io.inkHeart.cli.auth;

import io.inkHeart.cli.crypto.CryptoUtils;
import io.inkHeart.cli.dto.CreateJournalEntryRequest;
import io.inkHeart.cli.dto.EncryptedPayload;
import io.inkHeart.cli.dto.JournalEntryResponse;
import io.inkHeart.cli.util.JsonUtil;
import io.inkHeart.cli.util.MessagePrinter;

import javax.crypto.SecretKey;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.inkHeart.cli.CliApplication.API_URL;
import static io.inkHeart.cli.CliApplication.JOURNAL_BASE_URl;

public class JournalService {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
    private final SecretKey encryptionKey;
    private final String jwtToken;
    private final HttpClient httpClient;
    public JournalService(SecretKey encryptionKey, String jwtToken, HttpClient httpClient) {
        this.encryptionKey = encryptionKey;
        this.jwtToken = jwtToken;
        this.httpClient = httpClient;
    }

    public void createEntry() {
        Scanner scanner = new Scanner(System.in);

        MessagePrinter.prompt("Title: ");
        String title = scanner.nextLine();

        MessagePrinter.prompt("Content: ");
        String content = scanner.nextLine();

        MessagePrinter.prompt("Mood (optional): ");
        String mood = scanner.nextLine();

        MessagePrinter.prompt("Tags (comma-separated, optional): ");
        String tagInput = scanner.nextLine();
        List<String> tags = Arrays.stream(tagInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        MessagePrinter.prompt("Visible After (yyyy-MM-dd HH:mm, optional - blank = now): ");
        String visibleAfterStr = scanner.nextLine();

        MessagePrinter.prompt("Expires At (yyyy-MM-dd HH:mm, optional - blank = never): ");
        String expiresAtStr = scanner.nextLine();

        try {
            EncryptedPayload encryptedTitle = title.isBlank() ? null : encryptField(title);
            EncryptedPayload encryptedContent = content.isBlank() ? null: encryptField(content);
            EncryptedPayload encryptedMood = mood.isBlank() ? null : encryptField(mood);
            List<EncryptedPayload> encryptedTags = tags.isEmpty() ? Collections.emptyList() : tags.stream()
                    .map(this::encryptField)
                    .toList();
            LocalDateTime visibleAfter = visibleAfterStr.isBlank()
                    ? LocalDateTime.now()
                    : LocalDateTime.parse(visibleAfterStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime expiresAt = expiresAtStr.isBlank()
                    ? null
                    : LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                    encryptedTitle,
                    encryptedContent,
                    encryptedTags,
                    encryptedMood,
                    visibleAfter,
                    expiresAt
            );
            sendToServer(request);
        } catch (Exception e) {
            MessagePrinter.error("Error while encrypting or sending entry: " + e.getMessage());
        }

    }

    private void sendToServer(CreateJournalEntryRequest request) {
        try {
            String requestJson = JsonUtil.getObjectMapper().writeValueAsString(request);
            HttpRequest httpJournalEntryRequest = HttpRequest.newBuilder()
                    .uri(URI.create(JOURNAL_BASE_URl + "/create"))
                    .header("Authorization", "Bearer " + this.jwtToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            var response = this.httpClient.send(httpJournalEntryRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response : " + response.body());
            JournalEntryResponse journalEntryResponse = JsonUtil.getObjectMapper().readValue(response.body(), JournalEntryResponse.class);
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                MessagePrinter.success("Journal entry saved!");
                String title =  CryptoUtils.decrypt(CryptoUtils.base64EncodedToBytes(journalEntryResponse.encryptedTitle().cipherText()),
                        this.encryptionKey, CryptoUtils.base64EncodedToBytes(journalEntryResponse.encryptedTitle().iv()), null);
                MessagePrinter.info("Your journal entry \"" + title + "\" was created on " + journalEntryResponse.createdAt().format(DATE_TIME_FORMATTER));
            } else {
                MessagePrinter.error(" Failed to save entry: " + response.body());
            }
        } catch (Exception e) {
            MessagePrinter.error("Failed sending request: " + e.getMessage());
        }
    }

    public boolean createEntry(String title, String content) {
        return false;
    }

    public List<String> listEntries() {
        return null;
    }

    public Object search(String keyword) {
        return new ArrayList<String>();
    }

    public boolean delete(String title) {
        return false;
    }

    private EncryptedPayload encryptField(String input) {
        try {
            byte[] iv = CryptoUtils.generateIV();
            var result = CryptoUtils.encrypt(input, encryptionKey, iv, null);
            return new EncryptedPayload(
                    result.getCipherTextInBase64(),
                    result.getIvInBase64()
            );
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
}
