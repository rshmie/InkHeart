package io.inkHeart.cli.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.inkHeart.cli.crypto.CryptoUtils;
import io.inkHeart.cli.dto.*;
import io.inkHeart.cli.util.CLIMenu;
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
    public static final DateTimeFormatter INPUT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter FALL_BACK_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final SecretKey encryptionKey;
    private final String jwtToken;
    private final HttpClient httpClient;
    private final Scanner scanner;
    public JournalService(SecretKey encryptionKey, String jwtToken, HttpClient httpClient, Scanner scanner) {
        this.encryptionKey = encryptionKey;
        this.jwtToken = jwtToken;
        this.httpClient = httpClient;
        this.scanner = scanner;
    }

    /**
     * Create a new journal entry
     */
    public void createEntry() {
        CreateEntryPromptResult result = CLIMenu.getCreateEntryPromptResult(this.scanner);
        try {
            EncryptedPayload encryptedTitle = result.title().isBlank() ? null : encryptField(result.title());
            EncryptedPayload encryptedContent = result.content().isBlank() ? null: encryptField(result.content());
            EncryptedPayload encryptedMood = result.mood().isBlank() ? null : encryptField(result.mood());
            List<EncryptedPayload> encryptedTags = result.tags().isEmpty() ? Collections.emptyList() : result.tags().stream()
                    .map(this::encryptField)
                    .toList();
            LocalDateTime visibleAfter = result.visibleAfterStr().isBlank()
                    ? LocalDateTime.now()
                    : LocalDateTime.parse(result.visibleAfterStr(), INPUT_DATE_TIME_FORMATTER);
            LocalDateTime expiresAt = result.expiresAtStr().isBlank()
                    ? null
                    : LocalDateTime.parse(result.expiresAtStr(), INPUT_DATE_TIME_FORMATTER);

            CreateJournalEntryRequest request = new CreateJournalEntryRequest(encryptedTitle, encryptedContent, encryptedTags,
                    encryptedMood, visibleAfter, expiresAt);
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
        } catch (Exception e) {
            MessagePrinter.error("Error while encrypting or sending entry: " + e.getMessage());
        }
    }

    /**
     * Lists all the journal entries within the specified time range
     * @param fromDate - Start date
     * @param toDate - End date
     */
    public List<DecryptedJournalEntryResponse> listEntriesWithinRange(LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            String url = JOURNAL_BASE_URl + "?from=" + fromDate + "&to=" + toDate;
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + this.jwtToken)
                    .GET()
                    .build();
            var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Unable to get the journal entries within the specified range: " + response.statusCode());
            }
            List<JournalEntryResponse> entriesWithinRange = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<JournalEntryResponse>>(){});
            List<DecryptedJournalEntryResponse> decryptedJournalEntryResponse = new ArrayList<>();
            for (JournalEntryResponse journalEntry: entriesWithinRange) {
                decryptedJournalEntryResponse.add(new DecryptedJournalEntryResponse(journalEntry.id(),
                        decryptContent(journalEntry.encryptedTitle()), journalEntry.createdAt(), journalEntry.updatedAt()));
            }
            return decryptedJournalEntryResponse;
        } catch (Exception ex) {
            MessagePrinter.error("Unable to get entries within the specified range: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Lists the recent 10 entries, ordered by createdAt time (DESC)
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
     * View the complete details of journal entry specified by ID - GET request
     * @param id - Journal entry ID
     */
    public DecryptedJournalGetResponse viewEntry(Long id) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(JOURNAL_BASE_URl + "/" + id))
                .header("Authorization", "Bearer " + this.jwtToken)
                .GET()
                .build();

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            // Try to parse and print error message from server
            try {
                Map<String, String> errorMap = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<>() {});
                String errorMessage = errorMap.getOrDefault("error", "Unknown error");
                MessagePrinter.error("Unable to get the entry: " + errorMessage);
            } catch (Exception e) {
                MessagePrinter.error("Unable to get the entry. Server Status: " + response.statusCode());
            }
            return null;
        }
        JournalGetResponse journalGetResponse = JsonUtil.getObjectMapper().readValue(response.body(), JournalGetResponse.class);
        if (journalGetResponse == null) {
           MessagePrinter.error("Unable to view journal entry!");
           return null;
        }
        return new DecryptedJournalGetResponse(journalGetResponse.id(), decryptContent(journalGetResponse.encryptedTitle()),
                decryptContent(journalGetResponse.encryptedContent()), decryptContent(journalGetResponse.encryptedMood()),
                decryptTags(journalGetResponse.encryptedTags()), journalGetResponse.createdAt(),
                journalGetResponse.updatedAt(), journalGetResponse.visibleAfter(), journalGetResponse.expiresAt());
    }

    /**
     * Deletes the journal entry with the specified ID
     * @param id Journal Entry ID
     * @return Decrypted Journal Entry
     */
    public DecryptedJournalEntryResponse deleteEntry(Long id) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(JOURNAL_BASE_URl + "/" + id))
                .header("Authorization", "Bearer " + this.jwtToken)
                .DELETE()
                .build();

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            // Try to parse and print error message from server
            try {
                Map<String, String> errorMap = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<>() {});
                String errorMessage = errorMap.getOrDefault("error", "Unknown error");
                MessagePrinter.error("Unable to delete entry: " + errorMessage);
            } catch (Exception e) {
                MessagePrinter.error("Unable to delete entry. Server Status: " + response.statusCode());
            }
            return null;
        }
        JournalEntryResponse journalDeleteResponse = JsonUtil.getObjectMapper().readValue(response.body(), JournalEntryResponse.class);
        return new DecryptedJournalEntryResponse(journalDeleteResponse.id(), decryptContent(journalDeleteResponse.encryptedTitle()),
                journalDeleteResponse.createdAt(), journalDeleteResponse.updatedAt());
    }

    /**
     * Edits the journal entry with the specified ID with edited contents and sends the PATCH to the backend
     * @param id - Journal entry ID
     * @return Decrypted Journal Entry
     */
    public DecryptedJournalEntryResponse editEntry(Long id) throws IOException, InterruptedException {
        // Get a journal entry
        DecryptedJournalGetResponse originalEntry = viewEntry(id);
        CLIMenu.printJournalViewEntries(originalEntry);

        CreateEntryPromptResult editedEntry = CLIMenu.promptForEditingJournalEntry(id, this.scanner);
        EncryptedPayload encryptedTitle = editedEntry.title().isBlank() ? null :
                (!editedEntry.title().equals(originalEntry.decryptedTitle()) ? encryptField(editedEntry.title()) : null);

        EncryptedPayload encryptedContent = editedEntry.content().isBlank() ? null :
                (!editedEntry.content().equals(originalEntry.decryptedContent()) ? encryptField(editedEntry.content()) : null);

        EncryptedPayload encryptedMood = editedEntry.mood().isBlank() ? null :
                (!editedEntry.mood().equals(originalEntry.decryptedMood()) ? encryptField(editedEntry.mood()) : null);

        List<EncryptedPayload> encryptedTags = editedEntry.tags().isEmpty() ? null :
                (!editedEntry.tags().equals(originalEntry.decryptedTags()) ? editedEntry.tags().stream().map(this::encryptField).toList() : null);

        LocalDateTime visibleAfter = editedEntry.visibleAfterStr().isBlank() ? null :
                LocalDateTime.parse(editedEntry.visibleAfterStr(), INPUT_DATE_TIME_FORMATTER);

        LocalDateTime expiresAt = editedEntry.expiresAtStr().isBlank() ? null :
                LocalDateTime.parse(editedEntry.expiresAtStr(), INPUT_DATE_TIME_FORMATTER);

        UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(encryptedTitle, encryptedContent,
                encryptedMood, encryptedTags, visibleAfter, expiresAt);
        var editRequestJson = JsonUtil.getObjectMapper().writeValueAsString(request);

        // Send the PATCH request to server
        HttpRequest httpJournalEditRequest = HttpRequest.newBuilder()
                .uri(URI.create(JOURNAL_BASE_URl + "/" + id))
                .header("Authorization", "Bearer " + this.jwtToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(editRequestJson))
                .build();

        var response = this.httpClient.send(httpJournalEditRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            try {
                Map<String, String> errorMap = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<>() {});
                String errorMessage = errorMap.getOrDefault("error", "Unknown error");
                MessagePrinter.error("Failed to update entry: " + errorMessage);
            } catch (Exception e) {
                MessagePrinter.error("Failed to update entry. Server Status: " + response.statusCode());
            }
            return null;
        }
        JournalEntryResponse journalEditResponse = JsonUtil.getObjectMapper().readValue(response.body(), JournalEntryResponse.class);
        return new DecryptedJournalEntryResponse(journalEditResponse.id(), decryptContent(journalEditResponse.encryptedTitle()),
                journalEditResponse.createdAt(), journalEditResponse.updatedAt());
    }

    /**
     * Search across the user's journal entry by tag, mood or by keyword/phrase
     */
    public List<DecryptedJournalEntryResponse> searchEntries(String tag, String mood, String content) throws IOException, InterruptedException {
        List<DecryptedJournalGetResponse> decryptedEntryList = getAllTheJournalEntries();
        if (decryptedEntryList == null || decryptedEntryList.isEmpty()) {
            return null;
        }

        Set<Long> seenIds = new HashSet<>();
        List<DecryptedJournalEntryResponse> searchResult = new ArrayList<>();
        for (DecryptedJournalGetResponse entry : decryptedEntryList) {
            boolean match = false;

            if (tag != null && !tag.isBlank() && entry.decryptedTags() != null) {
                match |= entry.decryptedTags().stream().anyMatch(t -> t != null && t.equalsIgnoreCase(tag));
            }

            if (mood != null && !mood.isBlank() && entry.decryptedMood() != null) {
                match |= entry.decryptedMood().equalsIgnoreCase(mood);
            }

            if (content != null && !content.isBlank() && entry.decryptedContent() != null) {
                match |= entry.decryptedContent().toLowerCase().contains(content.toLowerCase());
            }

            if (match && !seenIds.contains(entry.id())) {
                searchResult.add(new DecryptedJournalEntryResponse(entry.id(), entry.decryptedTitle(), entry.createdAt(), entry.updatedAt()));
                seenIds.add(entry.id());
            }
        }
        return searchResult;
    }

    /**
     * Get all the journal entries of the user
     */
    List<DecryptedJournalGetResponse> getAllTheJournalEntries() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(JOURNAL_BASE_URl + "/entries"))
                .header("Authorization", "Bearer " + this.jwtToken)
                .GET()
                .build();

        var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            try {
                Map<String, String> errorMap = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<>() {});
                String errorMessage = errorMap.getOrDefault("error", "Unknown error");
                MessagePrinter.error("Failed to get all the journal entry: " + errorMessage);
            } catch (Exception e) {
                MessagePrinter.error("Failed to get all the journal entry. Server Status: " + response.statusCode());
            }
            return null;
        }
        List<JournalGetResponse> responseList = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<JournalGetResponse>>(){});
        List<DecryptedJournalGetResponse> decryptedJournalGetResponseList = new ArrayList<>();
        for (JournalGetResponse journalGetResponse: responseList) {
            decryptedJournalGetResponseList.add(new DecryptedJournalGetResponse(journalGetResponse.id(), decryptContent(journalGetResponse.encryptedTitle()),
                    decryptContent(journalGetResponse.encryptedContent()), decryptContent(journalGetResponse.encryptedMood()),
                    decryptTags(journalGetResponse.encryptedTags()), journalGetResponse.createdAt(),
                    journalGetResponse.updatedAt(), journalGetResponse.visibleAfter(), journalGetResponse.expiresAt()));
        }

        return decryptedJournalGetResponseList;
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

    String decryptContent(EncryptedPayload encryptedPayload) {
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
