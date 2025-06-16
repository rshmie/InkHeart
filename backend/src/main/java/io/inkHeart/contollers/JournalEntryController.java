package io.inkHeart.contollers;

import io.inkHeart.dto.*;
import io.inkHeart.entity.CustomUserDetails;
import io.inkHeart.service.JournalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/*
POST /api/journal/entry or POST /api/journal

GET /api/journal/entry/{id} ← For read

GET /api/journal ← For list

DELETE /api/journal/entry/{id} ← Delete

PUT /api/journal/entry/{id} ← Edit
 */
@RestController
@RequestMapping("/api/journal")
@Validated
public class JournalEntryController {
    private final JournalService journalService;
    public JournalEntryController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping("/create")
    public ResponseEntity<JournalEntryResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateJournalEntryRequest request) {
        var savedEntry = journalService.createEntry(userDetails.getUser(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedEntry.getId())
                .toUri();

        // Create a DTO to send back to the client, excluding sensitive info
        JournalEntryResponse response = new JournalEntryResponse(
                savedEntry.getId(),
                new EncryptedPayload(savedEntry.getEncryptedTitle().cipherText(), request.encryptedTitle().iv()),
                savedEntry.getCreatedAt(),
                savedEntry.getUpdatedAt()
        );

        System.out.println("Post journal create done : " + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/entries")
    public ResponseEntity<List<JournalGetResponse>> getAllEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<JournalGetResponse> getResponse = journalService.getAllJournalEntries(userDetails.getUsername());
        return ResponseEntity.ok().body(getResponse);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<JournalEntryResponse>> get10RecentEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<JournalEntryResponse> recentJournalEntrySummary = journalService.get10RecentEntriesForUser(userDetails.getUser());
        return ResponseEntity.ok().body(recentJournalEntrySummary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalGetResponse> getCompleteJournalEntryById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("id") Long id) {
        JournalGetResponse completeJournalEntryById = journalService.getCompleteJournalEntryById(userDetails.getUser(), id);
        return ResponseEntity.ok().body(completeJournalEntryById);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<JournalEntryResponse> deleteJournalEntryById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("id") Long id) {
        JournalEntryResponse deletedJournalEntryById = journalService.deleteJournalEntryById(userDetails.getUser(), id);
        return ResponseEntity.ok().body(deletedJournalEntryById);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JournalEntryResponse> updateJournalEntryById(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id, @RequestBody UpdateJournalEntryRequest request) {
        JournalEntryResponse updated = journalService.updateJournalEntryById(userDetails.getUser(), id, request);
        return ResponseEntity.ok().body(updated);
    }

//    @GetMapping("entry/{id}")@AuthenticationPrincipal CustomUserDetails userDetails
//    public JournalEntryResponse getJournalEntryByID() {
//
//    }

//    @GetMapping
//    public List<JournalEntryResponse> getJournalEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        System.out.println("Saving journal for: " + userDetails.getUsername());
//        return journalService.getJournalEntries(userDetails.getUsername());
//    }
//    @GetMapping("/entries")
//    public List<JournalEntryResponse> getEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        return journalService.getVisibleEntries(userDetails.getUser());
//    }

    /*
    deleteEntryById()

    updateEntry()

    getEntryById()

    searchEntriesByTag()

    getTimeLockedOrExpiredEntries()
     */
}
