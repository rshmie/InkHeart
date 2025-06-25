package io.inkHeart.contollers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.inkHeart.dto.*;
import io.inkHeart.entity.CustomUserDetails;
import io.inkHeart.entity.JournalEntry;
import io.inkHeart.entity.User;

import io.inkHeart.exception.NoJournalEntryFoundException;
import io.inkHeart.security.JwtUtil;
import io.inkHeart.service.JournalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JournalEntryController.class)
class JournalEntryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JournalService journalService;
    @Autowired
    private ObjectMapper objectMapper;
    private CustomUserDetails userDetails;
    private User testUser;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public JournalService journalService() {
            return Mockito.mock(JournalService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
        }
    }
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        userDetails = new CustomUserDetails(testUser);
    }

    @Test
    void testJournalCreateWithValidRequest() throws Exception {
        CreateJournalEntryRequest request = createTestJournalEntryRequest();

        JournalEntry savedEntry = new JournalEntry();
        savedEntry.setId(1L);
        savedEntry.setEncryptedTitle(new EncryptedPayload("encrypted-title", "title-iv"));
        savedEntry.setEncryptedContent(new EncryptedPayload("encrypted-content", "content-iv"));
        savedEntry.setCreatedAt(LocalDateTime.now());

        when(journalService.createEntry(eq(testUser), any(CreateJournalEntryRequest.class)))
                .thenReturn(savedEntry);

        mockMvc.perform(post("/api/journal/create")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.encryptedTitle.cipherText").value("encrypted-title"));
    }

    @Test
    void testCreateWhenUserIsUnauthenticated() throws Exception {
        var request = createTestJournalEntryRequest();

        JournalEntry savedEntry = new JournalEntry();
        savedEntry.setId(1L);
        savedEntry.setEncryptedTitle(new EncryptedPayload("encrypted-title", "title-iv"));
        savedEntry.setEncryptedContent(new EncryptedPayload("encrypted-content", "content-iv"));
        savedEntry.setCreatedAt(LocalDateTime.now());

        when(journalService.createEntry(eq(testUser), any(CreateJournalEntryRequest.class)))
                .thenReturn(savedEntry);
        mockMvc.perform(post("/api/journal/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetJournalEntryByID() throws Exception {
        UUID uuid = UUID.randomUUID();
        JournalGetResponse response = new JournalGetResponse(1L, uuid,
                new EncryptedPayload("encrypted-title", "title-iv"),
                new EncryptedPayload("encrypted-content", "content-iv"),
                new EncryptedPayload("encrypted-mood", ""),
                List.of(new EncryptedPayload("encrypted-tag", "test")),
                LocalDateTime.now(),
                null, null, null);

        when(journalService.getCompleteJournalEntryById(testUser, 1L))
                .thenReturn(response);

        var result = mockMvc.perform(get("/api/journal/1")
                        .with(csrf())
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        JournalGetResponse actual = mapper.readValue(json, JournalGetResponse.class);

        assertEquals(response.id(), actual.id());
        assertEquals(response.encryptedTitle().cipherText(), actual.encryptedTitle().cipherText());
        assertEquals(response.encryptedContent().cipherText(), actual.encryptedContent().cipherText());
        assertEquals(response.encryptedContent().iv(), actual.encryptedContent().iv());
    }

    @Test
    void testGetJournalEntryById_NotFound() throws Exception {
        when(journalService.getCompleteJournalEntryById(testUser, 999L))
                .thenThrow(NoJournalEntryFoundException.class);

        mockMvc.perform(get("/api/journal/999")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }
    @Test
    void testGetAllJournalEntries() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        List<JournalGetResponse> responses = List.of(
                new JournalGetResponse(1L, uuid1, new EncryptedPayload("title1", "title1-iv"),
                        new EncryptedPayload("content1", "content1-iv"), null,
                                List.of(new EncryptedPayload("encrypted-tag", "entry1")),
                                LocalDateTime.now(),
                                null, null, null),
                new JournalGetResponse(2L, uuid2, new EncryptedPayload("title2", "title2-iv"),
                        new EncryptedPayload("content2", "content2-iv"), null,
                        List.of(new EncryptedPayload("encrypted-tag", "entry2")),
                        LocalDateTime.now(),
                        null, null, null));

        when(journalService.getAllJournalEntries(testUser.getEmail())).thenReturn(responses);

        mockMvc.perform(get("/api/journal/entries")
                        .with(csrf())
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

    }

    @Test
    void testGet10RecentEntries() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        List<JournalEntryResponse> responses = List.of(
                new JournalEntryResponse(1L, uuid1, new EncryptedPayload("Title 1", "iv 1"), LocalDateTime.now(), null),
                new JournalEntryResponse(2L, uuid2, new EncryptedPayload("Title 2", "iv 2"), LocalDateTime.now(), LocalDateTime.now().plusDays(10))
        );

        when(journalService.get10RecentEntriesForUser(testUser)).thenReturn(responses);

        mockMvc.perform(get("/api/journal/recent")
                        .with(csrf())
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].encryptedTitle.cipherText").value("Title 1"));
    }

    @Test
    void testDeleteJournalEntryById() throws Exception {
        UUID uuid = UUID.randomUUID();
        JournalEntryResponse response = new JournalEntryResponse(1L, uuid,
                new EncryptedPayload("Deleted Title", "deleted-iv"), LocalDateTime.now(), null);

        when(journalService.deleteJournalEntryById(testUser, 1L)).thenReturn(response);

        mockMvc.perform(delete("/api/journal/1")
                        .with(csrf())
                        .with(user(userDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.encryptedTitle.cipherText").value("Deleted Title"));
    }

    @Test
    void testUpdateJournalEntryById() throws Exception {
        UUID uuid = UUID.randomUUID();
        UpdateJournalEntryRequest updateRequest = new UpdateJournalEntryRequest( uuid,
                new EncryptedPayload("Updated Title", "updated-iv"),
                new EncryptedPayload("encrypted-content", "content-iv"),
                new EncryptedPayload("encrypted-mood", ""),
                List.of(new EncryptedPayload("encrypted-tag", "test")),
                null,
                null
        );

        JournalEntryResponse updatedResponse = new JournalEntryResponse(1L, uuid,
                new EncryptedPayload("Updated Title", "updated-iv"), LocalDateTime.now(), null);

        when(journalService.updateJournalEntryById(eq(testUser), eq(1L), any(UpdateJournalEntryRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/journal/1")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.encryptedTitle.cipherText").value("Updated Title"));
    }

    @Test
    void testGetEntriesBetweenDateRange() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        LocalDateTime from = LocalDateTime.of(2025, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 7, 30, 23, 59);

        List<JournalEntryResponse> responses = List.of(
                new JournalEntryResponse(1L, uuid1, new EncryptedPayload("2025 June Entry", "iv1"),
                        LocalDateTime.of(2025, 6, 23, 10, 0), null),
                new JournalEntryResponse(2L, uuid2, new EncryptedPayload("2025 July Entry", "iv2"),
                        LocalDateTime.of(2025, 7, 23, 10, 0), null),
                new JournalEntryResponse(3L, uuid3, new EncryptedPayload("2025 December Entry", "iv3"),
                        LocalDateTime.of(2025, 12, 23, 10, 0), null)
        );

        when(journalService.getJournalEntriesBetweenRange(testUser, from, to)).thenReturn(responses.subList(0, 2));

        mockMvc.perform(get("/api/journal")
                        .with(csrf())
                        .with(user(userDetails))
                        .param("from", "2025-06-01 00:00")
                        .param("to", "2025-07-30 23:59"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].encryptedTitle.cipherText").value("2025 July Entry"));
    }

    private static CreateJournalEntryRequest createTestJournalEntryRequest() {
        UUID uuid1 = UUID.randomUUID();
        return new CreateJournalEntryRequest( uuid1,
                new EncryptedPayload("encrypted-title", "title-iv"),
                new EncryptedPayload("encrypted-content", "content-iv"),
                List.of(new EncryptedPayload("encrypted-tag", "test")),
                new EncryptedPayload("encrypted-mood", ""),
                null,
                LocalDateTime.of(2030, 1, 1, 12, 0)
        );
    }

}
