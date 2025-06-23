package io.inkHeart.cli.service;

import io.inkHeart.cli.crypto.CryptoUtils;
import io.inkHeart.cli.dto.*;
import io.inkHeart.cli.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.crypto.SecretKey;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JournalServiceTest {
    private final HttpClient httpClient = mock(java.net.http.HttpClient.class);
    private final HttpResponse<String> httpResponse = mock(HttpResponse.class);
    private final SecretKey secretKey = CryptoUtils.deriveKeyFromPassword("password");;

    @Mock
    private Scanner scanner;

    @InjectMocks
    private JournalService journalService;

    JournalServiceTest() throws Exception {
    }

    @BeforeEach
    void setup() {
        journalService = new JournalService(secretKey, "fake-jwt", httpClient, scanner);
    }

    @Test
    void testListRecentUserEntriesReturnsDecryptedEntries() throws Exception {
        List<JournalEntryResponse> mockResponseList = List.of(
                new JournalEntryResponse(1L, new EncryptedPayload("cipher", "iv"),
                        LocalDateTime.now(), LocalDateTime.now())
        );

        String json = JsonUtil.getObjectMapper().writeValueAsString(mockResponseList);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);

        JournalService spyService = Mockito.spy(journalService);
        doReturn("Decrypted Title").when(spyService).decryptContent(any());
        List<DecryptedJournalEntryResponse> result = spyService.listRecentUserEntries();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Decrypted Title", result.get(0).title());
    }

    @Test
    void testViewJournalEntriesWhichReturnDecryptedResponse() throws Exception {
        JournalGetResponse mockResponseList = new JournalGetResponse(1L, new EncryptedPayload("encrypted-title", "title-iv"),
                new EncryptedPayload("encrypted-content", "content-iv"),
                null, List.of(new EncryptedPayload("encrypted-tag", "iv")),
                LocalDateTime.now(), null, null, null);

        String json = JsonUtil.getObjectMapper().writeValueAsString(mockResponseList);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);

        JournalService spyService = Mockito.spy(journalService);
        doReturn("Decrypted Title").when(spyService).decryptContent(any());
        DecryptedJournalGetResponse result = spyService.viewEntry(1L);

        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals("Decrypted Title", result.decryptedTitle());
    }

    @Test
    void testSearchEntries() throws Exception {
        List<DecryptedJournalGetResponse> decryptedList = List.of(
                new DecryptedJournalGetResponse(
                        1L,
                        "Title 1",
                        "Some content here",
                        "Happy",
                        List.of("test-tag1"),
                        LocalDateTime.now(),
                        LocalDateTime.now(), null, null
                ),
                new DecryptedJournalGetResponse(
                        2L,
                        "Title 2",
                        "Other content here",
                        "Sad",
                        List.of("test-tag2"),
                        LocalDateTime.now(),
                        LocalDateTime.now(), null, null
                )
        );

        JournalService spyService = Mockito.spy(journalService);
        doReturn(decryptedList).when(spyService).getAllTheJournalEntries();

        List<DecryptedJournalEntryResponse> result = spyService.searchEntries("test-tag2", null, null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).id());
        assertEquals("Title 2", result.get(0).title());

        result = spyService.searchEntries(null, "happy", null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).id());
        assertEquals("Title 1", result.get(0).title());
    }
}

