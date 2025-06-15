package io.inkHeart.cli.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.inkHeart.cli.crypto.CryptoUtils;
import io.inkHeart.cli.dto.*;
import io.inkHeart.cli.util.JsonUtil;
import io.inkHeart.cli.util.MessagePrinter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        System.out.println();
        String content = scanner.nextLine();
        System.out.println();

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
            JournalEntryResponse journalEntryResponse = JsonUtil.getObjectMapper().readValue(response.body(), JournalEntryResponse.class);
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println();
                MessagePrinter.success("Entry saved!");
                String title =  decryptContent(journalEntryResponse.encryptedTitle());
                MessagePrinter.info("Your journal entry titled \"" + title + "\" was created on " + journalEntryResponse.createdAt().format(DATE_TIME_FORMATTER));
            } else {
                MessagePrinter.error(" Failed to save entry: " + response.body());
            }
        } catch (Exception e) {
            MessagePrinter.error("Failed sending request: " + e.getMessage());
        }
    }


    /**
     * Lists the recent 10 entries, ordered by createdAt time (DESC)
     *
     */
    public List<DecryptedJournalEntryResponse> listRecentUserEntries() {
        try {
            List<JournalEntryResponse> recentEntries = fetchRecentEntries();
            List<DecryptedJournalEntryResponse> decryptedJournalEntryResponse = new ArrayList<>();
            for (JournalEntryResponse journalEntry: recentEntries) {
                decryptedJournalEntryResponse.add(new DecryptedJournalEntryResponse(journalEntry.id(),
                        decryptContent(journalEntry.encryptedTitle()), journalEntry.createdAt(), journalEntry.updatedAt()));
            }
            return decryptedJournalEntryResponse;
        } catch (Exception ex) {
            MessagePrinter.error("Unable to get recent entries: " + ex.getMessage());
        }
        return null;
    }

    private List<JournalEntryResponse> fetchRecentEntries() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(JOURNAL_BASE_URl + "/recent")) // perhaps a config file
                .header("Authorization", "Bearer " + this.jwtToken)
                .GET()
                .build();
        var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Unable to fetch the journal entries: " + response.statusCode());
        }
        return JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<JournalEntryResponse>>(){});
    }

    /**
     * View the complete journal entry details by ID
     * @param id - Journal entry ID
     */
    public DecryptedJournalGetResponse viewEntry(Long id) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(JOURNAL_BASE_URl + "/entry/" + id))
                .header("Authorization", "Bearer " + this.jwtToken)
                .GET()
                .build();

        HttpResponse<String> response;
        response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            // Try to parse and print error message from server
            try {
                Map<String, String> errorMap = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<>() {});
                String errorMessage = errorMap.getOrDefault("error", "Unknown error");
                MessagePrinter.error("Unable to view entry: " + errorMessage);
            } catch (Exception e) {
                MessagePrinter.error("Unable to view entry. Status: " + response.statusCode());
            }
            return null;
        }
        JournalGetResponse journalGetResponse = JsonUtil.getObjectMapper().readValue(response.body(), JournalGetResponse.class);
        return new DecryptedJournalGetResponse(journalGetResponse.id(), decryptContent(journalGetResponse.encryptedTitle()),
                decryptContent(journalGetResponse.encryptedContent()), decryptContent(journalGetResponse.encryptedMood()),
                decryptTags(journalGetResponse.encryptedTags()), journalGetResponse.createdAt(),
                journalGetResponse.updatedAt(), journalGetResponse.visibleAfter(), journalGetResponse.expiresAt());
    }

    public void editEntry(Long id) {
        MessagePrinter.info("Not yet implemented");
    }

    public DecryptedJournalEntryResponse deleteEntry(Long id) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(JOURNAL_BASE_URl + "/entry/" + id))
                .header("Authorization", "Bearer " + this.jwtToken)
                .DELETE()
                .build();

        HttpResponse<String> response;
        response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            // Try to parse and print error message from server
            try {
                Map<String, String> errorMap = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<>() {});
                String errorMessage = errorMap.getOrDefault("error", "Unknown error");
                MessagePrinter.error("Unable to delete entry: " + errorMessage);
            } catch (Exception e) {
                MessagePrinter.error("Unable to delete entry. Status: " + response.statusCode());
            }
            return null;
        }
        JournalEntryResponse journalDeleteResponse = JsonUtil.getObjectMapper().readValue(response.body(), JournalEntryResponse.class);
        return new DecryptedJournalEntryResponse(journalDeleteResponse.id(), decryptContent(journalDeleteResponse.encryptedTitle()),
                journalDeleteResponse.createdAt(), journalDeleteResponse.updatedAt());
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
            throw new RuntimeException("Encryption failed : " + e.getMessage());
        }
    }

    private String decryptContent(EncryptedPayload encryptedPayload) {
        if (checkNull(encryptedPayload) == null) {
            return null;
        }
        try {
            return CryptoUtils.decrypt(CryptoUtils.base64EncodedToBytes(encryptedPayload.cipherText()),
                    this.encryptionKey,
                    CryptoUtils.base64EncodedToBytes(encryptedPayload.iv()), null);
        } catch (Exception e) {
            throw new RuntimeException("Content decryption failed: " + e.getMessage());
        }
    }

    private List<String> decryptTags(List<EncryptedPayload> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags.stream().map(this::decryptContent).collect(Collectors.toList());
    }

    private EncryptedPayload checkNull(EncryptedPayload encryptedPayload) {
        return encryptedPayload == null ? null : new EncryptedPayload(encryptedPayload.cipherText(), encryptedPayload.iv());
    }

}
